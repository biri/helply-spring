package com.mta.sadna.controllers;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mta.sadna.enums.HelpCategory;
import com.mta.sadna.model.HelpPost;
import com.mta.sadna.model.NotificationEntity;
import com.mta.sadna.model.User;
import com.mta.sadna.utils.ProjectionUtils;
import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;

@Controller
@RequestMapping("/*")
public class LocationController
{
	@Value("${gcm_api_key}")
	private String gcmApiKey;
	
	@Value("${search_by_redius}")
	private double searhByRadius;
	
	Gson gson = new Gson();
	private static final Logger logger = LoggerFactory.getLogger(LocationController.class);
	
	//a patch for school presentation
	//we save for each post which users already saw it
	//so we won't show him twice. after someone accepts
	//the post we delete it from here as well
	private Map<String, List<String>> postsShownToUsers = new HashMap<String, List<String>>();			
	
	/**
	 * Updates the user location and invokes a trigger if needed
	 * @param latitude the latitude to update
	 * @param longitude the longitude to update
	 * @param facebookId the updating user id
	 * @return 
	 * @throws ElasticSearchException
	 * @throws IOException
	 */
	@RequestMapping(value = "updatelocation", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody 
	Boolean updateLocation(@RequestParam(required=true) double latitude, 
			@RequestParam(required=true) double longitude,
			@RequestParam(required=true) String facebookId) 
					throws ElasticSearchException, IOException
	{
		logger.info("updating location latitude=" + latitude
				+ " longitude=" + longitude + " facebook id=" + facebookId);
		
		User updatingUser = getUser("facebookId", facebookId);
		List<String> facebookFriendsIds = getFacebookFriendsIds(updatingUser.getAccessToken());
		if (facebookFriendsIds == null || facebookFriendsIds.isEmpty())
		{
			logger.info("No Facebook friends were found for " + facebookId);
			return true;
		}
		
		List<HelpPost> nearbyHelpPosts = getFriendsHelpPostsByLatLon
				(latitude, longitude, facebookFriendsIds);
		if (nearbyHelpPosts == null || nearbyHelpPosts.isEmpty())
		{
			logger.info("No help posts are around to trigger");
			return true;
		}
		
		//send notification
		for (HelpPost helpPost : nearbyHelpPosts)
		{
			String postId = helpPost.getUserId() + "_" + helpPost.getTime();
			if (isUserAlreadySawPost(postId, updatingUser.getFacebookId()))
				continue;
			
			User placeOwnerUser = getUser("facebookId", Long.toString(helpPost.getUserId()));
			NotificationEntity notification = buildPostNotificationEntity(
					postId,placeOwnerUser, helpPost.getFreeText(),
					helpPost.getCategory().toString(), 
					helpPost.getLatitude(), 
					helpPost.getLongitude());
			sendGcmMessage(updatingUser.getGcmRegId(),"post", gson.toJson(notification));
		}
		return true;
	}
	

	/**
	 * Send an accept notification
	 * @param facebookId the facebook id of the user who needs to get the notification
	 * @param firstName the first name of the user who sent the accept notification
	 * @param lastName the last name of the user who sent the accept notification
	 * @param postId the post accepted id
	 * @return
	 * @throws ElasticSearchException
	 * @throws IOException
	 */
	@RequestMapping(value = "sendacceptnotification", method = RequestMethod.GET, produces="application/json")
	public @ResponseBody
	Boolean sendAcceptNotification(@RequestParam(required=true) String facebookId,
			@RequestParam(required=true) String firstName,
			@RequestParam(required=true) String lastName,
			@RequestParam(required=true) String postId) 
				throws ElasticSearchException, IOException
	{
		logger.info("about to send accept message to " + facebookId);
		User user = getUser("facebookId", facebookId);
		NotificationEntity notification = buildAcceptNotificationEntity(firstName,
				lastName);
		boolean result = sendGcmMessage(user.getGcmRegId(), "accept", gson.toJson(notification));
		logger.info("sent accept notification message");
		
		logger.info("about to delete the post " + postId);
		deleteHelpPost(postId);
		logger.info("deleted post " + postId);
		return result;
	}
	
	/**
	 * Gets a user from the db
	 * @param fieldName the field name query 
	 * @param fieldValue the filed value query
	 * @return the filed value
	 * @throws ElasticSearchException
	 * @throws IOException
	 */
	private User getUser(String fieldName, String fieldValue) 
			throws ElasticSearchException, IOException
	{
		Node node = nodeBuilder().client(true).node();
		Client client = node.client();		

		SearchResponse response = client.prepareSearch().setFilter(FilterBuilders
									.termsFilter(fieldName, fieldValue.toLowerCase()))
									.execute().actionGet();
		
		SearchHit[] hits = response.getHits().getHits();
		client.close();
		
		if (hits == null)
		{
			logger.info("Hits are null");
			return null;
		}
		if (hits.length == 0)
		{
			logger.info("Hits are empty");
			return null;			
		}
		return gson.fromJson(hits[0].getSourceAsString(), User.class);		
	}
	
	/**
	 * Deletes a help post
	 * @param postId
	 */
	private void deleteHelpPost(String postId)
    {
		Node node = nodeBuilder().client(true).node();
		Client client = node.client();
		client.prepareDelete("helpposts", "post", postId).execute();
		client.close();
		
		//delete from map
		postsShownToUsers.remove(postId);
    }
	
	/**
	 * Gets Facebook friends ids for access token
	 * @param accessToken the access token
	 * @return
	 */
	private List<String> getFacebookFriendsIds(String accessToken)
	{
		List<String> friendsIds = new ArrayList<String>();
		FacebookClient facebookClient = new DefaultFacebookClient(accessToken);
		Connection<com.restfb.types.User> myFriends = 
				facebookClient.fetchConnection("me/friends", com.restfb.types.User.class);
		
		if (myFriends == null)
			return friendsIds;
		
		for (List<com.restfb.types.User> users : myFriends)
        {
			if (users == null || users.isEmpty())
				continue;
				
	        for (com.restfb.types.User user : users)
	        {
	        	friendsIds.add(user.getId());
	        }
        }
		return friendsIds;
	}
	
	/**
	 * Build post notification
	 * @param postId the post the notification is for
	 * @param placeOwnerUser the owner of the place we are notifying for
	 * @param message the message to send in the notification
	 * @param category the category of the notification
	 * @param placeLatitude the place location
	 * @param placeLongitude the place location
	 * @return the notification entity
	 */
	private NotificationEntity buildPostNotificationEntity(String postId,
			User placeOwnerUser, String message, String category, 
			double placeLatitude, double placeLongitude)
    {
		NotificationEntity notification = new NotificationEntity();
		notification.setFacebookId(placeOwnerUser.getFacebookId());
		notification.setFirstName(placeOwnerUser.getFirstName());
		notification.setLastName(placeOwnerUser.getLastName());
		notification.setMessage(message);
		notification.setCategory(HelpCategory.valueOf(category));
		notification.setLatitude(placeLatitude);
		notification.setLongitude(placeLongitude);
		notification.setPostId(postId);
		return notification;
    }
	
	/**
	 * Build accept notification
	 * @param firstName the first name of the user that accepted
	 * @param lastName the last name of the user that accepted
	 * @param latitude the current latitude of the user that accepted
	 * @param longitude the current longitude of the user that accepted
	 * @return the notification entity
	 */
	private NotificationEntity buildAcceptNotificationEntity(String firstName,
			String lastName)
    {
		NotificationEntity notification = new NotificationEntity();
		notification.setFirstName(firstName);
		notification.setLastName(lastName);
		return notification;
    }
	
	/**
	 * Thread issues!!
	 * Checks if a user already saw this post
	 * @param helpPost
	 * @return
	 */
	private boolean isUserAlreadySawPost(String postId, String facebookId)
    {
		if (postsShownToUsers.get(postId) == null)
		{
			List<String> usersList = new ArrayList<String>();
			usersList.add(facebookId);
			
			postsShownToUsers.put(postId, usersList);
			return false;
		}
		
		List<String> usersList = postsShownToUsers.get(postId);
		if (usersList.contains(facebookId))
			return true;
		
		usersList.add(facebookId);
		return false;
    }
	
	/**
	 * Sends a message to a user using GCM
	 * @param gcmRegId the gcm registration id
	 * @param messageText the message to send
	 * @throws IOException
	 */
	private boolean sendGcmMessage(String gcmRegId, String type, String messageText) throws IOException
    {
		logger.info("GCM reg id = " + gcmRegId);
		logger.info("message = " + messageText);
		
		Sender sender = new Sender(gcmApiKey);
		Message message = new Message.Builder()
		.addData("type", type).addData("message", messageText).build();
		
		Result result = sender.send(message, gcmRegId, 5);
		if (result.getErrorCodeName() != null)
		{
			logger.error("Failed to send gcm message to " 
					+ gcmRegId + " - " + result.getErrorCodeName());
			return false;
		}
		return true;
    }
	
	/**
	 * Get help posts by latitude longitude and facebook friends
	 * @param latitude the latitude to search by
	 * @param longitude the longitude to search by
	 * @param facebookIds friends facebook ids
	 * @return
	 */
	private List<HelpPost> getFriendsHelpPostsByLatLon(double latitude,
			double longitude, List<String> facebookIds)
	{
		logger.info("About to get near by posts for lat=" + latitude +
				" lon=" + longitude + " radius=" + searhByRadius);
		
		Node node = nodeBuilder().client(true).node();
		Client client = node.client();		

		SearchResponse response = client.prepareSearch()
        .setFilter(FilterBuilders.geoDistanceFilter("location")
        		.distance(searhByRadius, DistanceUnit.KILOMETERS)
        		.lat(latitude).lon(longitude))
        .setFilter(FilterBuilders.termsFilter("userId", facebookIds))
        .execute()
        .actionGet();

		List<HelpPost> helpPosts = new ArrayList<HelpPost>();
		SearchHit[] hits = response.getHits().getHits();
		for (SearchHit searchHit : hits)
		{
			JsonElement jsonElement = gson.
					fromJson(searchHit.getSourceAsString(), JsonElement.class);
			HelpPost helpPost = ProjectionUtils.jsonToHelpPost(jsonElement);
			helpPosts.add(helpPost);
		}

		client.close();
		return helpPosts;
	}
}

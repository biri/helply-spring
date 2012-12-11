package com.mta.sadna;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mta.sadna.enums.HelpCategory;
import com.mta.sadna.model.HelpPost;
import com.mta.sadna.model.NotificationEntity;
import com.mta.sadna.model.User;
import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;

@Controller
@RequestMapping("/*")
public class PostsController
{
	@Value("${gcm_api_key}")
	private String gcmApiKey;
	
	Gson gson = new Gson();
	private static final Logger logger = LoggerFactory.getLogger(PostsController.class);

	@RequestMapping(value = "test", method = RequestMethod.GET, produces="application/json")
	public @ResponseBody 
	String home()
	{
		logger.info("test " + new Date());
		return "Helply Server";
	}

	/**
	 * Saves a help post in the DB
	 * @param helpPost the help post
	 * @return true/false
	 * @throws ElasticSearchException
	 * @throws IOException
	 */
	@RequestMapping(value = "posthelp", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody
	Boolean postHelp(@RequestBody HelpPost helpPost) throws ElasticSearchException, IOException
	{
		logger.info("About to save post created by user - " + helpPost.getUserId());
		Node node = nodeBuilder().client(true).node();
		Client client = node.client();

		Map<String,Object> locationVals = new HashMap<String, Object>();
		locationVals.put("lat", helpPost.getLatitude());
		locationVals.put("lon", helpPost.getLongitude());

		client.prepareIndex("helpposts", "post")
			.setId(helpPost.getTriggerId())
			.setSource(jsonBuilder()
                .startObject()
                    .field("userId", helpPost.getUserId())
                    .field("text", helpPost.getFreeText())
                    .field("date", System.currentTimeMillis())
                    .field("category", helpPost.getCategory())
                    .field("location", locationVals)
                    .field("triggerId", helpPost.getTriggerId())
                .endObject())
		    .execute()
		    .actionGet();

		client.close();
		logger.info("Saved post by user - " + helpPost.getUserId());
		return true;
	}
	
	/**
	 * Gets all help posts from a radius of 10 KM
	 * only those who answer the facebook ids clause
	 * @param latitude
	 * @param longitude
	 * @param facebookIds
	 * @return
	 */
	@RequestMapping(value = "getnearbyhelpposts", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody 
	List<HelpPost> getNearbyHelpPosts(@RequestParam(required=true) double latitude, 
			@RequestParam(required=true) double longitude,
			@RequestParam(required=true) List<String> facebookIds)
	{
		logger.info("About to get near by posts for lat=" + latitude +
				" lon=" + longitude);
		Node node = nodeBuilder().client(true).node();
		Client client = node.client();		

		SearchResponse response = client.prepareSearch()
        .setFilter(FilterBuilders.geoDistanceFilter("location")
        		.distance(10, DistanceUnit.KILOMETERS).lat(latitude).lon(longitude))
        .setFilter(FilterBuilders.termsFilter("userId", facebookIds))
        .execute()
        .actionGet();

		List<HelpPost> helpPosts = new ArrayList<HelpPost>();
		SearchHit[] hits = response.getHits().getHits();
		for (SearchHit searchHit : hits)
		{
			JsonElement jsonElement = gson.
					fromJson(searchHit.getSourceAsString(), JsonElement.class);
			HelpPost helpPost = fromJsonElement(jsonElement);
			helpPosts.add(helpPost);
		}

		client.close();
		return helpPosts;
		
	}
	
	private HelpPost fromJsonElement(JsonElement jsonElment)
	{
		HelpPost helpPost = new HelpPost();
		helpPost.setUserId(jsonElment.getAsJsonObject().get("userId").getAsLong());
		helpPost.setFreeText(jsonElment.getAsJsonObject().get("text").getAsString());
		helpPost.setTriggerId(jsonElment.getAsJsonObject().get("triggerId").getAsString());
		helpPost.setCategory(HelpCategory.valueOf(jsonElment.getAsJsonObject().get("category").getAsString()));
		helpPost.setLatitude(((JsonObject)jsonElment.getAsJsonObject().get("location")).get("lat").getAsDouble());
		helpPost.setLongitude(((JsonObject)jsonElment.getAsJsonObject().get("location")).get("lon").getAsDouble());
		return helpPost;
	}
	
	@RequestMapping(value = "invoketrigger", method = RequestMethod.POST)
	public @ResponseBody
	String postHelp(@RequestBody String json) throws ElasticSearchException, IOException
	{
		JsonElement jsonElment = gson.fromJson(json, JsonElement.class);
		
		String triggerId = jsonElment.getAsJsonObject().get("trigger")
				.getAsJsonObject().get("trigger_id").getAsString();
		logger.info("Triggered id = " + triggerId + " was invoked");
		
		if (!doesHelpPostExist(triggerId))
		{
			logger.info("Triggered id = " + triggerId 
					+ " does not exist, trigger will be ignored");
			return "";
		}
		logger.info("Triggered id = " + triggerId 
				+ " exists, notification will be sent");
		
		String triggeredUserGlId = jsonElment.getAsJsonObject().get("user")
				.getAsJsonObject().get("user_id").getAsString();
		logger.info("Triggered User geoloqi id = " + triggeredUserGlId);
		
		User triggeredUser = getUser("geoloqiId", triggeredUserGlId);
		if (triggeredUser == null)
			return "";
		else
			logger.info("Triggered User facebook id = " 
					+ triggeredUser.getFacebookId());
		
		String placeOwnerFbId = jsonElment.getAsJsonObject().get("place")
				.getAsJsonObject().get("description").getAsString();
		logger.info("Place creator facebook id = " + placeOwnerFbId);
		
		User placeOwnerUser = getUser("facebookId", placeOwnerFbId);
		if (placeOwnerUser == null)
			return "";
		else
			logger.info("Got the place owner user entity");
		
		//checks if the triggered user is friends with the place owner
		if (!areUsersFriends(triggeredUser.getAccessToken(), placeOwnerFbId))
		{
			logger.info("trigger " + triggerId + " was fired but the users" +
					placeOwnerFbId + " and " + triggeredUser.getFacebookId()
					+ " were not friends");
			return "";
		}
		
		String message = jsonElment.getAsJsonObject().get("place")
				.getAsJsonObject().get("name").getAsString();
		
		double placeLatitude = jsonElment.getAsJsonObject().get("place")
				.getAsJsonObject().get("latitude").getAsDouble();
		logger.info("Place latitude = " + placeLatitude);
		
		double placeLongitude = jsonElment.getAsJsonObject().get("place")
				.getAsJsonObject().get("longitude").getAsDouble();
		logger.info("Place longitude = " + placeLongitude);
		
		String[] split = message.split("-------");
		NotificationEntity notification = buildPostNotificationEntity(triggerId,
				placeOwnerUser, split[0], split[1], placeLatitude, placeLongitude);
		
		sendGcmMessage(triggeredUser.getGcmRegId(), "post", gson.toJson(notification));
		logger.info("sent post notification message");
		return "";
	}
	
	@RequestMapping(value = "sendacceptnotification", method = RequestMethod.GET, produces="application/json")
	public @ResponseBody
	Boolean sendAcceptNotification(@RequestParam(required=true) String facebookId,
			@RequestParam(required=true) String firstName,
			@RequestParam(required=true) String lastName,
			@RequestParam(required=true) double latitude,
			@RequestParam(required=true) double longitude,
			@RequestParam(required=true) String triggerId) 
				throws ElasticSearchException, IOException
	{
		logger.info("about to send accept message to " + facebookId);
		User user = getUser("facebookId", facebookId);
		NotificationEntity notification = buildAcceptNotificationEntity(firstName,
				lastName, latitude, longitude);
		boolean result = sendGcmMessage(user.getGcmRegId(), "accept", gson.toJson(notification));
		logger.info("sent accept notification message");
		
		logger.info("about to delete a post with trigger id " + triggerId);
		deleteHelpPost(triggerId);
		logger.info("deleted a post with trigger id " + triggerId);
		return result;
	}
	
	private boolean doesHelpPostExist(String triggerId) 
			throws ElasticSearchException, IOException
	{
		Node node = nodeBuilder().client(true).node();
		Client client = node.client();		

		SearchResponse response = client.prepareSearch().setFilter(FilterBuilders
									.termsFilter("triggerId", triggerId.toLowerCase()))
									.execute().actionGet();
		
		SearchHit[] hits = response.getHits().getHits();
		client.close();
		
		if (hits == null || hits.length == 0)
			return false;
		return true;		
	}	

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
	 * Check if the friend id is a friend of the user who owns
	 * the access token
	 * @param accessToken access token to get friends
	 * @param friendId the friend to check
	 * @return true/false
	 */
	private boolean areUsersFriends(String accessToken, String checkFriendId)
	{
		FacebookClient facebookClient = new DefaultFacebookClient(accessToken);
		Connection<com.restfb.types.User> myFriends = 
				facebookClient.fetchConnection("me/friends", com.restfb.types.User.class);
		
		for (List<com.restfb.types.User> users : myFriends)
        {
	        for (com.restfb.types.User user : users)
	        {
	        	if (user.getId().equals(checkFriendId))
	        		return true;
	        }
        }
		return false;
	}
	
	private NotificationEntity buildPostNotificationEntity(String triggerId,
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
		notification.setTriggerId(triggerId);
		return notification;
    }
	
	private NotificationEntity buildAcceptNotificationEntity(String firstName,
			String lastName, double latitude, double longitude)
    {
		NotificationEntity notification = new NotificationEntity();
		notification.setFirstName(firstName);
		notification.setLastName(lastName);
		notification.setLatitude(latitude);
		notification.setLongitude(longitude);
		return notification;
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
	
	private void deleteHelpPost(String triggerId)
    {
		Node node = nodeBuilder().client(true).node();
		Client client = node.client();
		client.prepareDelete("helpposts", "post", triggerId).execute();
		client.close();
    }
	
	
}

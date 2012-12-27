package com.mta.sadna.controllers;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mta.sadna.model.HelpPost;
import com.mta.sadna.utils.ProjectionUtils;

@Controller
@RequestMapping("/*")
public class PostsController
{
	@Value("${find_friends_by_redius}")
	private double findFriendsByRadius;
	
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
		helpPost.setTime(System.currentTimeMillis());
		
		Node node = nodeBuilder().client(true).node();
		Client client = node.client();

		Map<String,Object> locationVals = new LinkedHashMap<String, Object>();
		locationVals.put("lat", helpPost.getLatitude());
		locationVals.put("lon", helpPost.getLongitude());

		client.prepareIndex("helpposts", "post")
			.setId(helpPost.getUserId() + "_" + helpPost.getTime())
			.setSource(jsonBuilder()
                .startObject()
                    .field("userId", helpPost.getUserId())
                    .field("text", helpPost.getFreeText())
                    .field("time", helpPost.getTime())
                    .field("category", helpPost.getCategory())
                    .field("location", locationVals)
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
        		.distance(findFriendsByRadius, DistanceUnit.KILOMETERS)
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

package com.mta.sadna;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.IOException;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mta.sadna.model.User;

@Controller
@RequestMapping("/*")
public class UsersController
{
	private static final Logger logger = LoggerFactory.getLogger(UsersController.class);
	
	@RequestMapping(value = "getuserbyfacebookid", method = RequestMethod.GET)
	public @ResponseBody
	String getUserByFacebookId(@RequestParam(required=true) String facebookId,
			@RequestParam(required=true) String geoloqiId) 
			throws ElasticSearchException, IOException
	{
		logger.info("Checking if user " + facebookId + "  alredy exists");
		Node node = nodeBuilder().client(true).node();
		Client client = node.client();		
		
		SearchResponse response = client.prepareSearch()
				.setQuery(QueryBuilders.fieldQuery("facebookId",facebookId)).execute().actionGet();
		
		Gson gson = new Gson(); 
		SearchHit[] hits = response.getHits().getHits();
		client.close();
		
		if (hits == null || hits.length == 0)
		{
			logger.info("User " + facebookId + "  was not found");
			return null;
		}
		if (hits.length != 1)
		{
			logger.info("Found more than 1 user answering the id, Fatal error!");
			return null;
		}		
		
		logger.info("User " + facebookId + "  was found. Results = " + hits.length);
		JsonElement jsonElement = gson.
				fromJson(hits[0].getSourceAsString(), JsonElement.class);
		String geoloqiIdFromDb = jsonElement.getAsJsonObject().get("geoloqiId").getAsString();
		if (!geoloqiId.equalsIgnoreCase(geoloqiIdFromDb))
		{
			logger.info("Geoloqi Id doesn't match, deleting user");
			deleteUser(facebookId);
			return null;
		}
		return jsonElement.getAsJsonObject().get("facebookId").getAsString();
	}
	
	private void deleteUser(String facebookId)
    {
		logger.info("About to delete user - " + facebookId);
		Node node = nodeBuilder().client(true).node();
		Client client = node.client();
		client.prepareDelete("users", "user", facebookId).execute();
		client.close();
		logger.info("Deleted user - " + facebookId);
    }

	@RequestMapping(value = "saveuser", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody
	Boolean saveUser(@RequestBody User user) throws ElasticSearchException, IOException
	{
		logger.info("About to save user - " + user.getFacebookId() +
				" -- " + user.getGeoloqiId());
		Node node = nodeBuilder().client(true).node();
		Client client = node.client();

		client.prepareIndex("users", "user")
			.setId(user.getFacebookId())
			.setSource(jsonBuilder()
                .startObject()
                    .field("facebookId", user.getFacebookId())
                    .field("facebookUserName", user.getFacebookUserName())
                    .field("firstName", user.getFirstName())
                    .field("lastName", user.getLastName())
                    .field("accessToken", user.getAccessToken())
                    .field("gcmRegId", user.getGcmRegId())
                    .field("geoloqiId", user.getGeoloqiId())
                .endObject())
		    .execute()
		    .actionGet();

		client.close();
		logger.info("Saved user - " + user.getFacebookId() +
				" -- " + user.getGeoloqiId());
		return true;
	}
}

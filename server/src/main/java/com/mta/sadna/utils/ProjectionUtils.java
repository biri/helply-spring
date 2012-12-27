package com.mta.sadna.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mta.sadna.enums.HelpCategory;
import com.mta.sadna.model.HelpPost;

public class ProjectionUtils
{
	public static HelpPost jsonToHelpPost(JsonElement jsonElment)
	{
		HelpPost helpPost = new HelpPost();
		helpPost.setTime(jsonElment.getAsJsonObject().get("time").getAsLong());
		helpPost.setUserId(jsonElment.getAsJsonObject().get("userId").getAsLong());
		helpPost.setFreeText(jsonElment.getAsJsonObject().get("text").getAsString());
		helpPost.setCategory(HelpCategory.valueOf(jsonElment.getAsJsonObject().get("category").getAsString()));
		helpPost.setLatitude(((JsonObject)jsonElment.getAsJsonObject().get("location")).get("lat").getAsDouble());
		helpPost.setLongitude(((JsonObject)jsonElment.getAsJsonObject().get("location")).get("lon").getAsDouble());
		return helpPost;
	}
}

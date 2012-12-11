package com.mta.sadna.activities;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.mta.sadna.R;
import com.mta.sadna.activities.map.HelplyMapActivity;
import com.mta.sadna.model.HelpPost;

public class PostsMapActivity extends HelplyMapActivity
{
	protected static final String TAG = PostsMapActivity.class.getSimpleName();

	@Override
	protected void setContentView()
	{
		setContentView(R.layout.posts_map_layout);
	}

	@Override
	protected void doOnLocationChanged(Location location)
	{
		new GetNearbyPostsTask().execute();
	}
	
	private class GetNearbyPostsTask extends AsyncTask<Void, Void, List<HelpPost>>
	{
		Map<Long, Bitmap> userProfileImage = new HashMap<Long, Bitmap>();
		
		@Override
		protected void onPreExecute()
		{
			showLoadingProgressDialog();
		}

		@Override
		protected List<HelpPost> doInBackground(Void... params)
		{
			try
			{
				Log.i(TAG, "About to get nearby posts");
				String friendsStr = buildFriendsStr();
				final String url = getMainApplication().getRestBaseUrl()
						+ "/getnearbyhelpposts?latitude=" + latitude 
						+ "&longitude=" + longitude
						+ "&facebookIds=" + friendsStr;
				
				HttpHeaders requestHeaders = new HttpHeaders();
				List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
				acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
				requestHeaders.setAccept(acceptableMediaTypes);
				HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);

				RestTemplate restTemplate = new RestTemplate();
				restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());

				ResponseEntity<HelpPost[]> responseEntity = restTemplate
						.exchange(new URI(url), HttpMethod.GET, requestEntity,
						HelpPost[].class);
				
				List<HelpPost> helpPosts = Arrays.asList(responseEntity.getBody());
				if (helpPosts != null && !helpPosts.isEmpty())
				{
					getUsersProfileImages(helpPosts);
				}
				return helpPosts;
			}
			catch (Exception e)
			{
				Log.e(TAG, "Failed to get nearby posts - " 
						+ e.getMessage(), e);
			}
			return null;
		}

		/**
		 * Convert friends list to CSV
		 */
		private String buildFriendsStr()
        {
			List<String> friendIds = facebookApi.friendOperations().getFriendIds();
			String friendsCsv = StringUtils.collectionToDelimitedString(friendIds, ",");
			Log.i(TAG, "Friends csv - " + friendsCsv);
			return friendsCsv;
        }

		/**
		 * Must be help at a backgroup thread
		 * @param helpPosts the results from the server
		 */
		private void getUsersProfileImages(List<HelpPost> helpPosts)
        {
			for (HelpPost helpPost : helpPosts)
			{
				if (userProfileImage.containsKey(helpPost.getUserId()))
					continue;
				
				byte[] imageByteArr = facebookApi.userOperations().
					getUserProfileImage(Long.toString(helpPost.getUserId()));
				
				Bitmap profileImg =  BitmapFactory.
						decodeByteArray(imageByteArr , 0, imageByteArr .length);
				userProfileImage.put(helpPost.getUserId(), profileImg);
			}
	        
        }

		@Override
		protected void onPostExecute(List<HelpPost> result)
		{
			dismissProgressDialog();
			addLoggedInUserOverlay();
			
			if (result == null)
			{
				Toast.makeText(PostsMapActivity.this,
						"Failed to get posts", Toast.LENGTH_LONG).show();
				zoomMapByLatLon(PostsMapActivity.this.latitude,
						PostsMapActivity.this.longitude);
				return;
			}
			
			if (result.isEmpty())
			{
				Toast.makeText(PostsMapActivity.this,
						"No posts by friends were found",
						Toast.LENGTH_LONG).show();
				zoomMapByLatLon(PostsMapActivity.this.latitude,
						PostsMapActivity.this.longitude);
				return;
			}
			
			for (HelpPost helpPost : result)
			{
				addOverlayToMap(helpPost.getLatitude(), helpPost.getLongitude(),
						helpPost.getCategory().toString(), helpPost.getFreeText(),
						helpPost, userProfileImage.get(helpPost.getUserId()));
			}
			
			zoomMapByLatLon(PostsMapActivity.this.latitude,
					PostsMapActivity.this.longitude);
		}
		
		private void addLoggedInUserOverlay()
		{
			String firstName = getMainApplication().getData("firstName").toString();
			String lastName = getMainApplication().getData("lastName").toString();
			Bitmap profileImg = (Bitmap)getMainApplication().getData("profilePicture");
			addOverlayToMap(latitude, longitude, "You are here",
					firstName + " " + lastName, profileImg);			
		}
	}
}

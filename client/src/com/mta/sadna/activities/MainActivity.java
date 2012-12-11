package com.mta.sadna.activities;

import java.net.URI;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.FacebookProfile;
import org.springframework.web.client.RestTemplate;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.geoloqi.android.sdk.LQSession;
import com.google.android.gcm.GCMRegistrar;
import com.mta.sadna.AbstractMenuActivity;
import com.mta.sadna.R;
import com.mta.sadna.model.User;

public class MainActivity extends AbstractMenuActivity
{
	private Facebook facebookApi;
	private LQSession geoLoqiBinding;
	protected static final String TAG = MainActivity.class.getSimpleName();

	@Override
	protected void initActivity()
	{
		// get the Facebook api
		this.facebookApi =
		        getApplicationContext().getConnectionRepository()
		        .findPrimaryConnection(Facebook.class).getApi();
		
		//get geo loqi binding and credentials
		geoLoqiBinding = getApplicationContext().getGeoLoqiBinding();
		getGeoLoqiUserId();
		
		//register user to GCM
		registerGcmClient();
		new SaveUserTask().execute();
	}


    private void getGeoLoqiUserId()
    {
		if (getApplicationContext().getData("geoloqiId") == null)
		{
	    	Log.i(TAG, "About to get Geoloqi info");
			getApplicationContext().addData("geoloqiId", geoLoqiBinding.getUserId());
		}
    }
    
	private void registerGcmClient()
	{
		try
		{
			// Check that the device supports GCM
			GCMRegistrar.checkDevice(this);
			// Check the manifest to be sure 
			//this app has all the required permissions.
			GCMRegistrar.checkManifest(this);
			// Get the existing registration id, if it exists.
			String gcmRegId = GCMRegistrar.getRegistrationId(this);

			if (gcmRegId.equals(""))
			{
				// register this device for this project
				Log.i(TAG, "GCM client is about to get registered");
				GCMRegistrar.register(this, getApplicationContext().getProjectId());
			}
			else
			{
				//save GCM registation id
				Log.i(TAG, "GCM client is already registered " + gcmRegId);
				getApplicationContext().addData("gcmRegId", gcmRegId);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.d(TAG, e.getMessage());
		}
	}

	@Override
	protected OnItemClickListener getMenuOnItemClickListener()
	{
		return new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parentView, View childView, int position, long id)
			{
				Class<?> cls = null;
				switch (position)
				{
					case 0:
						cls = PostHelpActivity.class;
						startActivity(new Intent(parentView.getContext(), cls));
						break;
					case 1:
						cls = PostsMapActivity.class;
						startActivity(new Intent(parentView.getContext(), cls));
						break;
					case 2:
						disconnect();
						cls = SignInActivity.class;
						startActivity(new Intent(parentView.getContext(), cls));
						finish();
					default:
						break;
				}
			}
		};
	}
	
	@Override
	protected void onDestroy()
	{
	    super.onDestroy();
	    getApplicationContext().releaseGeoLoqiBinding();
	}
	
	@Override
	protected String[] getMenuItems()
	{
		return getResources().getStringArray(R.array.main_menu_items);
	}
	
	private void disconnect() 
	{
		getApplicationContext().getConnectionRepository()
			.removeConnections(getApplicationContext()
					.getFacebookConnectionFactory().getProviderId());
		getApplicationContext().clearData();
	}
	
	private class SaveUserTask extends AsyncTask<Void, Void, Boolean>
	{
		@Override
        protected Boolean doInBackground(Void... arg0)
        {
			getFacebookCredentials();
			if (getUserFromDB() == null)
				saveUserToDB();
			return true;
        }
		
		/**
		 * Get the user Facebook information
		 * If we already have it then ignore
		 */
	    private void getFacebookCredentials()
		{
			if (getApplicationContext().getData("facebookId") == null)
			{
		    	Log.i(TAG, "About to get Facebook info");
				FacebookProfile userProfile = facebookApi.userOperations().getUserProfile();
				getApplicationContext().addData("facebookId", userProfile.getId());
				getApplicationContext().addData("facebookUserName", userProfile.getUsername());
				getApplicationContext().addData("firstName", userProfile.getFirstName());
				getApplicationContext().addData("lastName", userProfile.getLastName());
				
				byte[] b = facebookApi.userOperations().getUserProfileImage();
				Bitmap userImg =  BitmapFactory.decodeByteArray(b , 0, b .length);
				getApplicationContext().addData("profilePicture", userImg);
			}
		}
		
		private String getUserFromDB()
		{
			try
			{
				User user = buildUserFromSession();
				Log.i(TAG, "about to check if user " + user.getFacebookId() + " already exists");
				final String url = MainActivity.this.getApplicationContext()
						.getRestBaseUrl() + "/getuserbyfacebookid" +
						"?facebookId=" + user.getFacebookId() + 
						"&geoloqiId=" + user.getGeoloqiId();
	
				// Create a new RestTemplate instance
				RestTemplate restTemplate = new RestTemplate();
				restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
				return restTemplate.getForObject(new URI(url), String.class);
			}
			catch(Exception exception)
			{
				Log.e(TAG, "Failed to check if user already exists - " + exception.getMessage());
				return null;
			}
		}
		
		private Boolean saveUserToDB()
	    {
			try
			{
				User user = buildUserFromSession();
				Log.i(TAG, "saving user " + user.getFacebookId() + " to DB");
				final String url = MainActivity.this.getApplicationContext()
						.getRestBaseUrl() + "/saveuser";
	
				HttpHeaders requestHeaders = new HttpHeaders();
				requestHeaders.setContentType(MediaType.APPLICATION_JSON);
				HttpEntity<User> requestEntity = new HttpEntity<User>(user, requestHeaders);
	
				RestTemplate restTemplate = new RestTemplate();
				restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
				ResponseEntity<Boolean> response =
				        restTemplate.exchange(new URI(url), HttpMethod.POST, requestEntity, Boolean.class); 
				return response.getBody();
			}
			catch(Exception exception)
			{
				Log.e(TAG, "Failed to create user - " + exception.getMessage());
				return null;
			}			
	    }

		private User buildUserFromSession()
	    {
	        User user = new User();
	        user.setFacebookId(getApplicationContext().getData("facebookId").toString());
	        user.setFacebookUserName(getApplicationContext().getData("facebookUserName").toString());
	        user.setFirstName(getApplicationContext().getData("firstName").toString());
	        user.setLastName(getApplicationContext().getData("lastName").toString());
	        user.setAccessToken(getApplicationContext().getData("accessToken").toString());
	        user.setGcmRegId(getApplicationContext().getData("gcmRegId").toString());
	        user.setGeoloqiId(getApplicationContext().getData("geoloqiId").toString());
			return user;
	    }
	}
}

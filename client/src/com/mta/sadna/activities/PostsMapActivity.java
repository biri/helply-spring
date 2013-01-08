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
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.social.facebook.api.FacebookProfile;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.mta.sadna.R;
import com.mta.sadna.activities.map.HelplyMapActivity;
import com.mta.sadna.enums.HelpCategory;
import com.mta.sadna.model.HelpPost;
import com.mta.sadna.model.User;

public class PostsMapActivity extends HelplyMapActivity
{
	private View popupLayout;
	protected static final String TAG = PostsMapActivity.class.getSimpleName();

	@Override
	protected void setContentView()
	{
		setContentView(R.layout.main_activity_layout);
	}

	@Override
	protected void init()
	{
		// register user to GCM
		registerGcmClient();

		// bind disconnect button
		bindLogoffBtn();

		// bind refresh button
		bindRefreshBtn();

		// bind add new help post button
		bindAddHelpBtn();
		
		//bind show help btn
		bindShowHelpBtn();		

		new SaveUserTask().execute();
	}

	@Override
	protected void doOnLocationChanged(Location location)
	{
		new GetNearbyPostsTask().execute();
	}

	private void registerGcmClient()
	{
		try
		{
			// Check that the device supports GCM
			GCMRegistrar.checkDevice(this);
			// Check the manifest to be sure
			// this app has all the required permissions.
			GCMRegistrar.checkManifest(this);
			// Get the existing registration id, if it exists.
			String gcmRegId = GCMRegistrar.getRegistrationId(this);

			if (gcmRegId.equals(""))
			{
				// register this device for this project
				Log.i(TAG, "GCM client is about to get registered");
				GCMRegistrar.register(this, getMainApplication().getProjectId());
			}
			else
			{
				// save GCM registation id
				Log.i(TAG, "GCM client is already registered " + gcmRegId);
				getMainApplication().addData("gcmRegId", gcmRegId);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.d(TAG, e.getMessage());
		}
	}

	private void bindLogoffBtn()
	{
		Typeface font = Typeface.createFromAsset(getAssets(), "font/myster.ttf");
		
		Button logOffBtn = (Button) findViewById(R.id.logoffBtn);
		logOffBtn.setTypeface(font);
		logOffBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				disconnect();
				startActivity(new Intent(v.getContext(), SignInActivity.class));
				finish();
			}
		});
	}

	private void disconnect()
	{
		getMainApplication().getConnectionRepository().removeConnections(
		        getMainApplication().getFacebookConnectionFactory().getProviderId());
		getMainApplication().clearData();
	}

	private void bindRefreshBtn()
	{
		ImageButton refreshBtn = (ImageButton) findViewById(R.id.refreshBtn);
		refreshBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				new GetNearbyPostsTask().execute();
			}
		});
	}

	private void bindAddHelpBtn()
	{
		ImageButton addHelpBtn = (ImageButton) findViewById(R.id.addHelp);
		addHelpBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showAddHelpPopup(PostsMapActivity.this);
			}
		});
	}
	
	private void bindShowHelpBtn()
	{
		ImageButton helpBtn = (ImageButton) findViewById(R.id.help);
		helpBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showHelpPopup(PostsMapActivity.this);
			}
		});
	}	

	private void showAddHelpPopup(final Activity context)
	{
		//get the screen size
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int screenWidth = size.x;
		int screenHeight = size.y;

		// Inflate the post_help_layout.xml
		LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.popup);
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		popupLayout = layoutInflater.inflate(R.layout.post_help_layout, viewGroup);

		// Creating the PopupWindow
		final PopupWindow popup = new PopupWindow(context);
		popup.setContentView(popupLayout);
		popup.setWidth(screenWidth-100);
		popup.setHeight(screenHeight-300);
		popup.setFocusable(true);

		// Clear the default translucent background
		popup.setBackgroundDrawable(new BitmapDrawable(context.getResources()));

		// Displaying the popup at the specified location, + offsets.
		popup.showAtLocation(popupLayout, Gravity.CENTER, 0, 0);

		// Getting a reference to send button, and close the popup when clicked.
		Typeface font = Typeface.createFromAsset(getAssets(), "font/myster.ttf");
		Button submit = (Button) popupLayout.findViewById(R.id.submitBtn);
		submit.setTypeface(font);
		submit.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				new PostHelpTask().execute();
				popup.dismiss();
			}
		});
	}
	
	private void showHelpPopup(final Activity context)
	{
		//get the screen size
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int screenWidth = size.x;
		int screenHeight = size.y;

		// Inflate the post_help_layout.xml
		LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.helpPopup);
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		popupLayout = layoutInflater.inflate(R.layout.help_page_layout, viewGroup);

		// Creating the PopupWindow
		final PopupWindow popup = new PopupWindow(context);
		popup.setContentView(popupLayout);
		popup.setWidth(screenWidth-50);
		popup.setHeight(screenHeight-300);
		popup.setFocusable(true);

		// Clear the default translucent background
		popup.setBackgroundDrawable(new BitmapDrawable(context.getResources()));

		// Displaying the popup at the specified location, + offsets.
		popup.showAtLocation(popupLayout, Gravity.CENTER, 0, 0);
	}

	private String getFacebookId()
	{
		return getMainApplication().getData("facebookId").toString();
	}

	private void showResult(Boolean result)
	{
		if (result != null && result)
			Toast.makeText(this, "Posted Successfully", Toast.LENGTH_LONG).show();
		else
			Toast.makeText(this, "Posting failed", Toast.LENGTH_LONG).show();
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
			if (getMainApplication().getData("facebookId") == null)
			{
				Log.i(TAG, "About to get Facebook info");
				FacebookProfile userProfile = facebookApi.userOperations().getUserProfile();
				getMainApplication().addData("facebookId", userProfile.getId());
				getMainApplication().addData("facebookUserName", userProfile.getUsername());
				getMainApplication().addData("firstName", userProfile.getFirstName());
				getMainApplication().addData("lastName", userProfile.getLastName());

				byte[] b = facebookApi.userOperations().getUserProfileImage();
				Bitmap userImg = BitmapFactory.decodeByteArray(b, 0, b.length);
				getMainApplication().addData("profilePicture", userImg);
			}
		}

		private String getUserFromDB()
		{
			try
			{
				User user = buildUserFromSession();
				Log.i(TAG, "about to check if user " + user.getFacebookId() + " already exists");
				final String url =
				        getMainApplication().getRestBaseUrl() + "/getuserbyfacebookid"
				        		+ "?facebookId=" + user.getFacebookId();

				// Create a new RestTemplate instance
				RestTemplate restTemplate = new RestTemplate();
				restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
				return restTemplate.getForObject(new URI(url), String.class);
			}
			catch (Exception exception)
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
				final String url = getMainApplication().getRestBaseUrl() + "/saveuser";

				HttpHeaders requestHeaders = new HttpHeaders();
				requestHeaders.setContentType(MediaType.APPLICATION_JSON);
				HttpEntity<User> requestEntity = new HttpEntity<User>(user, requestHeaders);

				RestTemplate restTemplate = new RestTemplate();
				restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
				ResponseEntity<Boolean> response =
				        restTemplate.exchange(new URI(url), HttpMethod.POST, requestEntity, Boolean.class);
				return response.getBody();
			}
			catch (Exception exception)
			{
				Log.e(TAG, "Failed to create user - " + exception.getMessage());
				return null;
			}
		}

		private User buildUserFromSession()
		{
			User user = new User();
			user.setFacebookId(getMainApplication().getData("facebookId").toString());
			user.setFacebookUserName(getMainApplication().getData("facebookUserName").toString());
			user.setFirstName(getMainApplication().getData("firstName").toString());
			user.setLastName(getMainApplication().getData("lastName").toString());
			user.setAccessToken(getMainApplication().getData("accessToken").toString());
			user.setGcmRegId(getMainApplication().getData("gcmRegId").toString());
			return user;
		}
	}

	private class GetNearbyPostsTask extends AsyncTask<Void, Void, List<HelpPost>>
	{
		Map<Long, Bitmap> userProfileImage = new HashMap<Long, Bitmap>();

		@Override
		protected void onPreExecute()
		{
			showLoadingProgressDialog();
			postsOverlays.clearAllOverlay();
		}

		@Override
		protected List<HelpPost> doInBackground(Void... params)
		{
			try
			{
				Log.i(TAG, "About to get nearby posts");
				String friendsStr = buildFriendsStr();
				final String url =
				        getMainApplication().getRestBaseUrl() + "/getnearbyhelpposts?latitude=" + latitude +
				                "&longitude=" + longitude + "&facebookIds=" + friendsStr;

				HttpHeaders requestHeaders = new HttpHeaders();
				List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
				acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
				requestHeaders.setAccept(acceptableMediaTypes);
				HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);

				RestTemplate restTemplate = new RestTemplate();
				restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());

				ResponseEntity<HelpPost[]> responseEntity =
				        restTemplate.exchange(new URI(url), HttpMethod.GET, requestEntity, HelpPost[].class);

				List<HelpPost> helpPosts = Arrays.asList(responseEntity.getBody());
				if (helpPosts != null && !helpPosts.isEmpty())
				{
					getUsersProfileImages(helpPosts);
				}
				return helpPosts;
			}
			catch (Exception e)
			{
				Log.e(TAG, "Failed to get nearby posts - " + e.getMessage(), e);
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
		 * 
		 * @param helpPosts
		 *            the results from the server
		 */
		private void getUsersProfileImages(List<HelpPost> helpPosts)
		{
			for (HelpPost helpPost : helpPosts)
			{
				if (userProfileImage.containsKey(helpPost.getUserId()))
					continue;

				byte[] imageByteArr =
				        facebookApi.userOperations().getUserProfileImage(Long.toString(helpPost.getUserId()));

				Bitmap profileImg = BitmapFactory.decodeByteArray(imageByteArr, 0, imageByteArr.length);
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
				Toast.makeText(PostsMapActivity.this, "Failed to get posts", Toast.LENGTH_LONG).show();
				zoomMapByLatLon(PostsMapActivity.this.latitude, PostsMapActivity.this.longitude);
				return;
			}

			if (result.isEmpty())
			{
				Toast.makeText(PostsMapActivity.this, "No posts by friends were found", Toast.LENGTH_LONG).show();
				zoomMapByLatLon(PostsMapActivity.this.latitude, PostsMapActivity.this.longitude);
				return;
			}

			for (HelpPost helpPost : result)
			{
				addOverlayToMap(helpPost.getLatitude(), helpPost.getLongitude(), helpPost.getCategory().getName(),
				        helpPost.getFreeText(), helpPost, userProfileImage.get(helpPost.getUserId()));
			}

			zoomMapByLatLon(PostsMapActivity.this.latitude, PostsMapActivity.this.longitude);
		}

		private void addLoggedInUserOverlay()
		{
			String firstName = getMainApplication().getData("firstName").toString();
			String lastName = getMainApplication().getData("lastName").toString();
			Bitmap profileImg = (Bitmap) getMainApplication().getData("profilePicture");
			addOverlayToMap(latitude, longitude, "You are here", firstName + " " + lastName, profileImg);
		}
	}

	private class PostHelpTask extends AsyncTask<Void, Void, Boolean>
	{
		private HelpPost helpPost;

		@Override
		protected void onPreExecute()
		{
			showLoadingProgressDialog();
			helpPost = buildHelpPost();
		}

		private HelpPost buildHelpPost()
		{
			HelpPost helpPost = new HelpPost();
			helpPost.setUserId(Long.parseLong(getFacebookId()));

			EditText helpTxt = (EditText) popupLayout.findViewById(R.id.helpTxt);
			helpPost.setFreeText(helpTxt.getText().toString());

			HelpCategory helpCategory = getHelpCategory();
			helpPost.setCategory(helpCategory);

			helpPost.setLatitude(latitude);
			helpPost.setLongitude(longitude);

			return helpPost;
		}

		private HelpCategory getHelpCategory()
		{
			RadioButton stuckWithCar = (RadioButton) popupLayout.findViewById(R.id.stuckWithCarRbtn);
			if (stuckWithCar.isChecked())
				return HelpCategory.stuck_with_car;

			RadioButton drunkCantDriveRbtn = (RadioButton) popupLayout.findViewById(R.id.drunkCantDriveRbtn);
			if (drunkCantDriveRbtn.isChecked())
				return HelpCategory.drunk_cant_drive;

			RadioButton needARideRbtn = (RadioButton) popupLayout.findViewById(R.id.needARideRbtn);
			if (needARideRbtn.isChecked())
				return HelpCategory.need_ride;

			return HelpCategory.stuck_with_car;
		}

		@Override
		protected Boolean doInBackground(Void... params)
		{
			// create Facebook post
			facebookApi.feedOperations().updateStatus(
			        helpPost.getUserId() + "-" + helpPost.getCategory().getName() 
			        + "-" + helpPost.getFreeText());

			// save post to DB
			saveHelpPostToDB();

			return true;
		}

		private void saveHelpPostToDB()
		{
			try
			{
				final String url = getMainApplication().getRestBaseUrl() + "/posthelp";

				HttpHeaders requestHeaders = new HttpHeaders();
				requestHeaders.setContentType(MediaType.APPLICATION_JSON);
				HttpEntity<HelpPost> requestEntity = new HttpEntity<HelpPost>(helpPost, requestHeaders);

				RestTemplate restTemplate = new RestTemplate();
				restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
				restTemplate.exchange(new URI(url), HttpMethod.POST, requestEntity, Boolean.class);
			}
			catch (Exception e)
			{
				Log.e(TAG, e.getMessage(), e);
			}
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			dismissProgressDialog();
			showResult(result);
		}
	}
}

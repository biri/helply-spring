package com.mta.sadna.activities;

import java.net.URI;

import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

import com.mta.sadna.R;
import com.mta.sadna.activities.map.HelplyMapActivity;
import com.mta.sadna.enums.HelpCategory;
import com.mta.sadna.model.NotificationEntity;

/**
 * The activity that will pop up when a post help 
 * notification has arrived
 *
 * message + category - notification message and category
 * userImg - the user who asked for help Facebook profile image
 * userFullName - the user who asked for help full name
 * userLatitude + userLongitude - the location of the user who asked for help
 * userFacebookId - the Facebook id of the user who asked for help
 */
public class NotificationActivity extends HelplyMapActivity
{
	private String message;
	private Bitmap userImg;
	private String postId;
	private String userFullName;	
	private double userLatitude;
	private double userLongitude;
	private HelpCategory category;
	private String userFacebookId;
	
	protected static final String TAG = NotificationActivity.class.getSimpleName();

	@Override
    protected void setContentView()
    {
		setContentView(R.layout.notification_layout);
    }
	
	@Override
	protected void init()
	{
		try
		{
			Log.i(TAG, "Entered Notification Activity init");
			bindBtnEvents();
			NotificationEntity intentData = (NotificationEntity)getIntent()
					.getExtras().get("data");
			Log.i(TAG, "NotificationEntity as string = " + intentData.toString());
			
			Log.i(TAG, "About to fill data from intent");
			message = intentData.getMessage();
			category = intentData.getCategory();
			postId = intentData.getPostId();
			userFacebookId = intentData.getFacebookId();
			userLatitude = intentData.getLatitude();
			userLongitude = intentData.getLongitude();
			userFullName = intentData.getFirstName() 
					+ " " + intentData.getLastName();
			
			Log.i(TAG, "the post id - " + postId);
		}
		catch(Exception e)
		{
			Log.e(TAG, "Failed to init notification activity - " + 
						e.getMessage());
		}
	}
	
	@Override
	protected void doOnLocationChanged(Location location)
	{
		Log.i(TAG, "About to execute GetFbInfoTask");
		new GetFbInfoTask().execute();
	}
	
	private void bindBtnEvents()
    {
		Log.i(TAG, "Binding events");
		Typeface font = Typeface.createFromAsset(getAssets(), "font/rooney.ttf");
		
		Button acceptBtn = (Button) findViewById(R.id.acceptBtn);
		acceptBtn.setTypeface(font);
		acceptBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				new SendAcceptNotificationTask().execute();
			}
		});
		
		Button ignoreBtn = (Button) findViewById(R.id.IgnoreBtn);
		ignoreBtn.setTypeface(font);
		ignoreBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				goBackToMainActivity();
			}
		});
    }

	private void fillFields()
    {
		Log.i(TAG, "About to fill the fields");
		
		TextView mainUserTxt = (TextView)findViewById(R.id.mainUserTxt);
		mainUserTxt.setText(userFullName + " Needs Your Help");
		
		TextView categoryTxt = (TextView)findViewById(R.id.categoryTxt);
		categoryTxt.setText(category.getName());
		
		TextView messageTxt = (TextView)findViewById(R.id.messageTxt);
		messageTxt.setText(message);
		
		float[] distanceResults = new float[10];
		Location.distanceBetween(userLatitude, userLongitude,
				latitude, longitude, distanceResults);
		if (distanceResults != null && distanceResults.length != 0)
		{
			String distance = Float.toString(distanceResults[0]);
			TextView distanceTxt = (TextView)findViewById(R.id.distanceTxt);
			distanceTxt.setText(distance + " Meters");			
		}
		
		Log.i(TAG, "about to fill the image field");

		QuickContactBadge userImage = (QuickContactBadge) findViewById(R.id.userImage);
		userImage.setImageBitmap(userImg);
		
		addOverlayToMap(userLatitude, userLongitude, 
				"HELP! " + category.getName(), message, userImg);
		
		zoomMapByLatLon(userLatitude, userLongitude);
    }
	
	private void goBackToMainActivity()
	{
		startActivity(new Intent(this, PostsMapActivity.class));
		finish();		
	}
	
	private class GetFbInfoTask extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected void onPreExecute()
		{
			showLoadingProgressDialog();
		}
		
		@Override
        protected Void doInBackground(Void... arg0)
        {
			Log.i(TAG, "Getting profile image from Facebook");
			byte[] b = facebookApi.userOperations().getUserProfileImage(userFacebookId);
			userImg =  BitmapFactory.decodeByteArray(b , 0, b .length);
			Log.i(TAG, "Got profile image from Facebook");
			return null;
        }
		
		@Override
		protected void onPostExecute(Void result)
		{
			dismissProgressDialog();
			fillFields();
		}
	}
	
	/**
	 * Sends the user who posted the an accept messsage
	 * FacebookId - the user who will receive the message facebook id
	 * FirstName + LastName - the user who is coming to help full name
	 * Location -  the user who is coming to help current location
	 */
	private class SendAcceptNotificationTask extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected void onPreExecute()
		{
			showLoadingProgressDialog();
		}
		
		@Override
        protected Boolean doInBackground(Void... arg0)
        {
			try
			{
				final String url = getMainApplication().getRestBaseUrl() 
						+ "/sendacceptnotification?" +
						"facebookId=" + userFacebookId + "&" +
						"firstName=" + getMainApplication().getData("firstName") + "&" +
						"lastName=" + getMainApplication().getData("lastName") + "&" +
						"postId=" + postId;

				RestTemplate restTemplate = new RestTemplate();
				restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
				return restTemplate.getForObject(new URI(url), Boolean.class);
			}
			catch (Exception e)
			{
				Log.e(TAG, e.getMessage(), e);
			}
			return false;
        }
		
		@Override
		protected void onPostExecute(Boolean result)
		{
			dismissProgressDialog();
			if (result)
			{
				Toast.makeText(NotificationActivity.this,
					"Accept message has been sent to " + userFullName,
					Toast.LENGTH_LONG).show();
			}
			else
			{
				Toast.makeText(NotificationActivity.this,
						"Failed to send accept " +
						"message has been sent to " + userFullName,
						Toast.LENGTH_LONG).show();				
			}
			goBackToMainActivity();
		}
	}
}

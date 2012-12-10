package com.mta.sadna.activities;

import java.net.URI;

import org.apache.http.Header;
import org.apache.http.StatusLine;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.web.client.RestTemplate;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.geoloqi.android.sdk.LQException;
import com.geoloqi.android.sdk.LQSession;
import com.geoloqi.android.sdk.LQSession.OnRunApiRequestListener;
import com.mta.sadna.AbstractAsyncActivity;
import com.mta.sadna.R;
import com.mta.sadna.enums.HelpCategory;
import com.mta.sadna.model.HelpPost;

public class PostHelpActivity extends AbstractAsyncActivity
{
	private double longitude;
	private double latitude;
	private Facebook facebookApi;
	private LQSession geoLoqiBinding;
	private LocationManager locationManager;
	private LocationListener locationListener;
	private String TAG = PostHelpActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_help_layout);
		getCurrentLocation();
		bindEvent();

		// get the Facebook api
		this.facebookApi =
		        getApplicationContext().getConnectionRepository()
		        .findPrimaryConnection(Facebook.class).getApi();

		// get geoloqi binding
		geoLoqiBinding = getApplicationContext().getGeoLoqiBinding();
	}

	private void bindEvent()
	{
		final Button submitBtn = (Button) findViewById(R.id.submitBtn);
		submitBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				new PostHelpTask().execute();
			}
		});
	}

	private void getCurrentLocation()
	{
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		getLastKnownLocation();
		locationListener = new LocationListener()
		{
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras)
			{
			}

			@Override
			public void onProviderEnabled(String provider)
			{
			}

			@Override
			public void onProviderDisabled(String provider)
			{
			}

			@Override
			public void onLocationChanged(Location location)
			{
				longitude = location.getLongitude();
				latitude = location.getLatitude();
			}
		};
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
		0, 0, locationListener);
	}

	private void getLastKnownLocation()
	{
		Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); 
		if (lastKnownLocation == null)
		{
			Log.i(TAG, "NETWORK_PROVIDER gave null");
			lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		}
		
		if (lastKnownLocation != null)
		{
			this.latitude = lastKnownLocation.getLatitude();
			this.longitude = lastKnownLocation.getLongitude();
			Log.i(TAG, "Got the location from last known");
		}
	}

	private void showResult(Boolean result)
	{
		if (result != null && result)
			Toast.makeText(this, "Posted Successfully", Toast.LENGTH_LONG).show();
		else
			Toast.makeText(this, "Posting failed", Toast.LENGTH_LONG).show();
	}

	private String getFacebookId()
	{
		return getApplicationContext().getData("facebookId").toString();
	}


	private class PostHelpTask extends AsyncTask<Void, Void, Boolean>
	{
		private String placeId;
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

			EditText helpTxt = (EditText) findViewById(R.id.helpTxt);
			helpPost.setFreeText(helpTxt.getText().toString());

			HelpCategory helpCategory = getHelpCategory();
			helpPost.setCategory(helpCategory);

			helpPost.setLatitude(latitude);
			helpPost.setLongitude(longitude);
			locationManager.removeUpdates(locationListener); // stop updates

			return helpPost;
		}

		private HelpCategory getHelpCategory()
		{
			RadioButton stuckWithCar = (RadioButton) findViewById(R.id.stuckWithCarRbtn);
			if (stuckWithCar.isChecked())
				return HelpCategory.stuck_with_car;

			RadioButton drunkCantDriveRbtn = (RadioButton) findViewById(R.id.drunkCantDriveRbtn);
			if (drunkCantDriveRbtn.isChecked())
				return HelpCategory.drunk_cant_drive;

			RadioButton needARideRbtn = (RadioButton) findViewById(R.id.needARideRbtn);
			if (needARideRbtn.isChecked())
				return HelpCategory.need_ride;

			return HelpCategory.stuck_with_car;
		}

		@Override
		protected Boolean doInBackground(Void... params)
		{
			// create facebook post
			facebookApi.feedOperations().updateStatus(
			        helpPost.getUserId() + "-" + helpPost.getCategory() 
			        + "-" + helpPost.getFreeText());

			// create geoloqi layer place and trigger
			buildGeoLoqiPlace();

			return true;
		}

		private void buildGeoLoqiPlace()
		{
			JSONObject placeJson = new JSONObject();
			try
			{
				placeJson.put("latitude", latitude);
				placeJson.put("longitude", longitude);
				placeJson.put("radius", 500);
				placeJson.put("description", getFacebookId());
				placeJson.put("name",helpPost.getFreeText() 
						+ "-------" + 
				        helpPost.getCategory());
			}
			catch (JSONException e)
			{
				Log.e(TAG, "Failed to create place json - " + e.getMessage());
				return;
			}

			geoLoqiBinding.runPostRequest("place/create", placeJson, new OnRunApiRequestListener()
			{
				@Override
				public void onComplete(LQSession arg0, JSONObject arg1, Header[] arg2, StatusLine arg3)
				{
					Log.i(TAG, "Successfully completed creating place");
				}

				@Override
				public void onFailure(LQSession arg0, LQException e)
				{
					Log.e(TAG, "Failed to create place - " + e.getMessage());
				}

				@Override
				public void onSuccess(LQSession arg0, JSONObject arg1, Header[] arg2)
				{
					try
                    {
						placeId = arg1.getString("place_id");
	                    Log.i(TAG, "place_id=" + placeId);
						buildGeoLoqiTrigger();
                    }
                    catch (JSONException e)
                    {
                    	Log.e(TAG, "Failed to get place id," +
                    			"trigger won't be created - " + e.getMessage());
                    }
				}
			});
		}

		private void buildGeoLoqiTrigger()
		{
			JSONObject triggerJson = new JSONObject();
			try
			{
				triggerJson.put("place_id", placeId);
				triggerJson.put("type", "callback");
				triggerJson.put("url", PostHelpActivity.this
						.getApplicationContext().getFromProperties("geoloqi_invoke_url"));
			}
			catch (JSONException e)
			{
				Log.e(TAG, "Failed to create trigger json - " + e.getMessage());
				return;
			}

			geoLoqiBinding.runPostRequest("trigger/create", triggerJson, new OnRunApiRequestListener()
			{
				@Override
				public void onComplete(LQSession arg0, JSONObject arg1, Header[] arg2, StatusLine arg3)
				{
					Log.i(TAG, "successfully created trigger");
				}

				@Override
				public void onFailure(LQSession arg0, LQException e)
				{
					Log.e(TAG, "Failed to create trigger - " + e.getMessage());
				}

				@Override
				public void onSuccess(LQSession arg0, final JSONObject arg1, Header[] arg2)
				{
					Thread trd = new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							try
		                    {
								String triggerId = arg1.getString("trigger_id");
			                    Log.i(TAG, "trigger_id=" + triggerId);
			        			saveHelpPostToDB(triggerId);
		                    }
		                    catch (JSONException e)
		                    {
		                    	Log.e(TAG, "Failed to get the " +
		                    			"trigger id - " + e.getMessage());
		                    }
						}
					});
					trd.start();
				}
			});
		}
		
		private void saveHelpPostToDB(String triggerId)
		{
			try
			{
				helpPost.setTriggerId(triggerId);
				final String url = PostHelpActivity.this.getApplicationContext()
						.getRestBaseUrl() + "/posthelp";

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

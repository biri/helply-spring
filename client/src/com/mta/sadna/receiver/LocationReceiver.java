package com.mta.sadna.receiver;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.geoloqi.android.sdk.LQTracker.LQTrackerProfile;
import com.geoloqi.android.sdk.receiver.LQBroadcastReceiver;
import com.mta.sadna.MainApplication;

public class LocationReceiver extends LQBroadcastReceiver
{
	protected static final String TAG = LocationReceiver.class.getSimpleName();
	
	@Override
    public void onLocationChanged(Context context, Location location)
    {
		Log.i(TAG, "OnLocation changed was called " +
				"latitude=" + location.getLatitude() 
				+ " longitude=" + location.getLongitude());
	
		//get main application from context
		MainApplication mainApplication = (MainApplication)context.
				getApplicationContext();
		
		//notify server of location change
		Thread thread = new Thread(new UpdateLocation(mainApplication, 
				location.getLatitude(), location.getLongitude()));
		thread.start();
    }
	
	public class UpdateLocation implements Runnable
	{
		private double latitude;
		private double longitude;
		private MainApplication mainApplication;

		public UpdateLocation(MainApplication mainApplication,
				double latitude, double longitude)
		{
			this.latitude = latitude;
			this.longitude = longitude;
			this.mainApplication = mainApplication;
		}
		
		@Override
        public void run()
        {
			try
			{
				Log.i(TAG, "entered thread run for updating location");
				
				String facebookId = mainApplication.getData("facebookId").toString();
				final String url = mainApplication.getRestBaseUrl() 
						+ "/updatelocation?latitude=" + latitude +
				                "&longitude=" + longitude + "&facebookId=" + facebookId;

				HttpHeaders requestHeaders = new HttpHeaders();
				List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
				acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
				requestHeaders.setAccept(acceptableMediaTypes);
				HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);

				RestTemplate restTemplate = new RestTemplate();
				restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
				restTemplate.exchange(new URI(url), HttpMethod.GET, requestEntity, Boolean.class);
			}
			catch (Exception e)
			{
				Log.e(TAG, "Failed to update location " +
						"- " + e.getMessage(), e);
			}
        }
	}

	@Override
    public void onLocationUploaded(Context arg0, int arg1)
    {
    }

	@Override
    public void onPushMessageReceived(Context arg0, Bundle arg1)
    {
    }

	@Override
    public void onTrackerProfileChanged(Context arg0, LQTrackerProfile arg1, LQTrackerProfile arg2)
    {
    }
}

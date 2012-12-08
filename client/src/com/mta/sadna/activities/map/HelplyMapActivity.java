package com.mta.sadna.activities.map;

import java.util.List;

import org.springframework.social.facebook.api.Facebook;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.mta.sadna.AsyncActivity;
import com.mta.sadna.MainApplication;
import com.mta.sadna.R;

public abstract class HelplyMapActivity extends MapActivity implements AsyncActivity
{
	protected double latitude;
	protected double longitude;
	protected Facebook facebookApi;
	protected LocationManager locationManager;
	protected LocationListener locationListener;
	
	protected MapView mapView;
	protected PostsOverlays postsOverlays;
	protected ProgressDialog progressDialog;
	
	protected static final String TAG = HelplyMapActivity.class.getSimpleName();
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.i(TAG, "Entered main map activity");
		setContentView();
		
		Log.i(TAG, "Facebook Api");
		this.facebookApi =
		        getMainApplication().getConnectionRepository()
		        .findPrimaryConnection(Facebook.class).getApi();

		Log.i(TAG, "Init Map");
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.getController().setZoom(15);

		Log.i(TAG, "Init overlays");
		List<Overlay> mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(R.drawable.spring_android);
		postsOverlays = new PostsOverlays(drawable, this);
		mapOverlays.add(postsOverlays);

		Log.i(TAG, "About to call child init");
		init();
		
		Log.i(TAG, "Getting current location");
		getCurrentLocation();
	}
	
	protected MainApplication getMainApplication()
	{
		return (MainApplication)super.getApplicationContext();
	}
	
	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}

	@Override
	public void showLoadingProgressDialog()
	{
		this.showProgressDialog("Loading. Please wait...");
	}

	@Override
	public void showProgressDialog(CharSequence message)
	{
		if (this.progressDialog == null)
		{
			this.progressDialog = new ProgressDialog(this);
			this.progressDialog.setIndeterminate(true);
		}

		this.progressDialog.setMessage(message);
		this.progressDialog.show();
	}

	@Override
	public void dismissProgressDialog()
	{
		if (this.progressDialog != null)
		{
			this.progressDialog.dismiss();
		}
	}
	
	protected int getMicrodegrees(double value)
	{
		double microDegree = value * 1000000;
		return (int)microDegree;
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
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
	}
	
	private void getLastKnownLocation()
	{
		Log.i(TAG, "About to get last known location");
		
		Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		this.latitude = lastKnownLocation.getLatitude();
		this.longitude = lastKnownLocation.getLongitude();
		
		Log.i(TAG, "Got last known location");
		
		zoomMapByLatLon(this.latitude, this.longitude);
		doOnLocationChanged(lastKnownLocation);
	}

	protected void addOverlayToMap(double latitude, double longitude,
			String subject, String text, Bitmap pictute)
	{
		GeoPoint point = new GeoPoint(getMicrodegrees(latitude), getMicrodegrees(longitude));
		OverlayItem overlayitem = new OverlayItem(point,subject, text);
		postsOverlays.addOverlay(overlayitem, new BitmapDrawable(getResources(), pictute));		
	}
	
	@Override
	protected void onDestroy()
	{
	    super.onDestroy();
	    locationManager.removeUpdates(locationListener);
	}
	
	protected abstract void setContentView();
	
	protected void init()
	{
		return;
	}
	
	protected void doOnLocationChanged(Location location)
    {
        return;
    }
	
	protected void zoomMapByLatLon(double latitude, double longitude)
    {
	    mapView.getController().setCenter(new GeoPoint(getMicrodegrees(latitude), 
				getMicrodegrees(longitude)));
    }
}

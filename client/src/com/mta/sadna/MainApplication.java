package com.mta.sadna;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.security.crypto.encrypt.AndroidEncryptors;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.sqlite.SQLiteConnectionRepository;
import org.springframework.social.connect.sqlite.support.SQLiteConnectionRepositoryHelper;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.geoloqi.android.sdk.LQSession;
import com.geoloqi.android.sdk.service.LQService;
import com.geoloqi.android.sdk.service.LQService.LQBinder;

public class MainApplication extends Application
{
	private LQSession geoLoqiSession;
	private ServiceConnection geoLoqiConnection;
	private final AtomicInteger bindingCount = new AtomicInteger();
	
	private Properties properties;
	private SQLiteOpenHelper repositoryHelper;
	private ConnectionRepository connectionRepository;
	private ConnectionFactoryRegistry connectionFactoryRegistry;
	private Map<String, Object> data = new HashMap<String, Object>();
	
	protected static final String TAG = MainApplication.class.getSimpleName();

	@Override
	public void onCreate()
	{
		createProperties();
		createFacebookConnectionFactory();
		createConnectionRepository();
		startGeoLoqiService();
		bindGeoLoqiService();
	}

	private void createProperties()
    {
	    try
        {
	        InputStream propertiesStream = getAssets().open("helply.properties");
	        properties = new Properties();
	        properties.load(propertiesStream);

	        Log.i(TAG, "Read properties successfully");
        }
        catch (Exception e)
        {
	        Log.e(TAG, "Failed to read properties from assets");
        }
    }

	private void createFacebookConnectionFactory()
    {
	    this.connectionFactoryRegistry = new ConnectionFactoryRegistry();
		this.connectionFactoryRegistry.addConnectionFactory(new FacebookConnectionFactory(getFacebookAppId(),
		        getFacebookAppSecret()));
    }
	
	private void createConnectionRepository()
    {
	    this.repositoryHelper = new SQLiteConnectionRepositoryHelper(this);
		this.connectionRepository =
		        new SQLiteConnectionRepository(this.repositoryHelper, this.connectionFactoryRegistry,
		                AndroidEncryptors.text("password", 
		                		getFromProperties("connection_factory_pass")));
    }	
	
	private void startGeoLoqiService()
    {
	    Intent intent = new Intent(this, LQService.class);
		startService(intent);
    }
	
	private void bindGeoLoqiService()
    {
		geoLoqiConnection = new ServiceConnection()
		{
			@Override
			public void onServiceConnected(ComponentName name, IBinder service)
			{
				try
				{
					final LQBinder binder = (LQBinder) service;
					final Handler handler = new Handler();
					
					Log.i(TAG, "OnService connected");
					Runnable runnable = new Runnable()
					{
						@Override
						public void run()
						{
							geoLoqiSession = binder.getService().getSession();
							if (geoLoqiSession == null)
							{
								handler.postDelayed(this, 1000);
							}
							else
							{
								geoLoqiSession.setAccessToken(getFromProperties("geoloqi_access_token"));
								Log.i(TAG, "Got the geoloqi session - " +
										geoLoqiSession.getAccessToken());
							}
						}
					};
					runnable.run();
				}
				catch (ClassCastException e)
				{
					Log.e(TAG, "onServiceConnected error - " + e.getMessage());
				}
			}

			@Override
			public void onServiceDisconnected(ComponentName name)
			{
			}
		};

		// bind geoloqi service
		Intent intent = new Intent(this, LQService.class);
		bindService(intent, geoLoqiConnection, 0);
    }
	
	private String getFacebookAppId()
	{
		return getFromProperties("facebook_app_id");
	}

	private String getFacebookAppSecret()
	{
		return getFromProperties("facebook_app_secret");
	}
	
	public String getRestBaseUrl()
	{
		return getFromProperties("rest_server_url");
	}
	
	public String getProjectId()
	{
		return getFromProperties("google_project_id");
	}

	public ConnectionRepository getConnectionRepository()
	{
		return this.connectionRepository;
	}

	public FacebookConnectionFactory getFacebookConnectionFactory()
	{
		return (FacebookConnectionFactory) this.connectionFactoryRegistry.getConnectionFactory(Facebook.class);
	}
	
	/**
	 * When we get a binding we count
	 * how many activities are holding it
	 * @return
	 */
	public LQSession getGeoLoqiBinding()
	{
		bindingCount.incrementAndGet();
		return geoLoqiSession;
	}
	
	/**
	 * When the last activity released
	 * we relase the binding
	 */
	public void releaseGeoLoqiBinding()
	{
		bindingCount.decrementAndGet();
		if (bindingCount.get() == 0)
			unbindService(geoLoqiConnection);
	}
	
	public void addData(String key, Object value)
	{
		this.data.put(key, value);
	}
	
	public Object getData(String key)
	{
		return this.data.get(key);
	}

	public void clearData()
    {
		this.data.clear();
    }
	
	public String getFromProperties(String key)
	{
		return properties.getProperty(key);
	}
}

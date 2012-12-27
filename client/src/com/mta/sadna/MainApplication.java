package com.mta.sadna;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.security.crypto.encrypt.AndroidEncryptors;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.sqlite.SQLiteConnectionRepository;
import org.springframework.social.connect.sqlite.support.SQLiteConnectionRepositoryHelper;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;

import android.app.Application;
import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.geoloqi.android.sdk.service.LQService;

public class MainApplication extends Application
{
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

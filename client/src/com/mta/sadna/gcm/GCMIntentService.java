package com.mta.sadna.gcm;

import org.springframework.social.facebook.api.Facebook;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.mta.sadna.MainApplication;
import com.mta.sadna.enums.GcmAction;

/**
 * This service will send the registration id to the server to register the
 * current device.
 * 
 * @author tprizler
 * 
 */
public class GCMIntentService extends GCMBaseIntentService
{	
	private static final String TAG = GCMIntentService.class.getSimpleName();

	public GCMIntentService()
	{
		super();
		Log.i(TAG, TAG + " init");
	}
	
	private MainApplication getMainApplicationContext()
	{
		return (MainApplication)super.getApplicationContext();
	}

	@Override
	protected void onError(Context ctx, String sError)
	{
		Log.i(TAG, "Error: " + sError);
	}

	@Override
	protected void onMessage(Context ctx, Intent intent)
	{
		Log.i(TAG, "GCM Message Received");
		String type = intent.getStringExtra("type");
		String message = intent.getStringExtra("message");
		sendGCMIntent(ctx, type, message);
	}

	private void sendGCMIntent(Context ctx, String type, String message)
	{
		if (!isConnectedToFacebook())
		{
			Log.i(TAG, "Not connected to Facebook " +
					" message will be ignored");
			return;
		}
		
		if (type.equals("post"))
			sendBroadcast(ctx, GcmAction.POST_NOTIFICATION, message);
		else if (type.equals("accept"))
			sendBroadcast(ctx, GcmAction.ACCEPT_NOTIFICATION, message);
	}

	private void sendBroadcast(Context ctx, GcmAction action, String message)
    {
	    Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(action.toString());
		broadcastIntent.putExtra("gcm", message);
		ctx.sendBroadcast(broadcastIntent);
    }
	
	private boolean isConnectedToFacebook()
	{
		MainApplication mainApplication =  (MainApplication)super.getApplicationContext();
		return mainApplication.getConnectionRepository()
				.findPrimaryConnection(Facebook.class) != null;
	}

	/**
	 * Will send the regId to the server
	 * 
	 * @param regId the registration id
	 */
	@Override
	protected void onRegistered(Context ctx, String gcmRegId)
	{
		try
		{
			Log.i(TAG, "onRegistered called " + gcmRegId);
			getMainApplicationContext().addData("gcmRegId", gcmRegId);
		}
		catch (Exception e)
		{
			Log.e(TAG, "Failed to send registration id to the server", e);
		}
	}
	
	@Override
	protected void onUnregistered(Context ctx, String regId)
	{
		// send notification to your server to remove that regId
	}
}

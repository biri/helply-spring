package com.mta.sadna.receiver;

import com.google.gson.Gson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public abstract class AbstractReceiver extends BroadcastReceiver
{
	protected Gson gson = new Gson();
	protected static final String TAG = AbstractReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		String gcmMessage = intent.getExtras().getString("gcm");
		if (gcmMessage == null || gcmMessage.equals(""))
		{
			Log.e(TAG, "GCM message is null or empty");
			return;
		}
		doOnGCMReceiveEvent(context, gcmMessage);
	}

	protected abstract void doOnGCMReceiveEvent(Context context, String gcmMessage);
}

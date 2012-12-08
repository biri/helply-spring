package com.mta.sadna.receiver;

import android.content.Context;

import com.google.android.gcm.GCMBroadcastReceiver;

/**
 * This class extends the GCMBroadcastReceiver in order to set the
 * GCMIntentService name to a costume one.
 * 
 * @author tprizler
 * 
 */
public class GCMReceiver extends GCMBroadcastReceiver
{

	@Override
	protected String getGCMIntentServiceClassName(Context context)
	{
		return "com.mta.sadna.gcm.GCMIntentService";
	}
}

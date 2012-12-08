package com.mta.sadna.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.util.Log;

import com.mta.sadna.R;
import com.mta.sadna.activities.NotificationActivity;
import com.mta.sadna.model.NotificationEntity;

public class PostsReceiver extends AbstractReceiver
{
	protected static final String TAG = PostsReceiver.class.getSimpleName();
	
	@Override
	protected void doOnGCMReceiveEvent(Context context, String gcmMessage)
    {
		Log.i(TAG, "GCM post message = " + gcmMessage);
		NotificationEntity notificationJson = 
				gson.fromJson(gcmMessage, NotificationEntity.class);
		
		Intent intent = new Intent(context, NotificationActivity.class);
		intent.putExtra("data", notificationJson);
		
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
		Notification noti = new Notification.Builder(context)
		        .setContentTitle(buildTitle(notificationJson))
		        .setContentText(notificationJson.getMessage())
		        .setSmallIcon(R.drawable.spring_android)
		        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
		        .setContentIntent(pIntent).getNotification();
		  
		NotificationManager notificationManager = 
		  (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		noti.defaults |= Notification.DEFAULT_SOUND ;
		noti.defaults |= Notification.DEFAULT_LIGHTS;
		notificationManager.notify(0, noti); 
    }
	
	private String buildTitle(NotificationEntity json)
	{
		String fullName = json.getFirstName() 
				+ " " + json.getLastName();
		return fullName + " needs your help!";
	}
}

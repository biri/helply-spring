package com.mta.sadna.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import com.mta.sadna.R;
import com.mta.sadna.activities.MainActivity;
import com.mta.sadna.model.NotificationEntity;

public class AcceptReceiver extends AbstractReceiver
{
	protected static final String TAG = AcceptReceiver.class.getSimpleName();
	
	@Override
    protected void doOnGCMReceiveEvent(Context context, String gcmMessage)
    {
		Log.i(TAG, "GCM accept message = " + gcmMessage);
		NotificationEntity notificationJson = 
				gson.fromJson(gcmMessage, NotificationEntity.class);
		
		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
		Builder notiBuilder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.spring_android)
				.setContentTitle("Help is on the way!")
		        .setContentText(buildNotificationText(notificationJson))
		        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
		        .setContentIntent(pIntent);
		
		NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
		inboxStyle.setBigContentTitle("Help is on the way!");
		inboxStyle.setSummaryText(buildNotificationText(notificationJson));
		notiBuilder.setStyle(inboxStyle);
		  
		Notification noti = notiBuilder.build();
		NotificationManager notificationManager = 
		  (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		noti.defaults |= Notification.DEFAULT_SOUND ;
		noti.defaults |= Notification.DEFAULT_LIGHTS;
		notificationManager.notify(0, noti); 
    }
	
	private String buildNotificationText(NotificationEntity json)
	{
		String fullName = json.getFirstName() 
				+ " " + json.getLastName();
		return fullName + " is coming to help you!";
	}
}

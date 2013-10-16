package edu.neu.zhengxingchen.madcourse.communication;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import edu.neu.mhealth.api.KeyValueAPI;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class OfflineSyncService extends WakefulIntentService{
	public static NotificationManager mNotificationManager;
	
	
	public OfflineSyncService() {
		super("CheckMoveService");
	}

	@Override
	protected void doWakefulWork(Intent intent) {
		
		Log.d("waitroom", "doWakefulWork" );
		//Log.d("waitroom", intent.getStringExtra("player")==null? "null" : intent.getStringExtra("player"));
		//updateGuysList(GetGuysTask.LOOK_FOR_GUY);
	//	String serial = intent.getStringExtra("player");
		
		String getResult = null;
		if(KeyValueAPI.isServerAvailable()) {
			getResult = KeyValueAPI.get(Global.USER_NAME, Global.PASSWORD, Global.SERIAL);
			showNotification();
		}
	}

	@Override
	public void onDestroy() {
//		Log.d("waitroom", "onDestroy" );
		super.onDestroy();
	}

	private void showNotification() {
		Intent i = new Intent();
		i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		i.setClass(this, WaitRoom.class);
		i.putExtra("continue", true);
		//
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setContentIntent(contentIntent).setContentTitle("Dabble")
				.setContentText("New guy found").setSmallIcon(R.drawable.noti);
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(R.string.new_guy_found, mBuilder.build());
	}
}

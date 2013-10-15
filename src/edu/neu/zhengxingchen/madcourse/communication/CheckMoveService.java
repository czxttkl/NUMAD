package edu.neu.zhengxingchen.madcourse.communication;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class CheckMoveService extends WakefulIntentService{
	public CheckMoveService() {
		super("CheckMoveService");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.d("waitroom","onstartcommand");
		Log.d("waitroom", intent.getStringExtra("player")==null? "null" : "not null");
		return super.onStartCommand(intent, flags, startId);
//		return START_REDELIVER_INTENT;
	}

	@Override
	protected void doWakefulWork(Intent intent) {
		
		Log.d("waitroom", "doWakefulWork" );
		Log.d("waitroom", intent.getStringExtra("player")==null? "null" : intent.getStringExtra("player"));
		//updateGuysList(GetGuysTask.LOOK_FOR_GUY);
	}

	private void showNotification() {
		Intent i = new Intent();
		i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		i.setClass(this, WaitRoom.class);
		//
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setContentIntent(contentIntent).setContentTitle("Dabble")
				.setContentText("New guy found").setSmallIcon(R.drawable.noti);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(R.string.new_guy_found, mBuilder.build());

	}
}

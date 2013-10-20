package edu.neu.zhengxingchen.madcourse.dabble.twoplayer;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import edu.neu.mhealth.api.KeyValueAPI;
import edu.neu.zhengxingchen.madcourse.dabble.DabbleWaitRoom;
import edu.neu.zhengxingchen.madcourse.dabble.R;
import edu.neu.zhengxingchen.madcourse.dabble.helper.Global;

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
		
		
		String getResult = null;
		if(KeyValueAPI.isServerAvailable()) {
			getResult = KeyValueAPI.get(Global.USER_NAME, Global.PASSWORD, Global.SERIAL);
			Log.d("dabblewaitroom", "offline service:" + getResult + " guyslist:" + KeyValueAPI.get(Global.USER_NAME, Global.PASSWORD, Global.SERVER_KEY_GUY_LIST));
			//showNotification();
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
		i.setClass(this, DabbleWaitRoom.class);
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

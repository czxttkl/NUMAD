package edu.neu.zhengxingchen.madcourse.communication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class OnlineBroadcastReceiver extends BroadcastReceiver {
	private static final int PERIOD = 5000; // 15 minutes
	private static final int INITIAL_DELAY = 0; // 5 seconds
//	private static WaitRoom wr= null;
	private static OnlineResultReceiver mReceiver;
	
	
	@Override
	public void onReceive(Context ctxt, Intent i) {
		if (i.getAction() == null) {
			Intent a = new Intent(ctxt, OnlineSyncService.class);
			a.putExtra("receiver", mReceiver);
			WakefulIntentService.sendWakefulWork(ctxt, a);
		} else {
			scheduleAlarms(ctxt, mReceiver);
		}
	}
	
	static void scheduleAlarms(Context ctxt, OnlineResultReceiver receiver) {
		mReceiver = receiver;
		AlarmManager mgr = (AlarmManager) ctxt
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(ctxt, OnlineBroadcastReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(ctxt, 0, i, 0);

		mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + INITIAL_DELAY, PERIOD, pi);

	}
	
	static void cancelAlarms(Context ctxt) {
		AlarmManager mgr = (AlarmManager) ctxt
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(ctxt, OnlineBroadcastReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(ctxt, 0, i, 0);
		mgr.cancel(pi);
	}
}

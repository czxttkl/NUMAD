/***
  Copyright (c) 2012 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
  
  From _The Busy Coder's Guide to Android Development_
    http://commonsware.com/Android
 */

package edu.neu.zhengxingchen.madcourse.communication;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class OfflineBroadcastReceiver extends BroadcastReceiver {
	private static final int PERIOD = 5000; // 15 minutes
	private static final int INITIAL_DELAY = 0; // 5 seconds
	
	
	@Override
	public void onReceive(Context ctxt, Intent i) {
		if (i.getAction() == null) {
//			Log.d("waitroom", "onreceive");
			Intent a = new Intent(ctxt, OfflineSyncService.class);
//			a.setComponent(new ComponentName("edu.neu.zhengxingchen.madcourse.communication",
//					"CheckMoveService"));
			WakefulIntentService.sendWakefulWork(ctxt, a);
		} else {
			scheduleAlarms(ctxt);
		}
	}
	
	static void scheduleAlarms(Context ctxt) {

		AlarmManager mgr = (AlarmManager) ctxt
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(ctxt, OfflineBroadcastReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(ctxt, 0, i, 0);

		mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + INITIAL_DELAY, PERIOD, pi);

	}
	
	static void cancelAlarms(Context ctxt) {
		AlarmManager mgr = (AlarmManager) ctxt
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(ctxt, OfflineBroadcastReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(ctxt, 0, i, 0);
		mgr.cancel(pi);
		if(OfflineSyncService.mNotificationManager!=null)
			OfflineSyncService.mNotificationManager.cancelAll();
	}
}

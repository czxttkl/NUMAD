package edu.neu.zhengxingchen.madcourse.communication;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import android.content.Intent;
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
		Log.d("waitroom", intent.getStringExtra("player")==null? "null" : "not null");
		//updateGuysList(GetGuysTask.LOOK_FOR_GUY);
	}

}

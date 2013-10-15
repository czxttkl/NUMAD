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
		Log.d("waitroom",intent.getStringExtra("player"));
		return START_STICKY;
	}

	@Override
	protected void doWakefulWork(Intent intent) {
		Log.d("waitroom", "doWakefulWork");
		//updateGuysList(GetGuysTask.LOOK_FOR_GUY);
	}

}

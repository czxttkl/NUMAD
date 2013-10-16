package edu.neu.zhengxingchen.madcourse.communication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Messenger;
import android.os.ResultReceiver;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import edu.neu.mhealth.api.KeyValueAPI;
import edu.neu.zhengxingchen.madcourse.communication.WaitRoomService.IncomingHandler;

//final Messenger mMessenger = new Messenger(new IncomingHandler());
public class OnlineSyncService extends WakefulIntentService{
	public OnlineSyncService() {
		super("CheckEverythingService");
	}
	@Override
	protected void doWakefulWork(Intent intent) {
		
	    ResultReceiver rec = intent.getParcelableExtra("receiver");
		String getResult = null;
		if(KeyValueAPI.isServerAvailable()) {
			getResult = KeyValueAPI.get(Global.USER_NAME, Global.PASSWORD, Global.SERIAL);
			Bundle b = new Bundle();
			b.putString("status", getResult);
			rec.send(0, b);			
		}
		
		
	}

	
	

}

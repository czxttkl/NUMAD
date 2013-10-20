package edu.neu.zhengxingchen.madcourse.communication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Messenger;
import android.os.ResultReceiver;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import edu.neu.mhealth.api.KeyValueAPI;

//final Messenger mMessenger = new Messenger(new IncomingHandler());
public class OnlineSyncService extends WakefulIntentService{
	public OnlineSyncService() {
		super("CheckEverythingService");
	}
	@Override
	protected void doWakefulWork(Intent intent) {
//		Log.d("waitroom", "wakefulwork");
	    ResultReceiver rec = intent.getParcelableExtra("receiver");
		String getResult = null;
		Bundle b = new Bundle();
		if(KeyValueAPI.isServerAvailable() && Global.SERIAL!=null) {
			getResult = KeyValueAPI.get(Global.USER_NAME, Global.PASSWORD, Global.SERIAL);
			b.putString("status", getResult);
			rec.send(0, b);			
		}
		else {
			b.putString("status", "Error:Network");
			rec.send(0, b);
		}
		
		
	}

	
	

}

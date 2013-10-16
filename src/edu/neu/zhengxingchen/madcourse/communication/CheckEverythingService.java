package edu.neu.zhengxingchen.madcourse.communication;

import android.content.Intent;
import android.os.Messenger;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import edu.neu.mhealth.api.KeyValueAPI;
import edu.neu.zhengxingchen.madcourse.communication.WaitRoomService.IncomingHandler;

//final Messenger mMessenger = new Messenger(new IncomingHandler());
public class CheckEverythingService extends WakefulIntentService{
	public CheckEverythingService() {
		super("CheckEverythingService");
	}
	@Override
	protected void doWakefulWork(Intent intent) {
		String getResult = null;
		if(KeyValueAPI.isServerAvailable()) {
			getResult = KeyValueAPI.get(Global.USER_NAME, Global.PASSWORD, Global.SERIAL);
			Log.d("waitroom", getResult );
		}
		
		
	}

	
	

}

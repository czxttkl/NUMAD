package edu.neu.zhengxingchen.madcourse.communication;

import java.util.Date;

import edu.neu.mhealth.api.KeyValueAPI;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

public class ConnectGuyTask extends AsyncTask<String, Integer, String>{
	public static String usr = "czxttkl";
	public static String pwd = "cZxttkl,1";
	public WaitRoomService wr;
	
	public ConnectGuyTask(WaitRoomService wr) {
		this.wr = wr;
	}

	@Override
	protected String doInBackground(String... arg0) {
		String playerSerial = arg0[0];
		String putResult = "Error";
		if(KeyValueAPI.isServerAvailable()) {
			long now = Global.NTP_REFERENCE + SystemClock.elapsedRealtime();
			putResult = KeyValueAPI.put(usr, pwd, playerSerial, now + ":" + Global.SERVER_STATUS_INVITED + ":"  + Global.SERIAL);
//			Log.d("waitroom", "connectguy: key:" + playerSerial+ " value:" + now +":" + Global.SERVER_STATUS_INVITED + ":" + wr.mClientSerial);
		} 
		return putResult;
	}
	
	@Override
	protected void onPostExecute(String result) {
		if(result.equals("true"))
			wr.afterConnectGuyTask(result);
		else
			wr.returnError();
	}
}

package edu.neu.zhengxingchen.madcourse.dabble.twoplayer;

import java.util.Date;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import edu.neu.mhealth.api.KeyValueAPI;
import edu.neu.zhengxingchen.madcourse.dabble.DabbleWaitRoom;
import edu.neu.zhengxingchen.madcourse.dabble.helper.Global;

public class AddGuyTask extends AsyncTask<String, Integer, String> {

	public static String usr = "czxttkl";
	public static String pwd = "cZxttkl,1";
	public DabbleWaitRoom wr;
	public boolean needed = true;

	public AddGuyTask(DabbleWaitRoom wr) {
		this.wr = wr;
	}

	@Override
	protected String doInBackground(String... arg0) {
		String list = wr.list;
		String[] guys = list.split(":");
		
		
		for (String guy : guys) {
			if (guy.equals(Global.SERIAL))
				needed = false;
		}
		
		String putResult;
		if(needed)
			putResult= "Error";
		else
			putResult = "noneed";
		
		if (needed) {
			String value = list + ":" + Global.SERIAL;
			
			if (KeyValueAPI.isServerAvailable()) {
				putResult = KeyValueAPI.put(usr, pwd,
						Global.SERVER_KEY_GUY_LIST, value);
				SntpClient sn = new SntpClient();
				long now = 0;
				if (sn.requestTime("pool.ntp.org", 5000)) {
					now = sn.getNtpTime() + SystemClock.elapsedRealtime()
							- sn.getNtpTimeReference();
					putResult = KeyValueAPI.put(usr, pwd, Global.SERIAL, now
							+ ":" + Global.SERVER_STATUS_WAIT);
				}
				
				Log.d("addguytask", KeyValueAPI.get(usr, pwd, Global.SERVER_KEY_GUY_LIST)); 
			}
		} else {

		}

		return putResult;
	}

	@Override
	protected void onPostExecute(String result) {
		if (result.equals("true"))
			wr.afterAddGuyTask();
		else if (result.equals("noneed")) {
			
		} else {
			//exception
			wr.returnError();
		}
			
	}

}

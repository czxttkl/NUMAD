package edu.neu.zhengxingchen.madcourse.communication;

import edu.neu.mhealth.api.KeyValueAPI;
import android.os.AsyncTask;
import android.os.SystemClock;

public class PutValueTask extends AsyncTask<String, Integer, String> {

	public static String usr = "czxttkl";
	public static String pwd = "cZxttkl,1";
	public static int PUT_VALUE = 0;
	public static int SET_CONNECTED = 2;

	public WaitRoom wr;
	public int code;

	public PutValueTask(WaitRoom wr, int code) {
		this.wr = wr;
		this.code = code;
	}

	@Override
	protected String doInBackground(String... arg0) {
		String putResult = "Error";
		if (code == PUT_VALUE) {
			String key = arg0[0];
			String value = arg0[1];
			long time = System.currentTimeMillis();
			if (KeyValueAPI.isServerAvailable()) {
				putResult = KeyValueAPI.put(usr, pwd, key, value);
			}
			time = System.currentTimeMillis() - time;
		}
		
		if (code == SET_CONNECTED) {
			String rival = arg0[0];
			if (KeyValueAPI.isServerAvailable()) {
				long now = Global.NTP_REFERENCE + SystemClock.elapsedRealtime();
				putResult = KeyValueAPI.put(usr, pwd, rival, now+":" + Global.SERVER_STATUS_INGAME + ":" + Global.SERIAL);
			}
		}
		
		return putResult;
	}

	@Override
	protected void onPostExecute(String result) {
		if (result.equals("true")) {
			if (code == PUT_VALUE) {
				wr.afterPutValue(result);
			}
//			if (code == PURGE_INVITED) {
//				wr.startInvitePopup();
//			}
		} else {
			
		}
	}

}

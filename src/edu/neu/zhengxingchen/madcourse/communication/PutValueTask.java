package edu.neu.zhengxingchen.madcourse.communication;

import edu.neu.mhealth.api.KeyValueAPI;
import android.os.AsyncTask;
import android.os.SystemClock;

public class PutValueTask extends AsyncTask<String, Integer, String> {

	public static String usr = "czxttkl";
	public static String pwd = "cZxttkl,1";
	public static int PUT_VALUE = 0;
	public static int PURGE_INVITED = 1;

	public WaitRoom wr;
	public int code;

	public PutValueTask(WaitRoom wr, int code) {
		this.wr = wr;
		this.code = code;
	}

	@Override
	protected String doInBackground(String... arg0) {
		String putResult = "Error";
		if (code == 0) {
			String key = arg0[0];
			String value = arg0[1];
			long time = System.currentTimeMillis();
			if (KeyValueAPI.isServerAvailable()) {
				putResult = KeyValueAPI.put(usr, pwd, key, value);
			}
			time = System.currentTimeMillis() - time;
			return "time:" + String.valueOf(time) + " result:" + putResult;
		}
		if (code == 1) {
			if (KeyValueAPI.isServerAvailable()) {
				long now = Global.NTP_REFERENCE + SystemClock.elapsedRealtime();
				putResult = KeyValueAPI.put(usr, pwd, Global.SERIAL, now+":" + Global.SERVER_STATUS_WAIT);
			}
			return putResult;
		}
		
		return putResult;
	}

	@Override
	protected void onPostExecute(String result) {
		if (result.equals("true")) {
			if (code == 0) {
				wr.afterPutValue(result);
			}
			if (code == 1) {
				wr.startInvitePopup();
			}
		} else {
			
		}
	}

}

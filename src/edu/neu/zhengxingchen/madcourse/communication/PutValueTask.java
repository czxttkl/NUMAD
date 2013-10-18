package edu.neu.zhengxingchen.madcourse.communication;

import edu.neu.mhealth.api.KeyValueAPI;
import android.os.AsyncTask;
import android.os.SystemClock;

public class PutValueTask extends AsyncTask<String, Integer, String> {

	public static String usr = "czxttkl";
	public static String pwd = "cZxttkl,1";
	public static int PUT_VALUE = 0;
	public static int SET_WAIT = 1;
	public static int SET_CONNECTED = 2;
	public static int SET_UNSHAKE = 3;
	public static int SET_INVITE = 4;
	public static int SET_ADD_MYSELF = 5;
	public static int SET_MOVE = 6;
	public static int SET_UPDATE_CONNECTED = 7;
	public static int SET_REWAIT = 8;

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
			if (KeyValueAPI.isServerAvailable()) {
				putResult = KeyValueAPI.put(usr, pwd, key, value);
			}
		}

		if (code == SET_CONNECTED || code == SET_UPDATE_CONNECTED) {
			String rival = arg0[0];
			if (KeyValueAPI.isServerAvailable()) {
				long now = Global.NTP_REFERENCE + SystemClock.elapsedRealtime();
				putResult = KeyValueAPI.put(usr, pwd, rival, now + ":"
						+ Global.SERVER_STATUS_INGAME + ":" + Global.SERIAL);
			}
		}

		if (code == SET_UNSHAKE) {
			String rival = arg0[0];
			if (KeyValueAPI.isServerAvailable()) {
				long now = Global.NTP_REFERENCE + SystemClock.elapsedRealtime();
				putResult = KeyValueAPI.put(usr, pwd, rival, now + ":"
						+ Global.SERVER_STATUS_WAIT);
				if (!putResult.startsWith("Error")) {
					putResult = KeyValueAPI.put(usr, pwd, Global.SERIAL, now
							+ ":" + Global.SERVER_STATUS_WAIT);
				}
			}
		}

		if (code == SET_WAIT || code == SET_REWAIT) {
			if (KeyValueAPI.isServerAvailable()) {
				long now = Global.NTP_REFERENCE + SystemClock.elapsedRealtime();
				putResult = KeyValueAPI.put(usr, pwd, Global.SERIAL, now + ":"
						+ Global.SERVER_STATUS_WAIT);
			}
		}
		
		if (code == SET_MOVE) {
			String rival = arg0[0];
			if (KeyValueAPI.isServerAvailable()) {
				long now = Global.NTP_REFERENCE + SystemClock.elapsedRealtime();
				putResult = KeyValueAPI.put(usr, pwd, rival, now + ":"
						+ Global.SERVER_STATUS_INGAME + ":" + Global.SERIAL + ":" + "Move");
			}
		}
		
		if (code == SET_ADD_MYSELF) {
			boolean needed = true;
			String[] guys = wr.list.split(":");
			for (String guy : guys) {
				if (guy.equals(Global.SERIAL))
					needed = false;
			}

			if (!needed)
				putResult = "true";

			if (needed) {
				String value = wr.list + ":" + Global.SERIAL;
				if (KeyValueAPI.isServerAvailable()) {
					putResult = KeyValueAPI.put(usr, pwd, Global.SERVER_KEY_GUY_LIST, value);
					SntpClient sn = new SntpClient();
					long now = 0;
					if (sn.requestTime("pool.ntp.org", 5000)) {
						now = sn.getNtpTime() + SystemClock.elapsedRealtime() - sn.getNtpTimeReference();
						if (!putResult.startsWith("Error")) {
							putResult = KeyValueAPI.put(usr, pwd, Global.SERIAL, now + ":" + Global.SERVER_STATUS_WAIT);
						}
					}
				}
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

			if (code == SET_WAIT) {
//				wr.invitepopuped = false;
				// wr.startInvitePopup();
			}

			if (code == SET_REWAIT) {
				wr.afterSetRewait();
			}
			
			if (code == SET_CONNECTED) {
				
				wr.afterSetConnected();//wr.invitepopuped = false;
			}

			if (code == SET_UNSHAKE) {
				wr.afterUnshakeTask();
			}
			
			if (code == SET_ADD_MYSELF) {
				wr.afterAddGuyTask();
			}

			if (code == SET_MOVE) {
				
			}
			
			if (code == SET_UPDATE_CONNECTED) {
				
			}
			
		} else {
			wr.returnError();
		}
		// wr.invitepopuped = false;
	}

}

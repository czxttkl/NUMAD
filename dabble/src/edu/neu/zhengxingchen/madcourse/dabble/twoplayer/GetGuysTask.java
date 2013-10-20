package edu.neu.zhengxingchen.madcourse.dabble.twoplayer;

import android.os.AsyncTask;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import edu.neu.mhealth.api.KeyValueAPI;
import edu.neu.zhengxingchen.madcourse.dabble.DabbleWaitRoom;
import edu.neu.zhengxingchen.madcourse.dabble.helper.Global;

public class GetGuysTask extends AsyncTask<Integer, Integer, String> {

	public static String usr = "czxttkl";
	public static String pwd = "cZxttkl,1";

	public static final int REGISTER = 1;
	public static final int UNREGISTER = 2;
	public static final int LOOK_FOR_GUY = 3;
	// public static final int FIRST_TIME = 1;

	public DabbleWaitRoom wr;
	public int code;

	public GetGuysTask(DabbleWaitRoom wr, int code) {
		this.wr = wr;
		this.code = code;
	}

	@Override
	protected String doInBackground(Integer... arg0) {
		
		long time = System.currentTimeMillis();
		String getResult = null;
		
		if (KeyValueAPI.isServerAvailable()) {
			getResult = KeyValueAPI.get(usr, pwd, Global.SERVER_KEY_GUY_LIST);
		} else {
			getResult = "Error";
		}
		
		time = System.currentTimeMillis() - time;
//		Log.d("waitroom", getResult);
		return getResult;
	}

	@Override
	protected void onPostExecute(String result) {
		Log.d("waitroom", "getguytask:list" + result);
		
		if (result.startsWith("Error") && code!=UNREGISTER) {
//			wr.returnError();
		} else {
			wr.list = result;
			if (code == REGISTER) {
				wr.afterGetGuysListRegister();
			}
			if (code == LOOK_FOR_GUY) {
				wr.afterGetGuysList();
//				wr.afterLookForGetGuys();
			}
		}
//			if (code == REGISTER) {
//				wr.list = result;
//				wr.startAddGuyTask();
//			} else {
//				if (code == LOOK_FOR_GUY) {
//					wr.afterLookForGetGuys();
//				} else if (code == UNREGISTER) {
//					wr.startRemoveGuyTask();
//				}
//			}
//		}
	}
}

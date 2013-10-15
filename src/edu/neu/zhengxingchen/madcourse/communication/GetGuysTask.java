package edu.neu.zhengxingchen.madcourse.communication;

import android.os.AsyncTask;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import edu.neu.mhealth.api.KeyValueAPI;

public class GetGuysTask extends AsyncTask<Integer, Integer, String> {

	public static String usr = "czxttkl";
	public static String pwd = "cZxttkl,1";

	public static final int REGISTER = 1;
	public static final int UNREGISTER = 2;
	public static final int LOOK_FOR_GUY = 3;
	// public static final int FIRST_TIME = 1;

	public WaitRoomService wr;
	public int code;

	public GetGuysTask(WaitRoomService wr) {
		this.wr = wr;
	}

	@Override
	protected String doInBackground(Integer... arg0) {
		code = arg0[0];
		long time = System.currentTimeMillis();
		String getResult = null;
		Log.d("getgy", KeyValueAPI.isServerAvailable() + "");
		
		if (KeyValueAPI.isServerAvailable()) {
			getResult = KeyValueAPI.get(usr, pwd, "guyslist");
		} else {
			getResult = "Error";
		}
		
		time = System.currentTimeMillis() - time;
		Log.d("waitroom", getResult);
		return getResult;
	}

	@Override
	protected void onPostExecute(String result) {
		Log.d("waitroom", result);
		if (result.startsWith("Error") && code!=UNREGISTER) {
			wr.returnError();
		} else {
			wr.list = result;
			if (code == REGISTER) {
				wr.startAddGuyTask();
			} else {
				if (code == LOOK_FOR_GUY) {
					wr.afterLookForGetGuys();
				} else if (code == UNREGISTER) {
					wr.startRemoveGuyTask();
				}
			}
		}
	}
}

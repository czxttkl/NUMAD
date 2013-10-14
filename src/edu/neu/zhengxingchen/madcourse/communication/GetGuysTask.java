package edu.neu.zhengxingchen.madcourse.communication;

import android.os.AsyncTask;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import edu.neu.mhealth.api.KeyValueAPI;

public class GetGuysTask extends AsyncTask<String, Integer, String>{

	public static String usr = "czxttkl";
	public static String pwd = "cZxttkl,1";
	public static boolean checked = false;
	public WaitRoomService wr;
	
	public GetGuysTask(WaitRoomService wr) {
		this.wr = wr;
	}
	
	@Override
	protected String doInBackground(String... arg0) {
		
		long time = System.currentTimeMillis();
		String getResult = null;
		if(KeyValueAPI.isServerAvailable()) {
			getResult = KeyValueAPI.get(usr, pwd, "guyslist");
		}
		time = System.currentTimeMillis() - time;
		return getResult;
	}
	@Override
	protected void onPostExecute(String result) {
		Log.d("waitroom", result);
		wr.list = result;
		if(!checked) {
			wr.startAddGuyTask();
			checked = true;
		} else {
			wr.afterGetGuysTask();
		}
	}
}

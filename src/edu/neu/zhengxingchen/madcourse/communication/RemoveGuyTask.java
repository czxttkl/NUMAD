package edu.neu.zhengxingchen.madcourse.communication;

import android.os.AsyncTask;
import android.util.Log;
import edu.neu.mhealth.api.KeyValueAPI;

public class RemoveGuyTask extends AsyncTask<String, Integer, String>{
	
	public static String usr = "czxttkl";
	public static String pwd = "cZxttkl,1";
	public WaitRoomService wr;
	
	public RemoveGuyTask(WaitRoomService wr) {
		this.wr = wr;
	}
	
	@Override
	protected String doInBackground(String... arg0) {
		
		String[] guys = wr.list.split(":");
		String newResult = "";
		for (String guy : guys) {
			if(guy.equals(Global.SERIAL))
				continue;
			else {
				newResult = newResult + ":" + guy;
			}
		}
		
		Log.d("removeguy", newResult);
		String putResult = null;
		if(KeyValueAPI.isServerAvailable()) {
			putResult = KeyValueAPI.put(usr, pwd, Global.SERVER_KEY_GUY_LIST, newResult.substring(1));
		}
		return putResult;
	}
	@Override
	protected void onPostExecute(String result) {
		wr.afterRemoveGuyTask();
	}

}

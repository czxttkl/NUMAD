package edu.neu.zhengxingchen.madcourse.communication;

import android.os.AsyncTask;
import edu.neu.mhealth.api.KeyValueAPI;

public class AddGuyTask extends AsyncTask<String, Integer, String>{

	public static String usr = "czxttkl";
	public static String pwd = "cZxttkl,1";
	public WaitRoomService wr;
	
	public AddGuyTask(WaitRoomService wr) {
		this.wr = wr;
	}
	
	@Override
	protected String doInBackground(String... arg0) {
		String list = arg0[0];
		String value = list + ":" + arg0[1];
		
		long time = System.currentTimeMillis();
		String putResult = null;
		if(KeyValueAPI.isServerAvailable()) {
			putResult = KeyValueAPI.put(usr, pwd, "guyslist", value);
		}
		time = System.currentTimeMillis() - time;
		return putResult;
	}
	@Override
	protected void onPostExecute(String result) {
		wr.afterAddGuyTask(result);
	}

	
}

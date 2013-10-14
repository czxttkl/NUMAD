package edu.neu.zhengxingchen.madcourse.communication;

import edu.neu.mhealth.api.KeyValueAPI;
import android.os.AsyncTask;

public class PutValueTask extends AsyncTask<String, Integer, String>{

	public static String usr = "czxttkl";
	public static String pwd = "cZxttkl,1";
	public WaitRoom wr;
	
	public PutValueTask(WaitRoom wr) {
		this.wr = wr;
	}
	
	@Override
	protected String doInBackground(String... arg0) {
		String key = arg0[0];
		long time = System.currentTimeMillis();
		String putResult = null;
		if(KeyValueAPI.isServerAvailable()) {
			putResult = KeyValueAPI.put(usr, pwd, key, "79");
		}
		time = System.currentTimeMillis() - time;
		return String.valueOf(time) + putResult;
	}
	@Override
	protected void onPostExecute(String result) {
		wr.startConnect(result);
	}

	
}

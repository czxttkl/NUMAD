package edu.neu.zhengxingchen.madcourse.communication;


import edu.neu.mhealth.api.KeyValueAPI;
import android.os.AsyncTask;

public class GetValueTask extends AsyncTask<String, Integer, String>{

	public static String usr = "czxttkl";
	public static String pwd = "cZxttkl,1";
	public WaitRoom wr;
	
	public GetValueTask(WaitRoom wr) {
		this.wr = wr;
	}
	
	@Override
	protected String doInBackground(String... arg0) {
		String key = arg0[0];
		long time = System.currentTimeMillis();
		String getResult = null;
		if(KeyValueAPI.isServerAvailable()) {
			getResult = KeyValueAPI.get(usr, pwd, key);
		}
		time = System.currentTimeMillis() - time;
		return "time:" + String.valueOf(time) + " result:" + getResult;
	}
	@Override
	protected void onPostExecute(String result) {
		wr.afterGetValue(result);
	}

	
}

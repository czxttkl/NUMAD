package edu.neu.zhengxingchen.madcourse.communication;

import java.util.Date;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
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
		String[] guys = list.split(":");
		boolean needed = true;
		for(String guy : guys){
			if(guy.equals(wr.mClientSerial))
				needed = false;
		}
		
		String value;
		if( needed )
			value = list + ":" + arg0[1];
		else
			value = list;
		
		
		String putResult = null;
		if(KeyValueAPI.isServerAvailable()) {
			putResult = KeyValueAPI.put(usr, pwd, "guyslist", value);
			SntpClient sn = new SntpClient();
			long now = 0;
			if(sn.requestTime("pool.ntp.org", 5000))
				now = sn.getNtpTime() + SystemClock.elapsedRealtime() - sn.getNtpTimeReference();
			Date date=new Date(now);
			KeyValueAPI.put(usr, pwd, wr.mClientSerial, date.toString()+":wait");
			
			Log.d("waitroom", "sntp: ntpTime:" + now + " :" + date.toString());
		} else{
			putResult = "Error";
		}
		return putResult;
	}
	@Override
	protected void onPostExecute(String result) {
		if(result.equals("true"))
			wr.afterAddGuyTask(result);
		else
			wr.returnError();
	}

	
}

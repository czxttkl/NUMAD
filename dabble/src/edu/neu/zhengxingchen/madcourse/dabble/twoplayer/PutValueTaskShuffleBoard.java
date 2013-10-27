package edu.neu.zhengxingchen.madcourse.dabble.twoplayer;


import edu.neu.mhealth.api.KeyValueAPI;
import edu.neu.zhengxingchen.madcourse.dabble.DabbleWaitRoom;
import edu.neu.zhengxingchen.madcourse.dabble.ShuffleBoard;
import edu.neu.zhengxingchen.madcourse.dabble.helper.Global;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.SystemClock;

public class PutValueTaskShuffleBoard extends AsyncTask<String, Integer, String>{

	public static String usr = "czxttkl";
	public static String pwd = "cZxttkl,1";
	public ShuffleBoard wr;
	public static int code;
	public static int GET_SHUFFLED_STRING = 1;
	
	
	public PutValueTaskShuffleBoard(ShuffleBoard wr, int code) {
		this.wr = wr;
		this.code = code;
	}
	
	@Override
	protected String doInBackground(String... arg0) {
		
		String getResult = "Error";
		if(code == 0) {
			String key = arg0[0];
			if(KeyValueAPI.isServerAvailable()) {
				getResult = KeyValueAPI.get(usr, pwd, key);
			}
			return getResult;
		}
		
		if(code == GET_SHUFFLED_STRING) {
			
			if(KeyValueAPI.isServerAvailable()) {
				long now = Global.NTP_REFERENCE + SystemClock.elapsedRealtime();
				getResult = KeyValueAPI.put(Global.USER_NAME, Global.PASSWORD, Global.RIVAL, now + ":"
						+ Global.SERVER_STATUS_INGAME + ":" + Global.SERIAL + ":" + Global.SERVER_SUBSTATUS_SHUFFLE + ":"
						+ String.valueOf(wr.dabbleArray));
			}
			
			
			while(true && !getResult.startsWith("Error")) {
				if(KeyValueAPI.isServerAvailable()) {
					getResult = KeyValueAPI.get(usr, pwd, Global.SERIAL);
					String[] results = getResult.split(":");
					if ( results[3].equals(Global.SERVER_SUBSTATUS_SHUFFLE)) {
						if( results.length == 4 ) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
							
							}
							continue;
						}
						
						if( results.length == 5 ) {
							long now = Global.NTP_REFERENCE + SystemClock.elapsedRealtime();
							KeyValueAPI.put(Global.USER_NAME, Global.PASSWORD, Global.SERIAL, 
									now + ":" + Global.SERVER_STATUS_INGAME + ":" + Global.RIVAL 
									+ ":" + Global.SERVER_SUBSTATUS_START_GAME + ":" + results[4] + ":" + 0);
							return results[4];
						}
					}
				} else
					return "Error";
			}
		}
		
		
		return getResult;
		
	}
	
	
	@Override
	protected void onPostExecute(String result) {
//		wr.afterGetValue(result);
		if(result.equals("Error")) {
			
		} else {
			if( code == GET_SHUFFLED_STRING ) {
				wr.afterInitGameStart(result);
				
			}
		}
		
		
	}

	
}

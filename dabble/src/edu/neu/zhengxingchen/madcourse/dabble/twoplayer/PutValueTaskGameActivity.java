package edu.neu.zhengxingchen.madcourse.dabble.twoplayer;

import edu.neu.mhealth.api.KeyValueAPI;
import edu.neu.zhengxingchen.madcourse.dabble.game.GameActivity;
import edu.neu.zhengxingchen.madcourse.dabble.helper.Global;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

public class PutValueTaskGameActivity extends AsyncTask<String, Integer, String>{
	public GameActivity wr;
	public static int code;
	public boolean finishGameActivity = false;
	public static int SET_DABBLES_STRING_AND_SCORE = 1;
	public static int GAME_OVER_CLEAR_KEY = 2;
	public boolean[] finishDual = {false, false};
	
	
	public PutValueTaskGameActivity(GameActivity gameActivity, int code) {
		this.wr = gameActivity;
		this.code = code;
	}
	
	@Override
	protected String doInBackground(String... arg0) {
		// TODO Auto-generated method stub
		
		String putResult = "Error";
		if( code == SET_DABBLES_STRING_AND_SCORE) {
			if(KeyValueAPI.isServerAvailable()) {
				Log.d("dabblegameactivity", "now set score:" + arg0[1] + ": array" + arg0[0]);
				long now = Global.NTP_REFERENCE + SystemClock.elapsedRealtime();
				putResult = KeyValueAPI.put(Global.USER_NAME, Global.PASSWORD, Global.SERIAL, 
						now + ":" + Global.SERVER_STATUS_INGAME + ":" + Global.RIVAL + ":" +
							Global.SERVER_SUBSTATUS_START_GAME + ":" + arg0[0] + ":" + arg0[1]);
				if( Integer.valueOf(arg0[1])>=18 )
					finishDual[0] = true;
				
			}
		}
		
		if( code == GAME_OVER_CLEAR_KEY ) {
			if(KeyValueAPI.isServerAvailable()) {
//				KeyValueAPI.clearKey(Global.USER_NAME, Global.PASSWORD, Global.SERIAL);
				KeyValueAPI.clearKey(Global.USER_NAME, Global.PASSWORD, Global.RIVAL);
				//TODO here should not clear whole list
				KeyValueAPI.put(Global.USER_NAME, Global.PASSWORD, Global.SERVER_KEY_GUY_LIST, "");
				finishDual[1] = true;
			}
		}
		
		
		return putResult;
	}

	@Override
	protected void onPostExecute(String result) {
		if(result.equals("Error")) {
			
		} else {
//			if (code == SET_DABBLES_STRING_AND_SCORE && finishGameActivity) {
			if( (finishDual[0] || wr.rivalScore >= 18) && finishDual[1] )
				wr.finish();
//			}
		}
	}
}

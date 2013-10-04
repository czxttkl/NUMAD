package edu.neu.zhengxingchen.madcourse.dabble;

import java.util.ArrayList;
import java.util.Arrays;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

public class WordLookUpTask extends AsyncTask<char[], Void, int[]>{

	private final GameActivity gameActivity;
	
	public WordLookUpTask(GameActivity activity) {
		this.gameActivity = activity;
	}
	
	@Override
	protected int[] doInBackground(char[]... params) {

		return WordLookUp.lookUp(params[0]);
	}

	@Override
	protected void onPostExecute(int[] result) {
		Log.d("dabble", "onpostExecute:" + String.valueOf(gameActivity.dabbleArray));
		gameActivity.updateTileColor(result);
	}

//	private static String concatTile(int tileId) {
//		StringBuilder sb = new StringBuilder();
//		if(tileId >=0 && tileId <=2) {
//			String 
//			sb.append()
//		} else if(tileId>=3 && tileId<=6) {
//			
//		} else if (tileId>=7 &&tileId<=11) {
//			
//		} else if (tileId>=12 && tileId<= 17) {
//			
//		}
//		return null;
//		
//	}
//	
	
}

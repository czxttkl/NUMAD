package edu.neu.zhengxingchen.madcourse.dabble;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

//public class LoadDicTask extends AsyncTask<InputStream, Integer, Trie<String, String>>{
  public class LoadDicTask extends AsyncTask<InputStream, Integer, Object[]>{

	private final GameActivity activity;		
	
	public LoadDicTask(GameActivity activity) {
		this.activity = activity;
		
	}
	
	@Override
//	protected Trie<String, String> doInBackground(InputStream... in) {
	protected Object[] doInBackground(InputStream... in) {
		InputStream wordInputStream = in[0];
		BufferedReader buff = null;
		String[][] sa = new String[10][4];
		
		try {
			buff = new BufferedReader(new InputStreamReader(wordInputStream));
			String word;
			
			for(int i = 0; i<40; i++) {
				word = buff.readLine();
				sa[i/4][i%4] = word;
				//Log.d("dabble", word + " i/4" + i/4 + " i%4 :" + i%4);
			}
			buff.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 

		InputStream wordInputStream1 = in[1];
		BufferedReader buff1 = null;
		String[] sa1 = new String[432334];	
		
		try {
			buff1 = new BufferedReader(new InputStreamReader(wordInputStream1));
			for(int i = 0; i<432334; i++)
				sa1[i] = buff1.readLine();
			buff1.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		Object[] result = {sa, sa1};
		return result;
	}

	@SuppressLint("Recycle")
	@Override
	protected void onPostExecute(Object[] result) {
		Random rand = new Random();
		int  n = rand.nextInt(9);
		
		String[][] tileWords = (String[][])result[0];
		
		activity.dabbleString = tileWords[n][0] + tileWords[n][1] + tileWords[n][2] + tileWords[n][3];
		
		activity.wholeArray = (String[])result[1];
		WordLookUp.wholeDict = activity.wholeArray;
		
		activity.initialTile();
		
		TextView dicTitle = (TextView)activity.findViewById(R.id.game_title);
		dicTitle.setText("Go!");
        
        if( activity.beepStreamId!=0 && activity.sp!=null ) {
		 Timer timer = new Timer();
		    timer.schedule(new TimerTask() {
		         @Override
		         public void run() {
		     		activity.sp.play(activity.beepStreamId, 1, 1, 0, 0, 1);
		         }
		    }, 200);   
        }
	}

	

	

}

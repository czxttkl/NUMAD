package edu.neu.zhengxingchen.madcourse.dictionary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.StringKeyAnalyzer;
import org.ardverk.collection.Trie;

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
  public class LoadDicTask extends AsyncTask<InputStream, Integer, Hashtable<String, String>>{

	private final TestDictionary activity;		
	
	public LoadDicTask(TestDictionary activity) {
		this.activity = activity;
		
	}
	
	@Override
//	protected Trie<String, String> doInBackground(InputStream... in) {
	protected Hashtable<String, String> doInBackground(InputStream... in) {
		InputStream wordInputStream = in[0];
		BufferedReader buff = null;
//		Trie<String, String> trie = new PatriciaTrie<String, String>(StringKeyAnalyzer.INSTANCE);
		Hashtable<String, String> hb = new Hashtable<String, String>(440000);

		long startTime = System.nanoTime();
		
		try {
//			buff = new BufferedReader(new FileReader(new File(fileName).getAbsoluteFile()));
			buff = new BufferedReader(new InputStreamReader(wordInputStream));
			String word;

			while((word = buff.readLine()) != null) {
				//Log.d("TD", word);
				//trie.put(word, word);
				hb.put(word, word);
			}
			
			buff.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		long endTime = System.nanoTime();
		Log.d("TD", "load time consumed:" + (endTime - startTime));
		
		//Log.d("TD", trie.selectValue("abate"));
		return hb;
	}

	@SuppressLint("Recycle")
	@Override
//	protected void onPostExecute(Trie<String, String> result) {
	protected void onPostExecute(Hashtable<String, String> result) {
		Log.d("TD", "loaded wordlist");
//		activity.trie = result;
		activity.hb = result;
		TextView dicTitle = (TextView)activity.findViewById(R.id.dictionary_title);
		dicTitle.setText("Input Below:");
		EditText input = (EditText)activity.findViewById(R.id.input);
		input.setFocusable(true);
		input.setClickable(true);
		input.setFocusableInTouchMode(true);
		
		input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN , 0, 0, 0));
        input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP , 0, 0, 0));           
        
//        Log.d("TD", activity.beepStreamId + "" + activity.sp);
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

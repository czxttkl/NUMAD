package edu.neu.zhengxingchen.madcourse.dictionary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
  public class LoadDicTask extends AsyncTask<InputStream, Integer, String[]>{

	private final TestDictionary activity;		
	
	
	
	public LoadDicTask(TestDictionary activity) {
		this.activity = activity;
		
	}
	
	@Override
//	protected Trie<String, String> doInBackground(InputStream... in) {
	protected String[] doInBackground(InputStream... in) {
		InputStream wordInputStream = in[0];
		BufferedReader buff = null;
//		Trie<String, String> trie = new PatriciaTrie<String, String>(StringKeyAnalyzer.INSTANCE);
//		String[] sa = new String[432334];
		ArrayList<String> sa = new ArrayList<String>();
		
		long startTime = System.nanoTime();
		
		try {
//			buff = new BufferedReader(new FileReader(new File(fileName).getAbsoluteFile()));
			buff = new BufferedReader(new InputStreamReader(wordInputStream));
			

//			while((word = buff.readLine()) != null) {
//				//Log.d("TD", word);
//				//trie.put(word, word);
//				hb.put(word, word);
//			}
			String word;
			int i = 0;
			while((word = buff.readLine()) != null) {
				sa.add(word);
				i++;
			}
			
			buff.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		long endTime = System.nanoTime();
		Log.d("TD", "load time consumed:" + (endTime - startTime));
		
		//String[] result = new String[sa.size()];
		String[] result = sa.toArray(new String[sa.size()]);
		//Log.d("TD", trie.selectValue("abate"));
		return result;
	}

	@SuppressLint("Recycle")
	@Override
//	protected void onPostExecute(Trie<String, String> result) {
	protected void onPostExecute(String[] result) {
		Log.d("TD", "loaded wordlist");
//		activity.trie = result;
		activity.sa = result;
	}

	

	

}

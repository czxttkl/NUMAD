package edu.neu.zhengxingchen.madcourse.dictionary;

import java.util.ArrayList;
import java.util.Arrays;

import android.os.AsyncTask;
import android.util.Log;

public class WordLookUpTask extends AsyncTask<String, Void, String>{

	private final TestDictionary activity;
	static volatile String currentInput = "";
	
	
	public WordLookUpTask(TestDictionary activity) {
		this.activity = activity;
	}
	
	@Override
	protected String doInBackground(String... params) {
		// TODO Auto-generated method stub
		Log.d("TD","after:"+ params[0].toString());
		
//		if( activity.trie != null) {
		
//		if(!currentInput.equals(params[0].toString())) {
//				currentInput = params[0].toString();
////				if(currentInput.equals(activity.trie.selectValue(currentInput))){
//				if(currentInput.equals(activity.sa.get(currentInput))){
//					return currentInput;
//				}
//		}
		
		if(!currentInput.equals(params[0]) && !activity.record.contains(params[0])) { 
			currentInput = params[0];
			activity.record.add(currentInput);
			
			Log.e("dictionary", activity.sa.length + ":" + params[0].toString());
			
			do {
				if( activity.sa != null) {
					int i = Arrays.binarySearch(activity.sa, params[0]);
					if ( i >= 0)
						return activity.sa[i];
				}
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (true);
		}
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		if(result != null)
			activity.onWordFound(result);
	}

	
}

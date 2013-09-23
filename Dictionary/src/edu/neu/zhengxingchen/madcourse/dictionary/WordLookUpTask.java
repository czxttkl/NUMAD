package edu.neu.zhengxingchen.madcourse.dictionary;

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
		if(!currentInput.equals(params[0].toString())) {
			currentInput = params[0].toString();
			int i = Arrays.binarySearch(activity.sa, params[0]);
			if ( i >= 0)
				return activity.sa[i];
		}
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		if(result != null)
			activity.onWordFound(result);
	}

	
}
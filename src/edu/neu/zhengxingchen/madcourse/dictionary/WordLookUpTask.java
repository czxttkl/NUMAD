package edu.neu.zhengxingchen.madcourse.dictionary;

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
		if( activity.hb != null) {
			if(!currentInput.equals(params[0].toString())) {
				currentInput = params[0].toString();
//				if(currentInput.equals(activity.trie.selectValue(currentInput))){
				if(currentInput.equals(activity.hb.get(currentInput))){
					return currentInput;
				}
			}
		}
		
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		if(result != null)
			activity.onWordFound(result);
	}

	
}

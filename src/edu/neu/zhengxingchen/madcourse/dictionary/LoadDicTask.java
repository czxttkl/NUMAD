package edu.neu.zhengxingchen.madcourse.dictionary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.StringKeyAnalyzer;
import org.ardverk.collection.Trie;

import android.os.AsyncTask;
import android.util.Log;

public class LoadDicTask extends AsyncTask<String, Integer, Trie<String, String>>{

	@Override
	protected Trie<String, String> doInBackground(String... strings) {
		// TODO Auto-generated method stub
		String fileName = strings[0];
		BufferedReader in = null;
		Trie<String, String> trie = new PatriciaTrie<String, String>(StringKeyAnalyzer.INSTANCE);

		long startTime = System.nanoTime();
		
		try {
			in = new BufferedReader(new FileReader(new File(fileName).getAbsoluteFile()));
			String word;

			while((word = in.readLine()) != null) {
				Log.d("TD", word);
				trie.put(word, word);
			}
			
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		
		
		long endTime = System.nanoTime();
		Log.d("TD", "time consumed:" + (endTime - startTime));
		Log.d("TD", trie.selectKey("abate"));
		return trie;
	}

	@Override
	protected void onPostExecute(Trie<String, String> result) {
		Log.d("TD", "loaded");
	}

	

	

}

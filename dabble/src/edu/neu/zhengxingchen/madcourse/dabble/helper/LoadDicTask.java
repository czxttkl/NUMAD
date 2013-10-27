package edu.neu.zhengxingchen.madcourse.dabble.helper;

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

import edu.neu.zhengxingchen.madcourse.dabble.game.GameActivity;

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
public class LoadDicTask extends AsyncTask<InputStream, Integer, Object[]> {

	private final GameActivity activity;
	private final String target;

	public LoadDicTask(GameActivity activity, String target) {
		this.activity = activity;
		this.target = target;

	}

	@Override
	// protected Trie<String, String> doInBackground(InputStream... in) {
	protected Object[] doInBackground(InputStream... in) {

		// if(target.equals("short_wordlist")) {
		// // InputStream wordInputStream = in[0];
		// // BufferedReader buff = null;
		//
		// //
		// // try {
		// // buff = new BufferedReader(new InputStreamReader(wordInputStream));
		// // String word;
		// //
		// // for(int i = 0; i<40; i++) {
		// // word = buff.readLine();
		// // sa[i/4][i%4] = word;
		// // //Log.d("dabble", word + " i/4" + i/4 + " i%4 :" + i%4);
		// // }
		// // buff.close();
		// // } catch (IOException e) {
		// // e.printStackTrace();
		// // }
		// return sa;
		// }

		if (target.equals("medium_wordlist")) {
			InputStream wordInputStream1 = in[0];
			BufferedReader buff1 = null;
			String[] sa1 = new String[57300];

			try {
				buff1 = new BufferedReader(new InputStreamReader(
						wordInputStream1));
				for (int i = 0; i < 57300; i++)
					sa1[i] = buff1.readLine();
				buff1.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return sa1;
		}

		return null;
	}

	@SuppressLint("Recycle")
	@Override
	protected void onPostExecute(Object[] result) {

		if (target.equals("medium_wordlist")) {
			activity.wholeDict = (String[]) result;
			WordLookUp.wholeDict = activity.wholeDict;
			if (activity.dabbleString == null) {
				String[] a1 = { "you", "cold", "mouse", "really" };
				String[] a2 = { "two", "nine", "beach", "studio" };
				String[] a3 = { "one", "half", "paper", "inside" };
				String[][] tileWords = { a1, a2, a3 };
				Random rand = new Random();
				int n = rand.nextInt(3);
				activity.dabbleString = tileWords[n][0] + tileWords[n][1]
						+ tileWords[n][2] + tileWords[n][3];
			}
			activity.initialTile();

			if (activity.beepStreamId != 0 && activity.sp != null) {
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						activity.sp.play(activity.beepStreamId, 1, 1, 0, 0, 1);
					}
				}, 200);
			}
		}

		// Random rand = new Random();
		// int n = rand.nextInt(2);
		//
		// if(activity.dabbleString == null) {
		// String[][] tileWords = (String[][])result;
		// activity.dabbleString = tileWords[n][0] + tileWords[n][1] +
		// tileWords[n][2] + tileWords[n][3];
		// }
		//
		// while( WordLookUp.wholeDict == null ) {
		// // try {
		// //// Thread.sleep(200);
		// // } catch (InterruptedException e) {
		// //
		// // }
		// }
		// activity.initialTile();

		// }

	}

}

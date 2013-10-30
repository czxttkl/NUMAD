package edu.neu.zhengxingchen.madcourse.dabble.helper;


import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import edu.neu.zhengxingchen.madcourse.dabble.ShuffleBoard;
import edu.neu.zhengxingchen.madcourse.dabble.game.GameActivity;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.util.Log;

public class LoadShakeBeepTask extends AsyncTask<Void, Void, Integer>{

	private final ShuffleBoard activity;
	
	public LoadShakeBeepTask(ShuffleBoard activity) {
		this.activity = activity;
	}
	
	@Override
	protected Integer doInBackground(Void... params) {
		int beepStreamId = 0;
		try {
			activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			AssetFileDescriptor afd = activity.getAssets().openFd("beep.mp3");
			activity.sp = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
			beepStreamId = activity.sp.load(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength(),0);
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return beepStreamId;
	}

	@Override
	protected void onPostExecute(Integer result) {
		 activity.beepStreamId = result;
		 Log.d("TD", "loaded audio");
	}

}

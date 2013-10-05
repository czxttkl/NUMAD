package edu.neu.zhengxingchen.madcourse.dabble;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.util.Log;

public class LoadBeepTask extends AsyncTask<Void, Void, Integer>{

	private final GameActivity activity;
	
	public LoadBeepTask(GameActivity activity) {
		this.activity = activity;
	}
	
	@Override
	protected Integer doInBackground(Void... params) {
		int streamId = 0;
		try {
			activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			AssetFileDescriptor afd = activity.getAssets().openFd("beep.mp3");
			activity.sp = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
			streamId = activity.sp.load(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength(),0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return streamId;
	}

	@Override
	protected void onPostExecute(final Integer result) {
		 activity.beepStreamId = result;
		 Log.d("TD", "loaded audio");
	}

}

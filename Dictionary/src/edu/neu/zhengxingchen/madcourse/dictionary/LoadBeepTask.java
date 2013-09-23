package edu.neu.zhengxingchen.madcourse.dictionary;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.util.Log;

public class LoadBeepTask extends AsyncTask<Void, Void, Integer>{

	private final TestDictionary activity;
	
	public LoadBeepTask(TestDictionary activity) {
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
//		 Timer timer = new Timer();
//		    timer.schedule(new TimerTask() {
//		         @Override
//		         public void run() {
//		        	 Log.d("TD", "loaded audio");
//		     		activity.sp.play(result.intValue(), 1, 1, 0, 0, 1);
//		         }
//		    }, 200);     
	}

}

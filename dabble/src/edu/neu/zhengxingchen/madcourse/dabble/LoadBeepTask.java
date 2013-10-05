package edu.neu.zhengxingchen.madcourse.dabble;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.util.Log;

public class LoadBeepTask extends AsyncTask<Void, Void, int[]>{

	private final GameActivity activity;
	
	public LoadBeepTask(GameActivity activity) {
		this.activity = activity;
	}
	
	@Override
	protected int[] doInBackground(Void... params) {
		int beepStreamId = 0;
		int tickStreamId = 0;
		try {
			activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			AssetFileDescriptor afd = activity.getAssets().openFd("beep.mp3");
			activity.sp = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
			beepStreamId = activity.sp.load(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength(),0);
		
			AssetFileDescriptor afdt = activity.getAssets().openFd("tick.mp3");
			tickStreamId = activity.sp.load(afdt.getFileDescriptor(),afdt.getStartOffset(),afdt.getLength(),0);
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new int[]{beepStreamId, tickStreamId};
	}

	@Override
	protected void onPostExecute(final int[] result) {
		 activity.beepStreamId = result[0];
		 activity.tickStreamId = result[1];
		 Log.d("TD", "loaded audio");
	}

}

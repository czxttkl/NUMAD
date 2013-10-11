package edu.neu.zhengxingchen.madcourse.dictionary;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.TextView;

public class LoadBeepTask extends AsyncTask<Void, Void, Integer> {

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
			streamId = activity.sp.load(afd.getFileDescriptor(),
					afd.getStartOffset(), afd.getLength(), 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return streamId;
	}

	@Override
	protected void onPostExecute(final Integer result) {
		activity.beepStreamId = result;
		Log.d("TD", "loaded audio");
		TextView dicTitle = (TextView) activity
				.findViewById(R.id.dictionary_title);
		dicTitle.setText("Input Below:");
		EditText input = (EditText) activity.findViewById(R.id.input);
		input.setFocusable(true);
		input.setClickable(true);
		input.setFocusableInTouchMode(true);

		input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
				SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
		input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
				SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));

		// Log.d("TD", activity.beepStreamId + "" + activity.sp);
		if (activity.beepStreamId != 0 && activity.sp != null) {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					activity.sp.play(activity.beepStreamId, 1, 1, 0, 0, 1);
				}
			}, 200);
		}
		// Timer timer = new Timer();
		// timer.schedule(new TimerTask() {
		// @Override
		// public void run() {
		// Log.d("TD", "loaded audio");
		// activity.sp.play(result.intValue(), 1, 1, 0, 0, 1);
		// }
		// }, 200);
	}

}

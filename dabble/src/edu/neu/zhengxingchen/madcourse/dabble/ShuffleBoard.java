package edu.neu.zhengxingchen.madcourse.dabble;

import java.util.Random;

import edu.neu.zhengxingchen.madcourse.dabble.game.GameActivity;
import edu.neu.zhengxingchen.madcourse.dabble.game.Tile;
import edu.neu.zhengxingchen.madcourse.dabble.helper.LoadBeepTask;
import edu.neu.zhengxingchen.madcourse.dabble.helper.MyGameCountDownTimer;
import edu.neu.zhengxingchen.madcourse.dabble.helper.MyShuffleCountDownTimer;
import edu.neu.zhengxingchen.madcourse.dabble.helper.WordLookUpTask;
import android.media.SoundPool;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class ShuffleBoard extends Activity {
	public String dabbleString = null;
	public char[] dabbleArray = new char[18];

	public long startTime = 20 * 1000;
	public long interval = 70;
	public int score = 0;
	MyShuffleCountDownTimer myCountDownTimer;

	@Override
	protected void onPause() {
		super.onPause();
		if (Music.musicShouldPause) {
			Music.pause(this);
			Music.musicPaused = true;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (Music.musicPaused) {
			Music.start(this);
			Music.musicPaused = false;
		}
		Music.musicShouldPause = true;

	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_shuffle_board);
		myCountDownTimer = new MyShuffleCountDownTimer(this, startTime, interval);
		initialTile();
	}

	public void initialTile() {
		dabbleString = "youcoldmousereally";
		dabbleArray = dabbleString.toCharArray();

		for (int j = 1; j < 19; j++) {
			int resId = getResources().getIdentifier(
					"sb_tile" + Integer.toString(j), "id", getPackageName());
			Tile mTile = (Tile) findViewById(resId);
			mTile.setCharacter(String.valueOf(dabbleArray[j - 1]));
		}

		Log.d("dabble", "after init: dabbleString:" + dabbleString
				+ "  dabbleArray:" + String.valueOf(dabbleArray));

		myCountDownTimer.start();
	}

	public void initGameStart() {

	}

}

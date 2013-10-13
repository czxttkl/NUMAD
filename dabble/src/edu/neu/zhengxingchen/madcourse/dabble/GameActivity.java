package edu.neu.zhengxingchen.madcourse.dabble;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Color;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GameActivity extends Activity {


  
    public final String TAG = "dabble";
    public static GameActivity instance = null;
    
	public String dabbleString = null;
	public char[] dabbleArray = new char[18];
	public String[] wholeArray = null;
	// public Tile[] tileArray = null;
	protected SoundPool sp = null;
	protected int beepStreamId = 0;
	protected int tickStreamId = 0;
	
	public Vibrator vibrator;
	
	
	public int clickCount = 0;
	public int[] clickTileId = new int[2];

	public long startTime = 90 * 1000;
	public long interval = 70;
	public int score = 0;
	public boolean countDownShouldPlay = false;
//	private boolean musicShouldPause = true;

	MyCountDownTimer myCountDownTimer;
	SharedPreferences mSharedPreferences;
	
	public volatile boolean initing = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_game);
		vibrator= (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//		Music.play(this, R.raw.background);
		
		Object[] myObjectData = (Object[]) getLastNonConfigurationInstance();
		if (savedInstanceState != null && myObjectData != null) {
			loadWhole(savedInstanceState);
//			mSharedPreferences = (SharedPreferences)myObjectData[0];
			sp = (SoundPool) myObjectData[1];
		} else {
			// LinearLayout gameLayout = (LinearLayout)
			// findViewById(R.id.game_layout);
			if(getIntent().getBooleanExtra("continue", false) && Prefs.getSaved(getBaseContext())) {
				dabbleString = Prefs.getSavedDabbleString(getBaseContext());
				dabbleArray = Prefs.getSavedDabbleArray(getBaseContext()).toCharArray();
				startTime = Prefs.getSavedDabbleStartTime(getBaseContext());
				Log.d("dabble","gameactivity continue game" + " dabbleString:"+ dabbleString + " dabbleArray:" + String.valueOf(dabbleArray) + " continue:" + getIntent().getBooleanExtra("continue", false));
				Prefs.setSaved(getBaseContext(), false);				
			}
			
			try {
				if (wholeArray == null || dabbleString == null) {
					initing = true;
					new LoadDicTask(this).execute(getResources().getAssets()
							.open("short_wordlist.txt"), getResources()
							.getAssets().open("medium_wordlist.txt"));
					MyCountDownTimer.countDownPlayed = false;
				}

				if (beepStreamId == 0 || sp == null || tickStreamId == 0)
					new LoadBeepTask(this).execute();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		instance = this;
//		Log.d("dabble", "oncreate");

	}

	private void loadWhole(Bundle savedInstanceState) {
		wholeArray = savedInstanceState.getStringArray("wholeArray");
		dabbleArray = savedInstanceState.getCharArray("dabbleArray");
		beepStreamId = savedInstanceState.getInt("beepStreamId");
		tickStreamId = savedInstanceState.getInt("tickStreamId");
		dabbleString = savedInstanceState.getString("dabbleString");
		clickCount = savedInstanceState.getInt("clickCount");
		startTime = savedInstanceState.getLong("startTime");
//		musicShouldPause = savedInstanceState.getBoolean("musicShouldPause");
		
		TextView gameTitle = (TextView) findViewById(R.id.game_title);
		gameTitle.setText(savedInstanceState.getString("gameTitle"));
		TextView scoreText = (TextView) findViewById(R.id.score_text);
		scoreText.setText("Score:" + savedInstanceState.getInt("score"));

		Tile.setGameActivity(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveWhole(outState);
	}

	private void saveWhole(Bundle outState) {
		outState.putString("dabbleString", dabbleString);
		outState.putCharArray("dabbleArray", dabbleArray);
		outState.putStringArray("wholeArray", wholeArray);
		outState.putInt("beepStreamId", beepStreamId);
		outState.putInt("tickStreamId", tickStreamId);
		outState.putInt("clickCount", clickCount);
		outState.putLong("startTime", startTime);
		outState.putInt("score", score);
//		outState.putBoolean("musicShouldPause", musicShouldPause);
		
		TextView gameTitle = (TextView) findViewById(R.id.game_title);
		outState.putString("gameTitle", gameTitle.getText().toString());

		// outState.putSerializable("tileArray", tileArray);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		final Object[] myObjectData = new Object[2];
//		myObjectData[0] = mSharedPreferences;
		myObjectData[1] = sp;
		return myObjectData;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (myCountDownTimer != null)
			myCountDownTimer.cancel();
		//Log.d("dabble", "onpause:" + Music.musicShouldPause + ":" + Music.musicPaused);
		if(Music.musicShouldPause) {
			Music.pause(this);
			Music.musicPaused = true;
		}
		
		if(sp!=null && beepStreamId!=0 && countDownShouldPlay) {
			sp.pause(tickStreamId);
		}
		
		mSharedPreferences.registerOnSharedPreferenceChangeListener(mListener);
		
		if(startTime>0 && !dabbleString.equals(String.valueOf(dabbleArray))) {
			Log.d("dabble","gameactivity onpause save" + " dabbleString:"+ dabbleString + " dabbleArray:" + String.valueOf(dabbleArray) + " startTime:" + startTime);
			Prefs.setSaved(getBaseContext(), true);
			Prefs.setSavedDabbleArray(getBaseContext(), String.valueOf(dabbleArray));
			Prefs.setSavedDabbleString(getBaseContext(), dabbleString);
			Prefs.setSavedDabbleStartTime(getBaseContext(), startTime);
		}
		
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		//Log.d("dabble", "onrestart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		//Log.d("dabble", "onresume:"  + Music.musicShouldPause + ":" + Music.musicPaused);
		myCountDownTimer = new MyCountDownTimer(this, startTime, interval);
		if (!initing)
			myCountDownTimer.start();
		
		if( Music.musicPaused) {
			Music.start(this);
			Music.musicPaused = false;
		}
		Music.musicShouldPause = true;
		
		if(sp!=null && beepStreamId!=0 && countDownShouldPlay ) {
			sp.resume(tickStreamId);
		}
		
		mSharedPreferences.registerOnSharedPreferenceChangeListener(mListener);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		
		
//		Music.stop(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game, menu);
		return true;
	}

	public void initialTile() {

		if (dabbleString != null) {

			if(!getIntent().getBooleanExtra("continue", false)) {
				dabbleArray = dabbleString.toCharArray();
				Random rand = new Random();
				for (int i = 1; i < 18; i++) {
					int index = rand.nextInt(17);
					char a = dabbleArray[index];
					dabbleArray[index] = dabbleArray[i];
					dabbleArray[i] = a;
				}
			}
			
			for (int j = 1; j < 19; j++) {
				int resId = getResources().getIdentifier(
						"tile" + Integer.toString(j), "id", getPackageName());
				Tile mTile = (Tile) findViewById(resId);
				mTile.setCharacter(String.valueOf(dabbleArray[j - 1]));
			}
			//Log.d("dabble", String.valueOf(dabbleArray));
		}

		Tile.setGameActivity(this);
		new WordLookUpTask(GameActivity.this).execute(dabbleArray);
		
		Log.d("dabble", "after init: dabbleString:" + dabbleString  + "  dabbleArray:" + String.valueOf(dabbleArray));
		
		TextView dicTitle = (TextView)findViewById(R.id.game_title);
		dicTitle.setText("Go!");
		
		myCountDownTimer.start();
		initing = false;
	}

	public void onClickTiles(Tile tile) {

		Tile currentTile = (Tile) findViewById(R.id.tile1);

		//Log.d("dabble", "onclick" + (currentTile == tile ? "yes" : "no"));
		char[] cpDabbleArray = dabbleArray.clone();
		

		// Vibrate for 400 milliseconds
		vibrator.vibrate(50);
		
		if (clickCount == 1 | clickCount == 0) {

			if (!tile.choosed) {
				tile.setBorderColor(Color.YELLOW);
				tile.choosed = true;
				clickTileId[clickCount] = tile.getIntegerId();
				clickCount++;
			} else {
				tile.setBorderColor(Color.BLUE);
				tile.choosed = false;
				clickCount--;
				clickTileId = new int[2];
			}
		}

		if (clickCount == 2) {
			// Log.d("dabble", "game activity: tile id:" + tile.getIntegerId() +
			// " clickCount:" + clickCount + " tileArray:" + clickTileId[0] +
			// ":" + clickTileId[1]);
			int resId1 = getResources().getIdentifier(
					"tile" + Integer.toString(clickTileId[1] + 1), "id",
					getPackageName());
			Tile tmp1 = (Tile) findViewById(resId1);

			int resId0 = getResources().getIdentifier(
					"tile" + Integer.toString(clickTileId[0] + 1), "id",
					getPackageName());
			Tile tmp0 = (Tile) findViewById(resId0);

			//Log.d("dabble", "before:dabblearray:" + String.valueOf(dabbleArray));

			String c0 = tmp0.getCharacter();
			tmp0.setCharacter(tmp1.getCharacter());
			dabbleArray[tmp0.getIntegerId()] = tmp1.getCharacter().charAt(0);
			tmp1.setCharacter(c0);
			dabbleArray[tmp1.getIntegerId()] = c0.charAt(0);

			//Log.d("dabble", "after:dabblearray:" + String.valueOf(dabbleArray));

			clickCount = 0;
			clickTileId = new int[2];

			tmp0.choosed = false;
			tmp1.choosed = false;
			tmp0.setBorderColor(Color.BLUE);
			tmp1.setBorderColor(Color.BLUE);

			
			new WordLookUpTask(GameActivity.this).execute(cpDabbleArray);
		}

	}
	
	public void playCountDownSound() {
		if(sp!=null && tickStreamId!=0) {
			sp.play(tickStreamId, 1, 1, 0, 0, 1);
			countDownShouldPlay = true;
		}
	}
	
	
	public void playBonusSound() {
		if(sp!=null && beepStreamId!=0 )
			sp.play(beepStreamId, 1, 1, 0, 0, 1);
	}
	
	public void updateUI(int[] colorResult) {
//		Log.d("dabble", "updateTileColor");
	
		
		score = 0;
		int color;
		for (int j = 1; j< 19 ;j++) {
			color = colorResult[j-1];
			int resId = getResources().getIdentifier("tile" + j, "id",
					getPackageName());
			Tile tmp = (Tile) findViewById(resId);
			tmp.setCharacterColor(color);
			if( color == Color.GREEN)
				score++;
		}
		
		if(colorResult[18] == 1)
			playBonusSound();
		
		TextView scoreText = (TextView)findViewById(R.id.score_text);
		if(score == 18) {
			score = (int) (18 + myCountDownTimer.timeRemaining);
		}
		
		scoreText.setText("Score:" + score);
		
		if(score >= 18)
			initGameOver();
		
	}

	
	public void initGameOver() {
		myCountDownTimer.cancel();
		if(sp!=null && beepStreamId!=0 && countDownShouldPlay) {
			sp.stop(tickStreamId);
			countDownShouldPlay = false;
			MyCountDownTimer.countDownPlayed = false;
		}
		
		Intent i = new Intent();
		i.setClass(this, GameOver.class);
		i.putExtra("score", score);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
		
		Prefs.setHighScore(getBaseContext(), score);
		Music.musicShouldPause = false;
		
		Prefs.setSaved(getBaseContext(), false);
		
		finish();
	}
	
	public void blinkHintTile(ArrayList<Tile> tiles) {
		boolean blink = false;
		new MyCountDownTimerHint(this, 600, 120, tiles).start();
	}
	
	
	
	
	// Listener defined by anonymous inner class.
		public OnSharedPreferenceChangeListener mListener = new OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(
					SharedPreferences sharedPreferences, String key) {
				if (key.equals("music")) {
					boolean music = sharedPreferences.getBoolean(key, true);
					if (!music) {
						Music.stop(GameActivity.this);
					} else {
						Music.play(GameActivity.this, R.raw.background);
					}
				}
			}
		};
		
		
		
		
	public void onClickSettingsButton(View v) {
		Intent i = new Intent();
		i.setClass(GameActivity.this, Prefs.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
		Music.musicShouldPause = false;
	}
	
	public void onClickPauseButton(View v) {
		Intent i = new Intent();
		i.setClass(GameActivity.this, PauseActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
		Music.musicShouldPause = false;
	}
}

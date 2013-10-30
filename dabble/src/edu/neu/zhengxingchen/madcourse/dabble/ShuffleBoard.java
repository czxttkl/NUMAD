package edu.neu.zhengxingchen.madcourse.dabble;

import java.util.Random;

import edu.neu.zhengxingchen.madcourse.dabble.game.GameActivity;
import edu.neu.zhengxingchen.madcourse.dabble.game.Tile;
import edu.neu.zhengxingchen.madcourse.dabble.helper.Global;
import edu.neu.zhengxingchen.madcourse.dabble.helper.LoadBeepTask;
import edu.neu.zhengxingchen.madcourse.dabble.helper.LoadShakeBeepTask;
import edu.neu.zhengxingchen.madcourse.dabble.helper.MyGameCountDownTimer;
import edu.neu.zhengxingchen.madcourse.dabble.helper.MyShuffleCountDownTimer;
import edu.neu.zhengxingchen.madcourse.dabble.helper.WordLookUpTask;
import edu.neu.zhengxingchen.madcourse.dabble.twoplayer.PutValueTaskShuffleBoard;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class ShuffleBoard extends Activity implements SensorEventListener{
	public String dabbleString = null;
	public char[] dabbleArray = new char[18];

	public long startTime = 10 * 1000;
	public long interval = 70;
	public int score = 0;
	MyShuffleCountDownTimer myCountDownTimer;

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private long lastUpdateTime = -1;
	private float lastX=-1.0f, lastY=-1.0f, lastZ=-1.0f;
	private Random rand;
	
	public SoundPool sp = null;
	public int beepStreamId = 0;
	
	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
		
		if (Music.musicShouldPause) {
			Music.pause(this);
			Music.musicPaused = true;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener( this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		
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
		mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER);
		rand = new Random();
		
		if (beepStreamId == 0 || sp == null )
			new LoadShakeBeepTask(this).execute();
	}

	public void changeTiles() {
		
			int index1 = rand.nextInt(17);
			int index2 = rand.nextInt(17);
 			char a = dabbleArray[index1];
 			char b = dabbleArray[index2];
			dabbleArray[index1] = b;
			dabbleArray[index2] = a;
			
			int resId1 = getResources().getIdentifier(
					"sb_tile" + Integer.toString(index1+1), "id", getPackageName());
			int resId2 = getResources().getIdentifier(
					"sb_tile" + Integer.toString(index2+1), "id", getPackageName());
			
			Tile mTile1 = (Tile) findViewById(resId1);
			Tile mTile2 = (Tile) findViewById(resId2);
			mTile1.setCharacter(String.valueOf(b));
			mTile2.setCharacter(String.valueOf(a));
			
			Log.d("shuffleboard", "changetile");
			
			if(sp!=null && beepStreamId!=0 )
				sp.play(beepStreamId, 1, 1, 0, 0, 1);
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

		myCountDownTimer.start();
	}

	public void initGameStart() {
		new PutValueTaskShuffleBoard(this, PutValueTaskShuffleBoard.GET_SHUFFLED_STRING).execute();
	}
	
	public void afterInitGameStart(String dabbleArray) {
		Intent i = new Intent();
		i.setClass(this, GameActivity.class);
		i.putExtra("syncMode", "sync");
		i.putExtra("dabbleArray", dabbleArray);
		startActivity(i);
		finish();
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
			if(lastUpdateTime == -1) {
				lastUpdateTime = System.currentTimeMillis();
				float[] xyz = arg0.values;
				float x = xyz[0];
				float y = xyz[1];
				float z = xyz[2];
				lastX = x;
				lastY = y;
				lastZ = z;
				return;
			}
			else {
				long currentTime = System.currentTimeMillis();
				if(currentTime - lastUpdateTime > 500) {
					long diffTime = currentTime - lastUpdateTime;
					float[] xyz = arg0.values;
					float x = xyz[0];
					float y = xyz[1];
					float z = xyz[2];
					float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000;
					Log.d("shuffleboard", "shaked:" + speed + " x:" + x + " y:" + y + " z:" + z + " diffTime:" + diffTime);
					if (speed > 100) {
						Log.d("shuffleboard", "one shake");
						changeTiles();
					}
					lastX = x;
					lastY = y;
					lastZ = z;
					lastUpdateTime = currentTime;
				}
			}
		}



}

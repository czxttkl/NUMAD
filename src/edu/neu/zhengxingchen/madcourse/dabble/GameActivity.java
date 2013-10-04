package edu.neu.zhengxingchen.madcourse.dabble;

import java.io.IOException;
import java.util.Random;

import android.graphics.Color;
import android.media.SoundPool;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GameActivity extends Activity {

	public String dabbleString = null;
	public char[] dabbleArray = new char[18];
	public String[] wholeArray = null;
//	public Tile[] tileArray = null;
	protected SoundPool sp = null;
	protected int beepStreamId = 0;

	public int clickCount = 0;
	public int[] clickTileId = new int[2];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_game);

		if (savedInstanceState != null) {
			loadWhole(savedInstanceState);
		}

		Object[] myObjectData = (Object[]) getLastNonConfigurationInstance();
		if (myObjectData != null) {
//			tileArray = (Tile[]) myObjectData[0];
			sp = (SoundPool) myObjectData[1];
			//Log.d("dabble", "restore:" + tileArray[3].getCharacter());
			restoreTile();
		}

		LinearLayout gameLayout = (LinearLayout) findViewById(R.id.game_layout);

		try {
			if (wholeArray == null || dabbleString == null)
				new LoadDicTask(this).execute(
						getResources().getAssets().open("short_wordlist.txt"),
						getResources().getAssets().open("wordlist.txt"));

			if (beepStreamId == 0 || sp == null)
				new LoadBeepTask(this).execute();

		} catch (IOException e) {
			e.printStackTrace();
		}

		Log.d("dabble", "oncreate");
		// Tile mTile = (Tile)findViewById(R.id.tile10);
		// Log.d("dabble", (mTile == null ? "yes" : "no"));
		//

	}

	private void loadWhole(Bundle savedInstanceState) {
		wholeArray = savedInstanceState.getStringArray("wholeArray");
		dabbleArray = savedInstanceState.getCharArray("dabbleArray");
		beepStreamId = savedInstanceState.getInt("beepStreamId");
		dabbleString = savedInstanceState.getString("dabbleString");
		clickCount = savedInstanceState.getInt("clickCount");
		// tileArray = (Tile[]) savedInstanceState.getSerializable("tileArray");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveWhole(outState);
	}

	private void saveWhole(Bundle outState) {
		outState.putString("dabbleString", "dabbleString");
		outState.putCharArray("dabbleArray", dabbleArray);
		outState.putStringArray("wholeArray", wholeArray);
		outState.putInt("beepStreamId", beepStreamId);
		outState.putInt("clickCount", clickCount);
		// outState.putSerializable("tileArray", tileArray);

	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		final Object[] myObjectData = new Object[2];
//		myObjectData[0] = tileArray;
		myObjectData[1] = sp;
		return myObjectData;
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("dabble", "onpause");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d("dabble", "onrestart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("dabble", "onresume");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game, menu);
		return true;
	}

	public void restoreTile() {
		Log.d("dabble", "restoretile");
//		for (int j = 1; j < 19; j++) {
//			int resId = getResources().getIdentifier(
//					"tile" + Integer.toString(j), "id", getPackageName());
//			Tile mTile = (Tile) findViewById(resId);
//			mTile = tileArray[j-1];
//			Log.d("dabble", "restoreTile1" + (tileArray[0] == (Tile)findViewById(R.id.tile1)? "yes":"no"));
//			mTile.invalidate();
//		}
//		int resId = getResources().getIdentifier(
//				"tile1", "id", getPackageName());
//		Tile mTile = (Tile) findViewById(resId);
		
		Tile.setGameActivity(this);
//		Log.d("dabble", "restoreTile2" + (tileArray[0] == mTile? "yes":"no"));
	}
	
	public void initialTile() {

//		tileArray = new Tile[18];
		
		if (dabbleString != null) {

			dabbleArray = dabbleString.toCharArray();

			Random rand = new Random();

			for (int i = 1; i < 18; i++) {
				int index = rand.nextInt(17);
				char a = dabbleArray[index];
				dabbleArray[index] = dabbleArray[i];
				dabbleArray[i] = dabbleArray[index];
			}

			for (int j = 1; j < 19; j++) {
				int resId = getResources().getIdentifier(
						"tile" + Integer.toString(j), "id", getPackageName());
				Tile mTile = (Tile) findViewById(resId);
				mTile.setCharacter(String.valueOf(dabbleArray[j - 1]));
//				tileArray[j - 1] = mTile;
			}
			Log.d("dabble", String.valueOf(dabbleArray));
		}

		Tile.setGameActivity(this);
	}

	public void onClickTiles(Tile tile) {

		Tile currentTile = (Tile)findViewById(R.id.tile1);
		
		Log.d("dabble", "onclick" + (currentTile == tile? "yes":"no"));
		
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
//			Log.d("dabble", "game activity: tile id:" + tile.getIntegerId() + " clickCount:" + clickCount + " tileArray:" + clickTileId[0] + ":" + clickTileId[1]);
			int resId1 = getResources().getIdentifier(
					"tile" + Integer.toString(clickTileId[1]+1), "id", getPackageName());
			Tile tmp1 = (Tile)findViewById(resId1);
			
			int resId0 = getResources().getIdentifier(
					"tile" + Integer.toString(clickTileId[0]+1), "id", getPackageName());
			Tile tmp0 = (Tile)findViewById(resId0);
			
			
			
			
//			Tile tmp1 = tileArray[clickTileId[1]];
//			Tile tmp0 = tileArray[clickTileId[0]];
			
			Log.d("dabble", "before:tilearray:" + tmp1.getIntegerId() + " :" + tmp1.getCharacter());
			
			String c0 = tmp0.getCharacter();
			tmp0.setCharacter(tmp1.getCharacter());
			tmp1.setCharacter(c0);
			
			Log.d("dabble", "after:tilearray:" + tmp1.getIntegerId() + " :" + tmp1.getCharacter());
			
			clickCount = 0;
			clickTileId = new int[2];
			
			tmp0.choosed = false;
			tmp1.choosed = false;
			tmp0.setBorderColor(Color.BLUE); 
			tmp1.setBorderColor(Color.BLUE); 
			
			
		}
		

	}

}

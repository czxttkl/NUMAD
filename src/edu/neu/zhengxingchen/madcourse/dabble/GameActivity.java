package edu.neu.zhengxingchen.madcourse.dabble;

import java.io.IOException;
import java.util.Random;

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
	protected SoundPool sp = null;
	protected int beepStreamId = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_game);
		LinearLayout gameLayout = (LinearLayout) findViewById(R.id.game_layout);
		
		try {
			new LoadDicTask(this).execute(getResources().getAssets().open("short_wordlist.txt"), getResources().getAssets().open("wordlist.txt"));
			new LoadBeepTask(this).execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
//		Tile mTile = (Tile)findViewById(R.id.tile10);
//		Log.d("dabble", (mTile == null ? "yes" : "no"));
//		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game, menu);
		return true;
	}

	public void initialTile() {
		
		if (dabbleString != null) {
			
			dabbleArray = dabbleString.toCharArray();
			
			Random rand = new Random();
			
			for (int i = 1; i < 18 ; i++) {
				int index = rand.nextInt(17);
				char a = dabbleArray[index];
				dabbleArray[index] = dabbleArray[i];
				dabbleArray[i] = dabbleArray[index];
				}
			
		
		 	for (int j = 1; j < 19 ; j++) {
		 		int resId = getResources().getIdentifier("tile" + Integer.toString(j), "id", getPackageName());
		 		Tile mTile = (Tile)findViewById(resId);
		 		mTile.setCharacter(String.valueOf(dabbleArray[j-1]));
		 	}
		 	Log.d("dabble", String.valueOf(dabbleArray));
		}
	}
	
	
}

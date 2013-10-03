package edu.neu.zhengxingchen.madcourse.dabble;

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

	public String[] tiles = {"a", "b", "c", "d", "e", "f", "g"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		TileGroup tileGroup = new TileGroup(this);
//		RelativeLayout gameLayout = (RelativeLayout) findViewById(R.id.game_layout);
//		RelativeLayout gameLayout = new RelativeLayout(this);
//		
//		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//		Log.d("dabble", (gameLayout == null? "yes" : "no"));
//		
//		gameLayout.addView(mTile);
		setContentView(R.layout.activity_game);
		LinearLayout gameLayout = (LinearLayout) findViewById(R.id.game_layout);
//		Log.d("dabble", (gameLayout == null? "yes" : "no"));
//		Log.d("dabble", gameLayout.getLayoutParams().height + ":height");
//		
//		Tile mTile1 = new Tile(this);
//		gameLayout.addView(mTile1);
//		mTile1.getLayoutParams().height = 33;
//		
//		Tile mTile2 = new Tile(this);
//		gameLayout.addView(mTile2);
//		mTile1.getLayoutParams().height = 99;
		
	/*	LinearLayout tileRow1 = new LinearLayout(this);
		
		tileRow1.setOrientation(LinearLayout.HORIZONTAL);  
		tileRow1.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));  
		tileRow1.setGravity(Gravity.CENTER_HORIZONTAL); 
		//tileRow1.setGravity(Gravity.CENTER_HORIZONTAL);
		
		Tile mTile11 = new Tile(this);
		Tile mTile12 = new Tile(this);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		TextView a = new TextView(this);
		a.setText("dasf");
		
		
		TextView tv1 = new TextView(this);  
	    tv1.setText("FIRST");  
	    tv1.setTextSize(100);  
	    tv1.setGravity(Gravity.CENTER);  
	    TextView tv2 = new TextView(this);  
	    tv2.setTextSize(100);  
	    tv2.setGravity(Gravity.CENTER);  
	    tv2.setText("MIDDLE");  
	    TextView tv3 = new TextView(this);  
	    tv3.setTextSize(100);  
	    tv3.setGravity(Gravity.CENTER);  
	    tv3.setText("LAST");  
	    
	    tileRow1.addView(mTile11);  
	    tileRow1.addView(tv2);  
	    tileRow1.addView(tv3);  
	 
	    
		
		tileRow1.addView(mTile12,  params);
	

		gameLayout.addView(tileRow1);*/
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game, menu);
		return true;
	}

}

package edu.neu.zhengxingchen.madcourse.dabble;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class GameOver extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_over);
		
		
		
		//Log.d("dabble", "gameover score:" + Prefs.getHighScore(getBaseContext()) );
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent i = getIntent();
//		String yourScoreString = String.valueOf();
		
		TextView yourScore = (TextView)findViewById(R.id.your_score);
		TextView highScore = (TextView)findViewById(R.id.high_score);
	
		yourScore.setText(String.valueOf(i.getIntExtra("score", 0)));
		highScore.setText(String.valueOf(Prefs.getHighScore(getBaseContext())));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game_over, menu);
		return true;
	}
	
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		finish();
//		return true;
//	}
	public void onClickMainMenu(View view) {
		Intent i = new Intent();
		i.setClass(this, GameMenu.class);
		startActivity(i);
		finish();
	}
	
	public void onClickPlayAgain(View view) {
		Intent i = new Intent();
		i.setClass(this, GameActivity.class);
		startActivity(i);
		Music.play(getBaseContext(), R.raw.background);
		finish();
	}

}

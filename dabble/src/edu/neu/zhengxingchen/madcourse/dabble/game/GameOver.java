package edu.neu.zhengxingchen.madcourse.dabble.game;

import edu.neu.zhengxingchen.madcourse.dabble.GameMenu;
import edu.neu.zhengxingchen.madcourse.dabble.Music;
import edu.neu.zhengxingchen.madcourse.dabble.Prefs;
import edu.neu.zhengxingchen.madcourse.dabble.R;
import edu.neu.zhengxingchen.madcourse.dabble.R.id;
import edu.neu.zhengxingchen.madcourse.dabble.R.layout;
import edu.neu.zhengxingchen.madcourse.dabble.R.menu;
import edu.neu.zhengxingchen.madcourse.dabble.R.raw;
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
		
		if( Music.musicPaused) {
			Music.start(this);
			Music.musicPaused = false;
		}
		Music.musicShouldPause = true;
		
		TextView yourScore = (TextView)findViewById(R.id.your_score);
		TextView highScore = (TextView)findViewById(R.id.high_score);
		
		yourScore.setText(String.valueOf(i.getIntExtra("score", 0)));
		int rivalScore = i.getIntExtra("rivalScore", -1);
		if(  rivalScore  < 0 ) {
			//Single mode
			highScore.setText(String.valueOf(Prefs.getHighScore(getBaseContext())));
		} else {
			//Two player mode
			TextView highScoreTitle = (TextView)findViewById(R.id.high_score_title);
			highScoreTitle.setText("Rival's Score");
			highScore.setText(String.valueOf(rivalScore));
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(Music.musicShouldPause && !Music.musicPaused) {
			Music.pause(this);
			Music.musicPaused = true;
		}
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
//		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//
		i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(i);
		finish();
	}
	
	public void onClickPlayAgain(View view) {
		Intent i = new Intent();
		i.setClass(this, GameActivity.class);
//		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(i);
		Music.play(getBaseContext(), R.raw.background);
		finish();
	}

}

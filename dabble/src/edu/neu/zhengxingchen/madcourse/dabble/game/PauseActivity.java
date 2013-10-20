package edu.neu.zhengxingchen.madcourse.dabble.game;

import edu.neu.zhengxingchen.madcourse.dabble.GameMenu;
import edu.neu.zhengxingchen.madcourse.dabble.R;
import edu.neu.zhengxingchen.madcourse.dabble.R.layout;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;

public class PauseActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pause);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		finish();
		return true;
	}
	
	public void onClickBackToMenu(View v) {
		Intent i = new Intent();
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.setClass(this, GameMenu.class);
		startActivity(i);
	}

}

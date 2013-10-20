package edu.neu.zhengxingchen.madcourse.dabble.helper;

import java.util.ArrayList;

import edu.neu.zhengxingchen.madcourse.dabble.game.GameActivity;
import edu.neu.zhengxingchen.madcourse.dabble.game.Tile;

import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

public class MyHintCountDownTimer extends CountDownTimer {

	GameActivity gameActivity;
	public boolean blink = false;
	ArrayList<Tile> tileHints = null;

	public MyHintCountDownTimer(long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
		// TODO Auto-generated constructor stub
	}

	public MyHintCountDownTimer(GameActivity gameActivity, long millisInFuture,
			long countDownInterval, ArrayList<Tile> tileHints) {
		super(millisInFuture, countDownInterval);
		this.gameActivity = gameActivity;
		this.tileHints = tileHints;
	}

	@Override
	public void onFinish() {
		for (Tile mTile : tileHints) {
				mTile.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onTick(long millisUntilFinished) {

		for (Tile mTile : tileHints) {
			// Log.d("dabble", "blinkHintTile:" + mTile.getCharacter());
			if (!blink) {
				mTile.setVisibility(View.INVISIBLE);
				// mTile.setBorderColor(Color.YELLOW);
			} else {
				// mTile.setBorderColor(Color.BLUE);
				mTile.setVisibility(View.VISIBLE);
			}
		}
 
		blink = !blink;

	}

}

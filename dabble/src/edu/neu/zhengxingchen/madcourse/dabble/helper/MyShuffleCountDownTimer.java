package edu.neu.zhengxingchen.madcourse.dabble.helper;

import edu.neu.zhengxingchen.madcourse.dabble.Prefs;
import edu.neu.zhengxingchen.madcourse.dabble.R;
import edu.neu.zhengxingchen.madcourse.dabble.R.id;
import edu.neu.zhengxingchen.madcourse.dabble.ShuffleBoard;
import edu.neu.zhengxingchen.madcourse.dabble.game.GameActivity;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MyShuffleCountDownTimer extends CountDownTimer {

	ShuffleBoard sb;
	TextView timer;
	public boolean blink = false;
	public int countHint = 0;
	public int tempScore = 0;
	public long timeRemaining = 0;

	public MyShuffleCountDownTimer(long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
	}

	public MyShuffleCountDownTimer(ShuffleBoard sb, long millisInFuture,
			long countDownInterval) {
		super(millisInFuture, countDownInterval);
		this.sb = sb;
		timer = (TextView) sb.findViewById(R.id.sb_timer_text);
	}

	@Override
	public void onFinish() {
		timer.setText("0:00.000");
		sb.initGameStart();
	}

	@Override
	public void onTick(long millisUntilFinished) {

		timeRemaining = millisUntilFinished / 1000;
		sb.startTime = millisUntilFinished;
		
		long min = millisUntilFinished / 60000;
		long sec = (millisUntilFinished % 60000) / 1000;
		long millisec = millisUntilFinished % 1000;

		StringBuilder sb = new StringBuilder();
		sb.append(min);
		sb.append(":");
		sb.append(sec / 10);
		sb.append(sec % 10);
		sb.append(":");
		sb.append(millisec / 100);
		sb.append((millisec % 100) / 10);
		sb.append(millisec % 10);

		timer.setText(sb.toString());

		if (blink) {
			timer.setVisibility(View.VISIBLE);
			timer.setTextColor(Color.RED);
			// if blink is true, textview will be visible
		} else {
			timer.setVisibility(View.INVISIBLE);
		}

		blink = !blink; // toggle the value of blink

	}

}

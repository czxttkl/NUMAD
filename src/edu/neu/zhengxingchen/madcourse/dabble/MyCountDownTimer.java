package edu.neu.zhengxingchen.madcourse.dabble;

import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;

public class MyCountDownTimer extends CountDownTimer{

	GameActivity gameActivity;
	
	public MyCountDownTimer(long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
	}
	public MyCountDownTimer(GameActivity gameActivity, long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
		this.gameActivity = gameActivity;
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTick(long millisUntilFinished) {
		// TODO Auto-generated method stub
		gameActivity.startTime = millisUntilFinished;
		TextView timer = (TextView)gameActivity.findViewById(R.id.timer_text);
		timer.setText("" + millisUntilFinished);
		//Log.d("countdown", "" + millisUntilFinished);
	}

}

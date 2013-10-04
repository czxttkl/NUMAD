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
		TextView timer = (TextView)gameActivity.findViewById(R.id.timer_text);
		timer.setText("0:00.000");
	}

	@Override
	public void onTick(long millisUntilFinished) {
		// TODO Auto-generated method stub
		gameActivity.startTime = millisUntilFinished;
		TextView timer = (TextView)gameActivity.findViewById(R.id.timer_text);
		long min = millisUntilFinished/60000;
		long sec = (millisUntilFinished % 60000 ) / 1000; 
		long millisec = millisUntilFinished % 1000;
	    
		StringBuilder sb = new StringBuilder();
		sb.append(min);
		sb.append(":");
		sb.append(sec/10);
		sb.append(sec%10);
		sb.append(":");
		sb.append(millisec/100);
		sb.append( (millisec%100)/10 );
		sb.append(millisec%10);
		
		timer.setText(sb.toString());
		//Log.d("countdown", "" + millisUntilFinished);
	}

}

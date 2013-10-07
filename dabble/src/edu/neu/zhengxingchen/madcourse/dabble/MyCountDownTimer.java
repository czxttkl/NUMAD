package edu.neu.zhengxingchen.madcourse.dabble;

import android.graphics.Color;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MyCountDownTimer extends CountDownTimer{

	GameActivity gameActivity;
	public boolean blink = false;
	public int countHint = 0;
	public int tempScore = 0;
	public long timeRemaining = 0;
	
	public MyCountDownTimer(long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
	}
	public MyCountDownTimer(GameActivity gameActivity, long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
		this.gameActivity = gameActivity;
		Log.d("dabble", "mycountdowntimer in future millis:" + millisInFuture);
	}

	@Override
	public void onFinish() {
		TextView timer = (TextView)gameActivity.findViewById(R.id.timer_text);
		timer.setText("0:00.000");
		gameActivity.initGameOver();
		gameActivity.startTime = 0;
	}

	@Override
	public void onTick(long millisUntilFinished) {

		timeRemaining = millisUntilFinished/1000;
		
		if(timeRemaining < 5) {
			gameActivity.playCountDownSound();
		}
		
		
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
		
		if ( millisUntilFinished < 5000 ) {

            if ( blink ) {
            	timer.setVisibility(View.VISIBLE);
            	timer.setTextColor(Color.RED);
                // if blink is true, textview will be visible
            } else {
            	timer.setVisibility(View.INVISIBLE);
            }

            blink = !blink;         // toggle the value of blink
        } 
		
		if(gameActivity.score > tempScore) {
			tempScore = gameActivity.score;
			countHint = 0;
		}
		
		countHint++;
		
		if(countHint == 50) {
			if(Prefs.getHints(gameActivity.getBaseContext())){
//				Log.d("dabble", "show hint");
				new ShowHintTask(gameActivity).execute();
			}
				countHint = 0;
		}
		
		
		
	}

}

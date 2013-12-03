package edu.neu.mhealth.debug.helper;

import java.util.Observable;
import java.util.Observer;

import android.R.integer;
import android.content.Context;
import android.util.Log;

public class JumpBug extends Bug implements Observer{
	
	private static final String TAG = "JumpBug"; 

	public JumpBug(Context context) {
		super(context);
	}

	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub
		
		/// if phone moves
		if (observable instanceof MotionEventListener) {
			MotionMetrics motion = (MotionMetrics)data;
		}
		
		/// if user jumps
		if (observable instanceof AccEventListener) {
			Log.e(TAG, "I get the jumping notification");
		}
		
		/// if game mode changed
		if (observable instanceof ModeManager.ModeEventListener) {
			Integer gameMode = (Integer)data;
		}
	}
}

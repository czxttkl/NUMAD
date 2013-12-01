package edu.neu.mhealth.debug.helper;

import java.util.Observable;
import java.util.Observer;

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
		Log.i(TAG, "I get the jumping notification");
	}
}

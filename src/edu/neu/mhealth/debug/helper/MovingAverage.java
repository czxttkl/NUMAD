package edu.neu.mhealth.debug.helper;

import android.util.Log;

public class MovingAverage {

	private static final String TAG = "MovingAverage";
	private int circularBuffer[];
	private int avg;
	private int circularIndex;
	private int count;

	public MovingAverage(int k) {
		circularBuffer = new int[k];
		count = 0;
		circularIndex = 0;
		avg = 0;
	}

	public int getValue() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < circularBuffer.length; i++) {
			stringBuilder.append(circularBuffer[i] + " ");
		}
		System.out.println("Value before average:  " + stringBuilder);
		Log.e(TAG, "Value after average: " + avg);
		return avg;
	}

	public void pushValue(int x) {
		if (count++ == 0) {
			primeBuffer(x);
		}
		int lastValue = circularBuffer[circularIndex];
		avg = avg + (x - lastValue) / circularBuffer.length;
		circularBuffer[circularIndex] = x;
		circularIndex = nextIndex(circularIndex);
	}

	public int getCount() {
		return count;
	}

	private void primeBuffer(int val) {
		for (int i = 0; i < circularBuffer.length; i++) {
			circularBuffer[i] = val;
		}
		avg = val;
	}

	private int nextIndex(int curIndex) {
		if (curIndex + 1 >= circularBuffer.length) {
			return 0;
		}
		return curIndex + 1;
	}
}

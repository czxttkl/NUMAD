package edu.neu.mhealth.debug;

import android.R.integer;
import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;

public class Bug extends ImageButton {

	private int ID;
	private int accX = 0;
	private int accY = 0;

	public Bug(Context context) {
		super(context);
		accX = 15;
		accY = 20;
	}

	public Bug(Context context, int accx, int accy) {
		super(context);
		accX = accx;
		accY = accy;
		this.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		this.setBackgroundColor(Color.TRANSPARENT);
	}

	public void crawl() {
		this.offsetLeftAndRight(accX);
		this.offsetTopAndBottom(accY);
		int[] loc = new int[2];
		this.getLocationInWindow(loc);
		if(loc[0] > 1000 || loc[0] < 0) {
			accX *= -1;
		}
		if (loc[1] > 700 || loc[1] < 0) {
			accY *= - 1;
		}
	}
}

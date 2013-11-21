package edu.neu.mhealth.debug;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class ConfigureView extends View {
	private static final String TAG = "ConfigureView: ";
	
	private static final int rectWidth = 500;
	private static final int rectHeight = 250;
	private static final int squareMetric = 200;
	private boolean mPause;
	private Paint mPaint;
	private int screenHeight;
	private int screenWidth;
	
	public ConfigureView(Context context, int width, int height) {
		super(context);
		// TODO Auto-generated constructor stub
		screenHeight = height;
		screenWidth = width;
		mPause = false;
		mPaint = new Paint();
		this.setBackgroundColor(Color.TRANSPARENT);
		this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		if (!mPause) {
			mPaint.setColor(Color.RED);
			mPaint.setStrokeWidth(10);
			mPaint.setStyle(Style.STROKE);

			canvas.drawRect(screenWidth/2 - rectWidth/2, screenHeight-rectHeight, screenWidth/2 + rectWidth/2, screenHeight, mPaint);
			
			mPaint.setColor(Color.GREEN);
			canvas.drawRect(screenWidth/2 - squareMetric/2, screenHeight-rectHeight-squareMetric, screenWidth/2 + squareMetric/2, screenHeight-rectHeight, mPaint);
		}
	}
	
	public void disableDrawing() {
		mPause = true;
	}
	
	public void enableDrawing() {
		mPause = false;
	}
	
	public Point getFloorPosition() {
		Point floorPoint = new Point(screenWidth/2, screenHeight-rectHeight-squareMetric/2);	
		return floorPoint;
	}
	
	public Point getShoesPosition() {
		Point shoesPoint = new Point(screenWidth/2, screenHeight-rectHeight/2);
		return shoesPoint;
	}
}

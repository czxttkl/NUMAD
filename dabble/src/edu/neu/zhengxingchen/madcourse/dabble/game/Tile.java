package edu.neu.zhengxingchen.madcourse.dabble.game;

import edu.neu.zhengxingchen.madcourse.dabble.R;
import edu.neu.zhengxingchen.madcourse.dabble.R.styleable;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class Tile extends View {
	
	private String mCharacter = "a"; // TODO: use a default from R.string...
	private int mCharacterColor = Color.RED;
	private int mBorderColor = Color.BLUE; // R.color...
	private int mBorderRadius = 30;
//	private float mCharacterSize = 1;

	private RectF mRect;
	private Paint mRectPaint;
	private Paint mTextPaint;

	private static int parentWidth = 0;
	private static int parentHeight = 0;

	private boolean measureLock = false;
	boolean choosed = false;

	private GestureDetector mDetector;

	private static GameActivity gameActivity = null; 
	
	public Tile(Context context) {
		super(context);
		init(null, 0);
	}

	public Tile(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public Tile(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	private void init(AttributeSet attrs, int defStyle) {
		// Load attributes
		
//		Log.d("dabble", getIntegerId() + ":Tile init");
		
		final TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.Tile, defStyle, 0);

		try {
			
			 mCharacter = a.getString(R.styleable.Tile_character);
			mCharacterColor = a.getColor(R.styleable.Tile_characterColor,
					mCharacterColor);
//			mBackgroundColor = a.getColor(R.styleable.Tile_backgroundColor,
//					mBackgroundColor);
			mBorderColor = a.getColor(R.styleable.Tile_borderColor,
					mBorderColor);
			mBorderRadius = a.getInteger(R.styleable.Tile_borderRadius,
					mBorderRadius);
//			mCharacterSize = a.getDimension(R.styleable.Tile_characterSize,
//					mCharacterSize);

		} finally {

			a.recycle();
		}

		mDetector = new GestureDetector(this.getContext(),
				new GestureListener());

		// Update TextPaint and text measurements from attributes
		invalidateTextPaintAndMeasurements();
	}

	private void invalidateTextPaintAndMeasurements() {
		mTextPaint = new Paint();
		mTextPaint.setColor(mCharacterColor);
		mTextPaint.setTextSize(48);
//		mTextPaint.setTextSize(mCharacterSize);
//		mTextPaint.setColor(mCharacterColor);
		mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mRectPaint.setStyle(Style.FILL);
		mRectPaint.setColor(mBorderColor);
		mRectPaint.setStrokeWidth(mBorderRadius);
		invalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//
		if (event.getAction() == MotionEvent.ACTION_UP) {
			if(gameActivity!=null) {
				gameActivity.onClickTiles(this);
			}
		}

		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
//		mTextPaint = new Paint();
//		mTextPaint.setColor(Color.RED);
//		mTextPaint.setTextSize(48);
//		mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//		mRectPaint.setStyle(Style.FILL);
//		mRectPaint.setColor(mBorderColor);
//		mRectPaint.setStrokeWidth(mBorderRadius);
		
		
//		Log.d("dabble", getIntegerId() + ":tile.ondraw");
	
		canvas.drawRoundRect(mRect, mBorderRadius, mBorderRadius, mRectPaint);
		canvas.drawText(mCharacter, parentWidth / 20, parentHeight / 9, mTextPaint);

	}

	/**
	 * Sets the view's example string attribute value. In the example view, this
	 * string is the text to draw.
	 * 
	 * @param exampleString
	 *            The example string attribute value to use.
	 */
	public void setCharacter(String exampleString) {
		mCharacter = exampleString;
		invalidateTextPaintAndMeasurements();
	}

	public static void setGameActivity(GameActivity mGameActivity) {
		gameActivity = mGameActivity;
	}
	/**
	 * Sets the view's example color attribute value. In the example view, this
	 * color is the font color.
	 * 
	 * @param exampleColor
	 *            The example color attribute value to use.
	 */
//	public void setBackgroundColor(int exampleColor) {
//		mBackgroundColor = exampleColor;
//		invalidateTextPaintAndMeasurements();
//	}

	public void setBorderColor(int borderColor) {
		mBorderColor = borderColor;
		Log.d("dabbl1", "setbordercolor" + getIntegerId());
		invalidateTextPaintAndMeasurements();
	}

	public void setCharacterColor(int characterColor) {
		mCharacterColor = characterColor;
//		Log.d("dabble", "setCharacterColor:" + getIntegerId() + " : " + characterColor);
		invalidateTextPaintAndMeasurements();
	}
	
	public void setBorderRadius(int borderRadius) {
		mBorderRadius = borderRadius;
		invalidateTextPaintAndMeasurements();
	}
	/**
	 * Sets the view's example dimension attribute value. In the example view,
	 * this dimension is the font size.
	 * 
	 * @param exampleDimension
	 *            The example dimension attribute value to use.
	 */
//	public void setCharacterSize(float exampleDimension) {
//		
//		mCharacterSize = exampleDimension;
//		invalidateTextPaintAndMeasurements();
//	}

	/**
	 * Gets the example string attribute value.
	 * 
	 * @return The example string attribute value.
	 */
	public String getCharacter() {
		return mCharacter;
	}
	
	/**
	 * Gets the example dimension attribute value.
	 * 
	 * @return The example dimension attribute value.
	 */
	public float getCharacterColor() {
		return mCharacterColor;
	}

	/**
	 * Gets the example color attribute value.
	 * 
	 * @return The example color attribute value.
	 */
//	public int getBackgroundColor() {
//		return mBackgroundColor;
//	}
	
	public int getBorderColor() {
		return mBorderColor;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		if (MeasureSpec.getSize(widthMeasureSpec) > parentWidth && !measureLock) {
			parentWidth = MeasureSpec.getSize(widthMeasureSpec);
			parentHeight = MeasureSpec.getSize(heightMeasureSpec);
			measureLock = true;
		}
		// Log.d("dabble","parent:" + parentWidth + ":" + parentHeight);
		this.setMeasuredDimension(parentWidth / 8, parentHeight / 5);

	}

	// @Override
	// protected void onLayout(boolean changed, int left, int top, int right,
	// int bottom) {
	// // TODO Auto-generated method stub
	// super.onLayout(changed, left, top, right, bottom);
	// if(mRect==null)
	// mRect = new RectF(0, 0, parentWidth/8, parentHeight/5);
	// }

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mRect = new RectF(0, 0, parentWidth / 8, parentHeight / 6);
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
//		Log.d("dabble", getIntegerId() + ":tile.onrestoreinstancestate");

		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
//
//			String restoredCharacter = bundle.getString("mCharacter");
//			setCharacter(restoredCharacter);
//			choosed = bundle.getBoolean("choosed");
//			setBorderColor(bundle.getInt("mBorderColor"));
//			Gson gson = new Gson();
//			mRect = gson.fromJson(bundle.getString("mRect"), RectF.class);
//			mRectPaint = gson.fromJson(bundle.getString("mRectPaint"), Paint.class);
//			mTextPaint = gson.fromJson(bundle.getString("mTextPaint"), Paint.class);
			parentWidth = bundle.getInt("parentWidth");
			parentHeight = bundle.getInt("parentHeight");
			mRect = new RectF(0, 0, parentWidth / 8, parentHeight / 5);
			
			measureLock = bundle.getBoolean("measureLock");
			mCharacter = bundle.getString("mCharacter");
			choosed = bundle.getBoolean("choosed");
			mBorderColor = bundle.getInt("mBorderColor");
			mCharacterColor = bundle.getInt("mCharacterColor");
			setBorderRadius(bundle.getInt("mBorderRadius"));
			
			return;
		}

		super.onRestoreInstanceState(state);
		
		
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Bundle savedState = new Bundle();
		savedState.putParcelable("instanceState", super.onSaveInstanceState());
		savedState.putString("mCharacter", mCharacter);
		savedState.putBoolean("choosed",choosed);
		savedState.putInt("mBorderColor", mBorderColor);
		savedState.putInt("mBorderRadius", mBorderRadius);
		//Log.d("dabble", "tile.onsaveinstancestate");
		savedState.putInt("mCharacterColor", mCharacterColor);
//		savedState.putFloat("mCharacterSize", mCharacterSize);
		savedState.putBoolean("measureLock", measureLock);
		savedState.putInt("parentWidth", parentWidth);
		savedState.putInt("parentHeight", parentHeight);
		
		return savedState;
	}

	private class GestureListener extends
			GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			// Set the pie rotation directly.

			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {

			return true;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			//Log.d("dabble", "ondown");
			return true;
		}
	}

	public int getIntegerId() {
		String stringId = getResources().getResourceName(getId());
		String[] tmp = stringId.split("/");
		
		return Integer.valueOf(tmp[1].substring(4))-1;
	}

}

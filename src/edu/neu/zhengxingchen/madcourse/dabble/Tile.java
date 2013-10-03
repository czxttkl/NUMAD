package edu.neu.zhengxingchen.madcourse.dabble;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextPaint;
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
	private int mBackgroundColor = Color.WHITE; // TODO: use a default from
	private int mBorderColor = Color.BLUE; // R.color...
	private int mBorderRadius = 10;
	private float mCharacterSize = 1;

	private int paddingTop;
	private int paddingBottom;
	private int paddingLeft;
	private int paddingRight;

	private int recWidth;
	private int recHeight;
	private RectF mRect;
	private Paint mRectPaint;

	private TextPaint mTextPaint;
	private float mTextWidth;
	private float mTextHeight;

	private static int parentWidth = 0;
	private static int parentHeight = 0;

	private boolean measureLock = false;
	private boolean choosed = false;

	private GestureDetector mDetector;

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
		final TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.Tile, defStyle, 0);

		try {
			// mCharacter = a.getString(R.styleable.Tile_character);
			mCharacterColor = a.getColor(R.styleable.Tile_characterColor,
					mCharacterColor);
			mBackgroundColor = a.getColor(R.styleable.Tile_backgroundColor,
					mBackgroundColor);
			mBorderColor = a.getColor(R.styleable.Tile_borderColor,
					mBorderColor);
			mBorderRadius = a.getInteger(R.styleable.Tile_borderRadius,
					mBorderRadius);
			// Use getDimensionPixelSize or getDimensionPixelOffset when dealing
			// with
			// values that should fall on pixel boundaries.
			mCharacterSize = a.getDimension(R.styleable.Tile_characterSize,
					mCharacterSize);

			// if (a.hasValue(R.styleable.Tile_exampleDrawable)) {
			// mExampleDrawable =
			// a.getDrawable(R.styleable.Tile_exampleDrawable);
			// mExampleDrawable.setCallback(this);
			// }
			paddingLeft = getPaddingLeft();
			paddingTop = getPaddingTop();
			paddingRight = getPaddingRight();
			paddingBottom = getPaddingBottom();

		} finally {

			a.recycle();
		}

		// Set up a default TextPaint object
		mTextPaint = new TextPaint();
		mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTextAlign(Paint.Align.LEFT);

		mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		mDetector = new GestureDetector(this.getContext(),
				new GestureListener());

		// Update TextPaint and text measurements from attributes
		invalidateTextPaintAndMeasurements();
	}

	private void invalidateTextPaintAndMeasurements() {
		mTextPaint.setTextSize(mCharacterSize);
		mTextPaint.setColor(mCharacterColor);
		mTextWidth = mTextPaint.measureText(mCharacter);
		Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
		mTextHeight = fontMetrics.bottom;

		mRectPaint.setStyle(Style.FILL);
		mRectPaint.setColor(mBorderColor);
		mRectPaint.setStrokeWidth(mBorderRadius);
		invalidate();
		// Log.d("dabble", "finishinvalidate");
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//
		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (!choosed) {
				setBorderColor(Color.YELLOW);
				choosed = true;
			} else {
				setBorderColor(Color.BLUE);
				choosed = false;
			}
		}

		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		recWidth = 100;
		recHeight = 100;
		
		canvas.drawRoundRect(mRect, mBorderRadius, mBorderRadius, mRectPaint);

		canvas.drawText(mCharacter, 10, 10, mTextPaint);
		// Log.d("dabble", "mCharacter:" + mCharacter + "  mTextPaint:" +
		// mTextPaint);

		Paint paint = new Paint();
		paint.setColor(Color.RED);
		paint.setTextSize(48);
		canvas.drawText(mCharacter, parentWidth / 20, parentHeight / 8, paint);

		// canvas.drawRect(0, 0, recWidth, recWidth, paint);

		// // Draw the example drawable on top of the text.
		// if (mExampleDrawable != null) {
		// mExampleDrawable.setBounds(paddingLeft, paddingTop, paddingLeft
		// + contentWidth, paddingTop + contentHeight);
		// mExampleDrawable.draw(canvas);
		// }
	}

	/**
	 * Gets the example string attribute value.
	 * 
	 * @return The example string attribute value.
	 */
	public String getCharacter() {
		return mCharacter;
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
		invalidate();
	}

	/**
	 * Gets the example color attribute value.
	 * 
	 * @return The example color attribute value.
	 */
	public int getBackgroundColor() {
		return mBackgroundColor;
	}

	/**
	 * Sets the view's example color attribute value. In the example view, this
	 * color is the font color.
	 * 
	 * @param exampleColor
	 *            The example color attribute value to use.
	 */
	public void setBackgroundColor(int exampleColor) {
		mBackgroundColor = exampleColor;
		invalidateTextPaintAndMeasurements();
	}

	public void setBorderColor(int borderColor) {
		mBorderColor = borderColor;
		invalidateTextPaintAndMeasurements();
	}

	/**
	 * Gets the example dimension attribute value.
	 * 
	 * @return The example dimension attribute value.
	 */
	public float getCharacterSize() {
		return mCharacterSize;
	}

	/**
	 * Sets the view's example dimension attribute value. In the example view,
	 * this dimension is the font size.
	 * 
	 * @param exampleDimension
	 *            The example dimension attribute value to use.
	 */
	public void setCharacterSize(float exampleDimension) {
		mCharacterSize = exampleDimension;
		invalidateTextPaintAndMeasurements();
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
		mRect = new RectF(0, 0, parentWidth / 8, parentHeight / 5);
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Log.d("dabble", "tile.onrestoreinstancestate");

		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			super.onRestoreInstanceState(bundle.getParcelable("instanceState"));

			String restoredCharacter = bundle.getString("mCharacter");
			setCharacter(restoredCharacter);

			return;
		}

		super.onRestoreInstanceState(state);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Bundle savedState = new Bundle();
		savedState.putParcelable("instanceState", super.onSaveInstanceState());
		savedState.putString("mCharacter", mCharacter);

		Log.d("dabble", "tile.onsaveinstancestate");

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
			// The user is interacting with the pie, so we want to turn on
			// acceleration
			// so that the interaction is smooth.
			Log.d("dabble", "ondown");
			return true;
		}
	}

}

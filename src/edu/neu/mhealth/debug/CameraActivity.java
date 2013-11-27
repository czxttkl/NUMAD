package edu.neu.mhealth.debug;

import java.util.ArrayList;
import java.util.Arrays;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import edu.neu.mhealth.debug.helper.Global;
import edu.neu.mhealth.debug.helper.InitRenderTask;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class CameraActivity extends Activity implements CvCameraViewListener2, SensorEventListener {

	/* Basic Variables */
	/** Debug Tag */
	private final String TAG = Global.APP_LOG_TAG;
	/**
	 * The game activity's framelayout. Use this to handle adding/removing
	 * surfaceviews
	 */
	FrameLayout mFrameLayout;
	ImageView mMainMenuBackground;
	ImageView mMainMenuTitle;
	View mMainMenuButtonListView;
	View mAboutView;
	TextView mAboutText;
	public int screenWidth;
	public int screenHeight;

	/* OpenCv Variables */
	private CameraBridgeViewBase mOpenCvCameraView;
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				restoreOrCreateJavaCameraView();
				restoreOrCreateGLSurfaceView();
				restoreOrCreateAboutScreen();
//				restoreOrCreateMainMenu();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}

	};

	/* OpenGl Variables */
	/** OpenGL surface view for displaying bugs */
	public MyGLSurfaceView mGLSurfaceView;

	/* Sensor Variables */
	private final float MAX_ROATE_DEGREE = 1.0f;
	private SensorManager mSensorManager;
	private Sensor mOrientationSensor;
	private Sensor mLinearAccelerometer;
	private float mDirection;
	private float mTargetDirection;
	private AccelerateInterpolator mInterpolator;
	protected final Handler mHandler = new Handler();
	private boolean mStopDetecting;

	// private final long PACE_TWO_OPPOSITE_PEAK_INTERVAL = 2000;

	/*
	 * Activity Callbacks
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		mFrameLayout = new FrameLayout(this);
		setContentView(mFrameLayout);
		initSensors();
	}

	@Override
	protected void onPause() {
		if (mOpenCvCameraView != null) {
			mOpenCvCameraView.disableView();
			saveAndRemoveSurfaceViews();
		}
		pauseSensors();
		super.onPause();
	}

	@Override
	protected void onResume() {
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
		resumeSensors();
		super.onResume();
	}

	public void onClickStartGame(View v) {
		mFrameLayout.removeView(mMainMenuTitle);
		mFrameLayout.removeView(mMainMenuButtonListView);
		mMainMenuBackground.setAlpha(0.8f);
	}

	public void onClickAboutStartButton(View v) {
		mFrameLayout.removeView(mAboutView);
		restoreOrCreateMainMenu();
	}
	/*
	 * Opencv Callbacks
	 */
	@Override
	public void onCameraViewStarted(int width, int height) {

	}

	@Override
	public void onCameraViewStopped() {

	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		return inputFrame.rgba();
	}

	/**
	 * Save/Restore States
	 */
	private void saveAndRemoveSurfaceViews() {
		mFrameLayout.removeAllViewsInLayout();
	}

	/**
	 * Restore or create SurfaceView for bugs. This method is called after
	 * OpenCV library is loaded successfully and must be called after
	 * restoreOrCreateJavaCameraView is called so that CameraView would not overlap GLSurfaceView.
	 */
	private void restoreOrCreateGLSurfaceView() {
		mGLSurfaceView = new MyGLSurfaceView(this);
		new InitRenderTask(this).execute();
	}

	/**
	 * This method is executed after init render work, genrated from
	 * restoreOrCreateGLSurfaceView, has been done.
	 */
	public void restoreOrCreateGLSurfaceView2() {
		if (mGLSurfaceView != null) {
			mFrameLayout.addView(mGLSurfaceView);
			mGLSurfaceView.setZOrderMediaOverlay(true);
			mGLSurfaceView.setZOrderOnTop(true);
		}
	}

	/**
	 * Restore or create SurfaceView for opencv CameraView. This method is
	 * called after OpenCV library is loaded successfully and must be called
	 * before restoreOrCreateGLSurfaceView is called so that CameraView would not overlap GLSurfaceView.
	 */
	private void restoreOrCreateJavaCameraView() {
		mOpenCvCameraView = new CameraView(this);
		mOpenCvCameraView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
		mOpenCvCameraView.enableFpsMeter();
		mFrameLayout.addView(mOpenCvCameraView);
		mOpenCvCameraView.enableView();
	}

	/**
	 * Restore or create Main Menu button/title view. This method is called
	 * after clicking start button in about screen.
	 */
	private void restoreOrCreateMainMenu() {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		screenWidth = size.x;
		screenHeight = size.y;

		mMainMenuBackground = new ImageView(this);
		mMainMenuBackground.setImageResource(R.drawable.black_bg);
		mMainMenuBackground.setAlpha(0f);
		mMainMenuBackground.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		mMainMenuBackground.setScaleType(ImageView.ScaleType.FIT_XY);
		mFrameLayout.addView(mMainMenuBackground);

		mMainMenuTitle = new ImageView(this);
		mMainMenuTitle.setImageResource(R.drawable.main_menu_title2);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(screenWidth / 3, screenWidth / 8);
		lp.setMargins(screenWidth / 5, screenHeight / 10, 0, 1);
		mMainMenuTitle.setLayoutParams(lp);
		mFrameLayout.addView(mMainMenuTitle);

		LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		mMainMenuButtonListView = layoutInflater.inflate(R.layout.main_menu, null);
		FrameLayout.LayoutParams lp1 = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		lp1.setMargins(0, 300, 0, 0);
		mMainMenuButtonListView.setLayoutParams(lp1);
		mFrameLayout.addView(mMainMenuButtonListView);

		mHandler.postDelayed(mMainMenuBorderRunnable, 20);
	}

	/**
	 * Restore or create about screen. (including adding text from strings.xml)
	 */
	private void restoreOrCreateAboutScreen() {
		LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		mAboutView = layoutInflater.inflate(R.layout.about_screen, null);
		Resources res = getResources();
		Drawable background = res.getDrawable(R.drawable.black_bg);
		background.setAlpha(200);
		mAboutView.setBackgroundDrawable(background);
		mFrameLayout.addView(mAboutView);
		
		mAboutText = (TextView) findViewById(R.id.about_text);
        mAboutText.setText(Html.fromHtml(getString(R.string.about_text)));
        mAboutText.setMovementMethod(LinkMovementMethod.getInstance());
        // Setups the link color
        mAboutText.setLinkTextColor(getResources().getColor(
                R.color.holo_light_blue));
	}
	
	/*
	 * Sensor methods
	 */
	private void initSensors() {
		mDirection = 0.0f;
		mTargetDirection = 0.0f;
		mInterpolator = new AccelerateInterpolator();
		mStopDetecting = true;
		// sensor manager
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		mLinearAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
	}

	private void resumeSensors() {
		mStopDetecting = false;
		if (mOrientationSensor != null) {
			mSensorManager.registerListener(this, mOrientationSensor, SensorManager.SENSOR_DELAY_GAME);
		}
		if (mLinearAccelerometer != null) {
			mSensorManager.registerListener(this, mLinearAccelerometer, SensorManager.SENSOR_DELAY_GAME);
		}
		mHandler.postDelayed(mEyeLocationUpdaterRunnable, 20);
	}

	private void pauseSensors() {
		mStopDetecting = true;
		if (mOrientationSensor != null && mLinearAccelerometer != null) {
			mSensorManager.unregisterListener(this);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		if (Sensor.TYPE_ORIENTATION == arg0.sensor.getType()) {
			float direction = arg0.values[0] * -1.0f;
			mTargetDirection = normalizeDegree(direction);
		} else {
			if (Sensor.TYPE_LINEAR_ACCELERATION == arg0.sensor.getType()) {
				// mGLSurfaceView.mRenderer.eyeY = mGLSurfaceView.mRenderer.eyeY
				// + speedX;
				// mGLSurfaceView.mRenderer.eyeX = mGLSurfaceView.mRenderer.eyeX
				// - speedY * SPEED_CONSTANT;
			}
		}
	}

	protected Runnable mEyeLocationUpdaterRunnable = new Runnable() {
		@Override
		public void run() {
			if (!mStopDetecting) {
				if (mDirection != mTargetDirection) {
					// calculate the short routine
					float to = mTargetDirection;
					if (to - mDirection > 180) {
						to -= 360;
					} else if (to - mDirection < -180) {
						to += 360;
					}

					// limit the max speed to MAX_ROTATE_DEGREE
					float distance = to - mDirection;
					if (Math.abs(distance) > MAX_ROATE_DEGREE) {
						distance = distance > 0 ? MAX_ROATE_DEGREE : (-1.0f * MAX_ROATE_DEGREE);
					}

					// need to slow down if the distance is short
					float mDirectionNew = normalizeDegree(mDirection
							+ ((to - mDirection) * mInterpolator
									.getInterpolation(Math.abs(distance) > MAX_ROATE_DEGREE ? 0.4f : 0.3f)));
					mDirection = mDirectionNew;
					updateOpenGLEyeLocation(mDirection);
				}
				mHandler.postDelayed(mEyeLocationUpdaterRunnable, 20);
			}
		}
	};

	/**
	 * This runnable will be run until Main Menu Title
	 */
	protected Runnable mMainMenuBorderRunnable = new Runnable() {
		@Override
		public void run() {
			// This means the views haven't been rendered
			View buttonLinearLayout = findViewById(R.id.buttons_linear_layout);
			if (buttonLinearLayout == null || mMainMenuTitle == null) {
				mHandler.postDelayed(mMainMenuBorderRunnable, 200);
				return;
			}
			int mainMenuTitleWidth = mMainMenuTitle.getWidth();
			int mainMenuTitleHeight = mMainMenuTitle.getHeight();
			int buttonLinearLayoutWidth = buttonLinearLayout.getWidth();
			int buttonLinearLayoutHeight = buttonLinearLayout.getHeight();
			//
			int[] mainMenuTitleLocation = new int[2];
			mMainMenuTitle.getLocationInWindow(mainMenuTitleLocation);
			int[] mainMenuButtonLocation = new int[2];
			buttonLinearLayout.getLocationInWindow(mainMenuButtonLocation);
			// This means the views haven't been rendered
			if ((mainMenuTitleLocation[0] == 0 && mainMenuTitleLocation[1] == 0)
					|| (mainMenuButtonLocation[0] == 0 && mainMenuButtonLocation[1] == 0)
					|| mGLSurfaceView.mRenderer == null) {
				mHandler.postDelayed(mMainMenuBorderRunnable, 200);
				return;
			}
			// Convert to y-axis upwards coordinate
			mainMenuTitleLocation[1] = screenHeight - mainMenuTitleLocation[1];
			mainMenuButtonLocation[1] = screenHeight - mainMenuButtonLocation[1];
			Log.d(TAG, "main menu title location:" + mainMenuTitleLocation[0] + "," + mainMenuTitleLocation[1] + ":"
					+ mainMenuTitleWidth + "," + mainMenuTitleHeight);
			Log.d(TAG, "main menu button location:" + mainMenuButtonLocation[0] + "," + mainMenuButtonLocation[1] + ":"
					+ buttonLinearLayoutWidth + "," + buttonLinearLayoutHeight);
			// Add the title and buttons constraints
			BorderLine bl1 = new BorderLine(BorderLine.TYPE_UPPER_BOUND, 0, mainMenuTitleLocation[1], 1);
			BorderLine bl2 = new BorderLine(BorderLine.TYPE_UPPER_BOUND, 0, mainMenuButtonLocation[1], 1);
			BorderLine bl3 = new BorderLine(BorderLine.TYPE_LOWER_BOUND, 0, mainMenuTitleLocation[1]
					- mainMenuTitleHeight, 1);
			BorderLine bl4 = new BorderLine(BorderLine.TYPE_LOWER_BOUND, 0, mainMenuButtonLocation[1]
					- buttonLinearLayoutHeight, 1);
			BorderLine bl5 = new BorderLine(BorderLine.TYPE_X_LEFT_BOUND, 1, mainMenuTitleLocation[0], 0);
			BorderLine bl6 = new BorderLine(BorderLine.TYPE_X_LEFT_BOUND, 1, mainMenuButtonLocation[0], 0);
			BorderLine bl7 = new BorderLine(BorderLine.TYPE_X_RIGHT_BOUND, 1, mainMenuTitleLocation[0]
					+ mainMenuTitleWidth, 0);
			BorderLine bl8 = new BorderLine(BorderLine.TYPE_X_RIGHT_BOUND, 1, mainMenuButtonLocation[0]
					+ buttonLinearLayoutWidth, 0);
			// Add the screen constraints
			BorderLine bl9 = new BorderLine(BorderLine.TYPE_UPPER_BOUND, 0, screenHeight, 1);
			BorderLine bl10 = new BorderLine(BorderLine.TYPE_LOWER_BOUND, 0, 0, 1);
			BorderLine bl11 = new BorderLine(BorderLine.TYPE_X_LEFT_BOUND, 1, 0, 0);
			BorderLine bl12 = new BorderLine(BorderLine.TYPE_X_RIGHT_BOUND, 1, screenWidth, 0);

			ArrayList<BorderLine> mBorderLineList = new ArrayList<BorderLine>();
			mBorderLineList.add(bl1);
			mBorderLineList.add(bl2);
			mBorderLineList.add(bl3);
			mBorderLineList.add(bl4);
			mBorderLineList.add(bl5);
			mBorderLineList.add(bl6);
			mBorderLineList.add(bl7);
			mBorderLineList.add(bl8);
			mBorderLineList.add(bl9);
			mBorderLineList.add(bl10);
			mBorderLineList.add(bl11);
			mBorderLineList.add(bl12);
			mGLSurfaceView.mRenderer.borderLineList = mBorderLineList;
		}
	};

	private float normalizeDegree(float degree) {
		return (degree + 720) % 360;
	}

	private float average(float[] array) {
		float sum = 0;
		for (float i : array) {
			sum = sum + i;
		}
		return sum / array.length;
	}

	private void updateOpenGLEyeLocation(float mDirectionNew) {
		long now = System.currentTimeMillis();
		// if (speedY == 0 && speedX == 0) {
		//
		// mGLSurfaceView.mRenderer.globalRotateDegree = mDirectionNew;
		// }
		// Log.d(TAG, "speedY:" + speedY + " eyeX:" +
		// mGLSurfaceView.mRenderer.eyeX);
		// mGLSurfaceView.mRenderer.eyeX;
	}
}

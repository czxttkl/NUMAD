package edu.neu.mhealth.debug;

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

import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
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

public class CameraActivity extends Activity implements CvCameraViewListener2,
		SensorEventListener {

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
	View mButtonView;
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
				restoreOrCreateMainMenu();
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
	private MyGLSurfaceView mGLSurfaceView;

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
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this,
				mLoaderCallback);
		resumeSensors();
		super.onResume();
	}

	/*
	 * Opencv Callbacks
	 */
	@Override
	public void onCameraViewStarted(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub

	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		return inputFrame.rgba();
	}

	/*
	 * Save/Restore States
	 */
	private void saveAndRemoveSurfaceViews() {
		mFrameLayout.removeAllViewsInLayout();
	}

	/**
	 * Restore or create SurfaceView for bugs. This method is called after
	 * OpenCV library is loaded successfully.
	 */
	private void restoreOrCreateGLSurfaceView() {
		mGLSurfaceView = new MyGLSurfaceView(this);
		// CameraView must be added after GLSurfaceView so that GLSurfaceView
		// could appear upon CameraView
		mFrameLayout.addView(mGLSurfaceView);
		mGLSurfaceView.setZOrderMediaOverlay(true);
		mGLSurfaceView.setZOrderOnTop(true);

	}

	/**
	 * Restore or create SurfaceView for opencv CameraView. This method is
	 * called after OpenCV library is loaded successfully.
	 */
	private void restoreOrCreateJavaCameraView() {
		mOpenCvCameraView = new CameraView(this);
		mOpenCvCameraView.setLayoutParams(new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
		mOpenCvCameraView.enableFpsMeter();
		mFrameLayout.addView(mOpenCvCameraView);
		mOpenCvCameraView.enableView();
	}

	private void restoreOrCreateMainMenu() {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		screenWidth = size.x;
		screenHeight = size.y;

		if (mMainMenuBackground == null) {
			mMainMenuBackground = new ImageView(this);
			mMainMenuBackground.setImageResource(R.drawable.black_bg);
			mMainMenuBackground.setAlpha(0.6f);
			mMainMenuBackground.setLayoutParams(new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			mMainMenuBackground.setScaleType(ImageView.ScaleType.FIT_XY);
			mFrameLayout.addView(mMainMenuBackground);
		}

		if (mMainMenuTitle == null) {
			mMainMenuTitle = new ImageView(this);
			//We set mMainMenuTitle's id to be 6789
			mMainMenuTitle.setId(6789);
			mMainMenuTitle.setImageResource(R.drawable.main_menu_title2);
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
					screenWidth / 3, screenWidth / 8);
			lp.setMargins(screenWidth / 5, screenHeight / 10, 0, 1);
			mMainMenuTitle.setLayoutParams(lp);
			mFrameLayout.addView(mMainMenuTitle);
		}

		if (mButtonView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
					.getSystemService(LAYOUT_INFLATER_SERVICE);
			mButtonView = layoutInflater.inflate(R.layout.main_menu, null);
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			lp.setMargins(0, 300, 0, 0);
			mButtonView.setLayoutParams(lp);
			mFrameLayout.addView(mButtonView);
		}
		mHandler.postDelayed(mMainMenuBorderRunnable ,20);
//		Log.d(TAG, "position:" + mButtonView.getX() + "," + mButtonView.getWidth() + "," + mButtonView.getY() + mButtonView.getHeight());
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
		mOrientationSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		mLinearAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
	}

	private void resumeSensors() {
		mStopDetecting = false;
		if (mOrientationSensor != null) {
			mSensorManager.registerListener(this, mOrientationSensor,
					SensorManager.SENSOR_DELAY_GAME);
		}
		if (mLinearAccelerometer != null) {
			mSensorManager.registerListener(this, mLinearAccelerometer,
					SensorManager.SENSOR_DELAY_GAME);
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
						distance = distance > 0 ? MAX_ROATE_DEGREE
								: (-1.0f * MAX_ROATE_DEGREE);
					}

					// need to slow down if the distance is short
					float mDirectionNew = normalizeDegree(mDirection
							+ ((to - mDirection) * mInterpolator
									.getInterpolation(Math.abs(distance) > MAX_ROATE_DEGREE ? 0.4f
											: 0.3f)));
					mDirection = mDirectionNew;
					updateOpenGLEyeLocation(mDirection);
				}
				mHandler.postDelayed(mEyeLocationUpdaterRunnable, 20);
			}
		}
	};
	
	protected Runnable mMainMenuBorderRunnable = new Runnable() {
		@Override
		public void run() {
//			int mainMenuTitleX= (int) mMainMenuTitle.getX();
//			int mainMenuTitleY = (int) mMainMenuTitle.getY();
			View buttonLinearLayout = findViewById(R.id.buttons_linear_layout);
//			int buttonLinearLayoutX = (int) buttonLinearLayout.getX();
//			int buttonLinearLayoutY = (int) buttonLinearLayout.getY();
			int mainMenuTitleWidth = mMainMenuTitle.getWidth();
			int mainMenuTitleHeight = mMainMenuTitle.getHeight();
			int buttonLinearLayoutWidth = buttonLinearLayout.getWidth();
			int buttonLinearLayoutHeight = buttonLinearLayout.getHeight();
//			
			int[] mainMenuTitleLocation = new int[2];
			mMainMenuTitle.getLocationInWindow(mainMenuTitleLocation);
			int[] mainMenuButtonLocation = new int[2];
			buttonLinearLayout.getLocationInWindow(mainMenuButtonLocation);
			if ((mainMenuTitleLocation[0] == 0.0 && mainMenuTitleLocation[1] == 0.0) || (mainMenuButtonLocation[0] == 0 && mainMenuButtonLocation[1] == 0)) {
				mHandler.postDelayed(mMainMenuBorderRunnable, 20);
			}
			Log.d(TAG, "main menu title location:" + mainMenuTitleLocation[0] + "," + mainMenuTitleLocation[1] + ":" + mainMenuTitleWidth + "," + mainMenuTitleHeight);
			Log.d(TAG, "main menu button location:" + mainMenuButtonLocation[0] + "," + mainMenuButtonLocation[1] + ":" + buttonLinearLayoutWidth + "," + buttonLinearLayoutHeight);
//			Log.d(TAG, "position mainMenuTitle:" + mainMenuTitleX + "," +mainMenuTitleY + ":" + mainMenuTitleWidth + "," + mainMenuTitleHeight);
//			Log.d(TAG, "position buttonLinearLayout:" + buttonLinearLayoutX + "," + buttonLinearLayoutY + ":" + buttonLinearLayoutWidth + "," + buttonLinearLayoutHeight);
			
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

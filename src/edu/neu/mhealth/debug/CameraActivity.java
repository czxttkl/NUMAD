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
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import edu.neu.mhealth.debug.helper.Global;
import edu.neu.mhealth.debug.helper.InitRenderTask;
import android.graphics.Color;
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
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
	RelativeLayout mColorPickLayout;
	ImageView mMainMenuBackground;
	ImageView mMainMenuTitle;
	View mMainMenuButtonListView;
	View mAboutView;
	TextView mAboutText;
	Drawable blackBackground;
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
				// restoreOrCreateMainMenu();
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
	private Sensor mAccelerometer;
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
		storeScreenDimensions();
		initSensors();
		initBlackBackground();
		// initAnimations();
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

	View mColorPickBottomBar;
	View mColorPickInstructionsView;
	View mColorPickHelpNotif;
	View mColorPickCloseButton;
	View mColorPickCameraButton;
	TextView mColorPickHelpNotifTextView;

	public void onClickAboutStartButton(View v) {
		mFrameLayout.removeView(mAboutView);
		restoreOrCreateMainMenu();
	}

	public void onClickMainMenuStartGame(View v) {
		mFrameLayout.removeView(mMainMenuTitle);
		mFrameLayout.removeView(mMainMenuButtonListView);
		mFrameLayout.removeView(mMainMenuBackground);
		mGLSurfaceView.mRenderer.openGlMode = mGLSurfaceView.mRenderer.MODE_DEFAULT;

		// Inflates the Overlay Layout to be displayed above the Camera View
		LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		mColorPickLayout = (RelativeLayout) layoutInflater.inflate(R.layout.color_pick_overlay, null, false);

		// Set background to semi-transparent black
		blackBackground.setAlpha(200);
		mColorPickLayout.setBackgroundDrawable(blackBackground);

		mFrameLayout.addView(mColorPickLayout);
		// Gets a reference to the bottom navigation bar
		mColorPickBottomBar = mColorPickLayout.findViewById(R.id.bottom_bar);

		// Gets a reference to the instructions view
		mColorPickInstructionsView = mColorPickLayout.findViewById(R.id.instructions);

		// Gets a reference to the help notification viewstub
		mColorPickHelpNotif = mColorPickLayout.findViewById(R.id.overlay_build_target_help);

		// Gets a reference to the CloseBuildTargetMode button
		mColorPickCloseButton = mColorPickLayout.findViewById(R.id.close_button);

		// Gets a reference to the Camera button
		mColorPickCameraButton = mColorPickLayout.findViewById(R.id.camera_button);

		initializeInstructionMode();
	}

	public void onClickMainMenuExit(View v) {
		finish();
	}

	public void onClickMainMenuSettings(View v) {

	}

	/**
	 * Button Camera clicked. Marker's color is picked after this method is
	 * called.
	 */
	public void onClickColorPickCameraButton(View v) {
		// Builds the new target
		// startBuild();
		mColorPickCameraButton.setEnabled(false);
		openCvMode = COLOR_PICK_PICK_MODE;
	}

	/** Button Close/Cancel clicked in the process of color picking */
	public void onClickColorPickCameraClose(View v) {
		blackBackground.setAlpha(200);
		mColorPickLayout.setBackgroundDrawable(blackBackground);
		// Goes back to the Color Pick Add rMode
		initializeInstructionMode();
		// Set mode to default
		openCvMode = 0;
	}

	/** Button Add clicked - This will cause the instruction interfaces shows. */
	public void onClickColorPickAddButton(View v) {
		// Shows the instructions view and returns
		mColorPickInstructionsView.setVisibility(View.VISIBLE);
	}

	/** Instructions button OK clicked */
	public void onClickInstructionsOnOk(View v) {
		// Hides the instructions view
		mColorPickInstructionsView.setVisibility(View.GONE);
		// Calls to the newTargetButtonClick Method
		// to enter the BuildTargetMode
		initializeColorPickCameraMode();
	}

	/** Instructions button Cancel clicked */
	public void onClickInstructionsOnCancel(View v) {
		// Hides the instructions view without
		// updating the instructions flag
		// mColorPickInstructionsView.setVisibility(View.GONE);
		// mColorPickNewTargetButton.setEnabled(true);
		mFrameLayout.removeView(mColorPickLayout);
		restoreOrCreateMainMenu();
		mGLSurfaceView.mRenderer.openGlMode = mGLSurfaceView.mRenderer.MODE_MAIN_MENU;
	}

	/** Initialize Color Pick Add mode views */
	private void initializeInstructionMode() {
		// Shows the bottom bar with the new Target Button
		mColorPickBottomBar.setVisibility(View.GONE);
		// Hides the target build controls
		mColorPickHelpNotif.setVisibility(View.GONE);
		mColorPickCameraButton.setVisibility(View.GONE);
		mColorPickCloseButton.setVisibility(View.GONE);
		mColorPickInstructionsView.setVisibility(View.VISIBLE);
	}

	/** Initialize Color Pick Camera mode views */
	private void initializeColorPickCameraMode() {
		// Shows the bottom bar with the Build target options
		mColorPickHelpNotif.setVisibility(View.VISIBLE);
		mColorPickBottomBar.setVisibility(View.VISIBLE);
		mColorPickCameraButton.setVisibility(View.VISIBLE);
		mColorPickCloseButton.setVisibility(View.VISIBLE);

		// Gets a reference to the help notification textview
		mColorPickHelpNotifTextView = (TextView)mColorPickLayout.findViewById(R.id.color_pick_help_notif_text);
		
		// Set background to transparent
		blackBackground.setAlpha(0);
		mColorPickLayout.setBackgroundDrawable(blackBackground);
		
		//Set opencvmode to crosshair mode, so opencv will draw a crosshair in the center of image
		openCvMode = COLOR_PICK_CROSSHAIR_MODE;
	}

	/*
	 * Opencv Callbacks
	 */
	Mat mGray;
	Mat mRgba;
	Scalar grayColor;
	Scalar blueColor;
	org.opencv.core.Point crosshairHeftmost;
	org.opencv.core.Point crosshairRightmost;
	org.opencv.core.Point crosshairUpmost;
	org.opencv.core.Point crosshairDownmost;
	private Scalar mColorPickRgba;
	private Scalar mColorPickHsv;
	int openCvMode;
	private final int COLOR_PICK_CROSSHAIR_MODE = 123;
	private final int COLOR_PICK_PICK_MODE = 124;
	private final int COLOR_PICK_HOLD_WRONGLY_MODE = 125;
	Rect colorPickArea;

	@Override
	public void onCameraViewStarted(int width, int height) {
		mGray = new Mat();
		mRgba = new Mat();
		grayColor = new Scalar(192, 192, 192);
		blueColor = new Scalar(0, 255, 255);
		crosshairHeftmost = new org.opencv.core.Point(width / 2 - 50, height / 2);
		crosshairRightmost = new org.opencv.core.Point(width / 2 + 50, height / 2);
		crosshairUpmost = new org.opencv.core.Point(width / 2, height / 2 - 50);
		crosshairDownmost = new org.opencv.core.Point(width / 2, height / 2 + 50);
		colorPickArea = new Rect();
		colorPickArea.x = width / 2 - 5;
		colorPickArea.y = height / 2 - 5;
		colorPickArea.width = 10;
		colorPickArea.height = 10;
		// Log.d(TAG, "czx screenwidth & height:" + screenWidth + "," +
		// screenHeight + ":" + width + "," + height);
	}

	@Override
	public void onCameraViewStopped() {
		mGray.release();
		mRgba.release();
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		mGray = inputFrame.gray();
		mRgba = inputFrame.rgba();
		switch (openCvMode) {
		case COLOR_PICK_CROSSHAIR_MODE:
			Mat colorPickAreaRgba = mRgba.submat(colorPickArea);
			Mat colorPickAreaHsv = new Mat();
			Imgproc.cvtColor(colorPickAreaRgba, colorPickAreaHsv, Imgproc.COLOR_RGB2HSV_FULL);
			// Calculate average color of color pick region
			mColorPickHsv = Core.sumElems(colorPickAreaHsv);
			int pointCount = colorPickArea.width * colorPickArea.height;
			Log.i(TAG, "mBlobColorHsv.val.length:" + mColorPickHsv.val.length);
			Log.i(TAG, "pointCount:" + pointCount);

			for (int i = 0; i < mColorPickHsv.val.length; i++)
				mColorPickHsv.val[i] /= pointCount;

			mColorPickRgba = converScalarHsv2Rgba(mColorPickHsv);
			Mat colorLabel = mRgba.submat(44, 108, 8, 72);
			colorLabel.setTo(mColorPickRgba);
			Core.line(mRgba, crosshairHeftmost, crosshairRightmost, blueColor, 10);
			Core.line(mRgba, crosshairUpmost, crosshairDownmost, blueColor, 10);
			break;
		case COLOR_PICK_PICK_MODE:
			break;
		case COLOR_PICK_HOLD_WRONGLY_MODE:
			Core.line(mRgba, crosshairHeftmost, crosshairRightmost, grayColor, 10);
			Core.line(mRgba, crosshairUpmost, crosshairDownmost, grayColor, 10);
			break;
		default:
			// Do no process here
		}
		return mRgba;
	}

	private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
		Mat pointMatRgba = new Mat();
		Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
		Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);
		return new Scalar(pointMatRgba.get(0, 0));
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
	 * restoreOrCreateJavaCameraView is called so that CameraView would not
	 * overlap GLSurfaceView.
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
			mGLSurfaceView.mRenderer.openGlMode = mGLSurfaceView.mRenderer.MODE_MAIN_MENU;
			mFrameLayout.addView(mGLSurfaceView);
			mGLSurfaceView.setZOrderMediaOverlay(true);
			mGLSurfaceView.setZOrderOnTop(true);
		}
	}

	/**
	 * Restore or create SurfaceView for opencv CameraView. This method is
	 * called after OpenCV library is loaded successfully and must be called
	 * before restoreOrCreateGLSurfaceView is called so that CameraView would
	 * not overlap GLSurfaceView.
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
		mAboutText.setLinkTextColor(getResources().getColor(R.color.holo_light_blue));
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
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}

	private void resumeSensors() {
		mStopDetecting = false;
		if (mOrientationSensor != null) {
			mSensorManager.registerListener(this, mOrientationSensor, SensorManager.SENSOR_DELAY_GAME);
		}
		if (mLinearAccelerometer != null) {
			mSensorManager.registerListener(this, mLinearAccelerometer, SensorManager.SENSOR_DELAY_GAME);
		}
		if (mAccelerometer != null) {
			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
		}
		mHandler.postDelayed(mEyeLocationUpdaterRunnable, 20);
	}

	private void pauseSensors() {
		mStopDetecting = true;
		if (mOrientationSensor != null || mLinearAccelerometer != null || mAccelerometer != null) {
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
			} else {
				if (Sensor.TYPE_ACCELEROMETER == arg0.sensor.getType()) {
					Log.d(TAG, "accelerometer z:" + arg0.values[2]);
					switch (openCvMode) {
					case COLOR_PICK_CROSSHAIR_MODE:
						if (arg0.values[2] < 8.5f) {
							mColorPickHelpNotifTextView.setText(R.string.color_pick_help_notif_horizon);
							mColorPickHelpNotifTextView.setTextColor(Color.RED);
							openCvMode = COLOR_PICK_HOLD_WRONGLY_MODE;
							mColorPickCameraButton.setEnabled(false);
						}
						break;
					case COLOR_PICK_HOLD_WRONGLY_MODE:
						if (arg0.values[2] > 8.5f) {
							mColorPickHelpNotifTextView.setText(R.string.color_pick_help_notif);
							mColorPickHelpNotifTextView.setTextColor(Color.WHITE);
							openCvMode = COLOR_PICK_CROSSHAIR_MODE;
							mColorPickCameraButton.setEnabled(true);
						}
					default:
						//Do nothing
					}
				}
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

	/** Initialize blackbackground drawable resource */
	private void initBlackBackground() {
		Resources res = getResources();
		blackBackground = res.getDrawable(R.drawable.black_bg);
	}

	/** Store screenwidth and screenheight */
	private void storeScreenDimensions() {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		screenWidth = size.x;
		screenHeight = size.y;
	}

}

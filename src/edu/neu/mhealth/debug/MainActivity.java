package edu.neu.mhealth.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.video.Video;

import edu.neu.mhealth.debug.helper.AccEventListener;
import edu.neu.mhealth.debug.helper.Bug;
import edu.neu.mhealth.debug.helper.BugManager;
import edu.neu.mhealth.debug.helper.Global;
import edu.neu.mhealth.debug.helper.InitRenderTask;
import edu.neu.mhealth.debug.helper.JumpBug;
import edu.neu.mhealth.debug.helper.ModeManager;
import edu.neu.mhealth.debug.helper.MotionEventListener;
import edu.neu.mhealth.debug.helper.MovingAverage;
import edu.neu.mhealth.debug.opengl.OpenGLBug;
import edu.neu.mhealth.debug.opengl.OpenGLBugManager;
import edu.neu.mhealth.debug.opengl.OpenGLFire;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements OnTouchListener,
		CvCameraViewListener2, SensorEventListener {
	
	private static final String TAG = "MainActivity";
	
	/// Layout	
	private FrameLayout mFrameLayout;
	View mColorPickBottomBar;
	View mColorPickInstructionsView;
	View mColorPickHelpNotif;
	View mColorPickHelpConfirmButt1;
	View mColorPickHelpConfirmButt2;
	View mColorPickCloseButton;
	public View mColorPickCameraButton;
	public TextView mColorPickHelpNotifTextView;
	
	RelativeLayout mColorPickLayout;
	ImageView mMainMenuBackground;
	ImageView mMainMenuTitle;
	View mMainMenuButtonListView;
	View mAboutView;
	TextView mAboutText;
	
	Drawable blackBackground;
	
	public int score = 0;
	
	RelativeLayout mTutorial1InstructionLayout;
	RelativeLayout mTutorial2InstructionLayout;
	RelativeLayout mTutorial3InstructionLayout;

	
	/// OpenGL
	public MyGLSurfaceView mGLSurfaceView;
	protected final Handler mHandler = new Handler();

	
	/// OpenCV basic
	private List<android.hardware.Camera.Size> resolutions;
	
	/// For Game Flow Control
	public boolean shoeColorPicked = false;
	private boolean floorColorPicked = false;
	
	Mat colorPickAreaHsv;
	org.opencv.core.Point crosshairHeftmost;
	org.opencv.core.Point crosshairRightmost;
	org.opencv.core.Point crosshairUpmost;
	org.opencv.core.Point crosshairDownmost;
	org.opencv.core.Point destinationTarget;
	private Scalar mShoeColorPickRgba;
	private Scalar mShoeColorPickHsv;
	private Scalar mFloorColorPickRgba;
	private Scalar mFloorColorPickHsv;
	
	/// For color detection
	private boolean mIsColorSelected = false;
	private Mat mRgba;
	private Mat mGray;
	private Mat tempGray;
	private Scalar mBlobColorRgba;
	private Scalar mBlobColorHsv;
	private ColorDetector mDetector;
	private Mat mSpectrum;
	private Size SPECTRUM_SIZE;
	private Scalar CONTOUR_COLOR;

	private CameraBridgeViewBase mOpenCvCameraView;
	private ConfigureView configureView;
	private JumpBug jumpBug;
	private int initalX = 0;
	private int initalY = 0;
	private int steps = 0;
	private int xOffset = 0;
	private int yOffset = 0;

	public int screenOpenGLWidth;
	public int screenOpenGLHeight;

	public int imageOpenCvWidth;
	public int imageOpenCvHeight;
	
	public double openCVGLRatioX;
	public double openCVGLRatioY;
	
	private Scalar colorGreen;
	private Scalar colorRed;
	private Scalar colorBlue;
	private Scalar colorWhite;
	private Scalar colorGray;
	
	Rect colorPickArea;
	ColorDetector mColorBlobDetector;
	List<MatOfPoint> detectedShoesContours;
	List<MatOfPoint> detectedFloorContours;

	/// For optical flow
	private List<MatOfPoint> contourFloor;
	private List<MatOfPoint> contourShoe;


	private Mat mOpFlowCurr;
	private Mat mOpFlowPrev;
	private MatOfPoint mMOPopFlowCurr;
	private MatOfPoint mMOPopFlowPrev;
	private MatOfPoint2f mMOP2PtsCurr;
	private MatOfPoint2f mMOP2PtsPrev;
	private MatOfPoint2f mMOP2PtsSafe;
	private List<Point> cornersPrev;
	private List<Point> cornersCurr;
	private MatOfByte status;
	private MatOfFloat err;
	private List<Byte> byteStatus;
	private MovingAverage filterX;
	private MovingAverage filterY;


	Mat optFlowMatRgba;
	Mat optFlowMatGray;

	private static final int rectWidth = 500;
	private static final int rectHeight = 250;
	private static final int squareMetric = 200;
	
	private static final int motionThX = 100;
	private static final int motionThY = 100;
	
	/// For jumping detection and tilt detection
	private SensorManager sensorManager;
	private Sensor sensorLinearAcc;
	private Sensor sensorAcc;
	private AccEventListener accEventListener;
	
	/// For direction / motion detection
	private MotionEventListener motionEventListener;
	

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				
				restoreOrCreateJavaCameraView();
				restoreOrCreateGLSurfaceView();
				restoreOrCreateAboutScreen();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	public MainActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		screenOpenGLHeight = metrics.heightPixels;
		screenOpenGLWidth = metrics.widthPixels;

		mFrameLayout = new FrameLayout(this);
		setContentView(mFrameLayout);
		
		initBlackBackground();

		filterX = new MovingAverage(5);
		filterY = new MovingAverage(5);
	
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
		sensorManager.unregisterListener(accEventListener);
	}

	@Override
	public void onResume() {
		super.onResume();
		
		jumpBug = new JumpBug(getApplicationContext());
		
		sensorLinearAcc = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		sensorAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		accEventListener = new AccEventListener(this);
		sensorManager.registerListener(accEventListener, sensorLinearAcc, SensorManager.SENSOR_DELAY_FASTEST);	
		sensorManager.registerListener(this, sensorAcc, SensorManager.SENSOR_DELAY_FASTEST);
		accEventListener.addObserver(jumpBug);

		motionEventListener = new MotionEventListener();
		motionEventListener.addObserver(jumpBug);
		
		ModeManager.getModeManager().addObserver(jumpBug);
		
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {
		
		imageOpenCvWidth = width;
		imageOpenCvHeight = height;
		
		openCVGLRatioX = (double) imageOpenCvWidth / screenOpenGLWidth;
		openCVGLRatioY = (double) imageOpenCvHeight / screenOpenGLHeight;
		
		mRgba = new Mat(height, width, CvType.CV_8UC4);
		mGray = new Mat(height, width, CvType.CV_8UC1);
		mSpectrum = new Mat();
		mBlobColorRgba = new Scalar(255);
		mBlobColorHsv = new Scalar(255);
		SPECTRUM_SIZE = new Size(200, 64);
		CONTOUR_COLOR = new Scalar(255, 0, 0, 255);

		mOpFlowCurr = new Mat();
		mOpFlowPrev = new Mat();
		optFlowMatRgba = new Mat();
		optFlowMatGray = new Mat();
		mMOPopFlowPrev = new MatOfPoint();
		mMOPopFlowCurr = new MatOfPoint();
		mMOP2PtsCurr = new MatOfPoint2f();
		mMOP2PtsPrev = new MatOfPoint2f();
		mMOP2PtsSafe = new MatOfPoint2f();
		status = new MatOfByte();
		err = new MatOfFloat();

		colorRed = new Scalar(255, 0, 0);
		colorGreen = new Scalar(0, 255, 0);
		colorBlue = new Scalar(51, 181, 229);
		colorWhite = new Scalar(255, 255, 255);
		colorGray = new Scalar(192, 192, 192);

		colorPickAreaHsv = new Mat();
		crosshairHeftmost = new org.opencv.core.Point(width / 2 - 50, height / 2);
		crosshairRightmost = new org.opencv.core.Point(width / 2 + 50, height / 2);
		crosshairUpmost = new org.opencv.core.Point(width / 2, height / 2 - 50);
		crosshairDownmost = new org.opencv.core.Point(width / 2, height / 2 + 50);
		colorPickArea = new Rect();
		colorPickArea.x = width / 2 - 5;
		colorPickArea.y = height / 2 - 5;
		colorPickArea.width = 10;
		colorPickArea.height = 10;
		mColorBlobDetector = new ColorDetector(width, height);
	}

	public void onCameraViewStopped() {
		mRgba.release();
		mGray.release();
	}

	public boolean onTouch(View v, MotionEvent event) {

		Log.e(TAG, "touch coordinate: " + event.getX() + "  " + event.getY());

		int x = configureView.getFloorPosition().x;
		int y = configureView.getFloorPosition().y;

		return false; 
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();

		xOffset = (mOpenCvCameraView.getWidth() - imageOpenCvWidth) / 2;
		yOffset = (mOpenCvCameraView.getHeight() - imageOpenCvHeight) / 2;
		
		int gameMode = ModeManager.getModeManager().getCurrentMode();
		switch (gameMode) {
		case ModeManager.MODE_COLOR_PICK_CROSSHAIR:
			Mat colorPickAreaRgba = mRgba.submat(colorPickArea);
			Imgproc.cvtColor(colorPickAreaRgba, colorPickAreaHsv, Imgproc.COLOR_RGB2HSV_FULL);
			int pointCount = colorPickArea.width * colorPickArea.height;

			// Pick shoes' color
			if (!shoeColorPicked) {
				// Calculate average color of shoes' color pick region
				mShoeColorPickHsv = Core.sumElems(colorPickAreaHsv);
				for (int i = 0; i < mShoeColorPickHsv.val.length; i++)
					mShoeColorPickHsv.val[i] /= pointCount;
				mShoeColorPickRgba = converScalarHsv2Rgba(mShoeColorPickHsv);
				Mat colorLabel = mRgba.submat(44, 108, 8, 72);
				colorLabel.setTo(mShoeColorPickRgba);
			} else {
				mFloorColorPickHsv = Core.sumElems(colorPickAreaHsv);
				for (int i = 0; i < mFloorColorPickHsv.val.length; i++)
					mFloorColorPickHsv.val[i] /= pointCount;
				mFloorColorPickRgba = converScalarHsv2Rgba(mFloorColorPickHsv);
				Mat colorLabel = mRgba.submat(44, 108, 8, 72);
				colorLabel.setTo(mFloorColorPickRgba);
			}
			Core.line(mRgba, crosshairHeftmost, crosshairRightmost, colorBlue, 10);
			Core.line(mRgba, crosshairUpmost, crosshairDownmost, colorBlue, 10);
			break;

		case ModeManager.MODE_SHOE_COLOR_PICKED:
			mColorBlobDetector.process(mRgba);
			detectedShoesContours = mColorBlobDetector.getShoesContours();
			// Log.e(TAG, "Contours count: " + contours.size());
			if (detectedShoesContours == null)
				return mRgba;
			detectedShoesContours.removeAll(Collections.singleton(null));
			Imgproc.drawContours(mRgba, detectedShoesContours, -1, colorRed, 6);
			break;

		case ModeManager.MODE_COLOR_PICK_HOLD_WRONGLY:
			Core.line(mRgba, crosshairHeftmost, crosshairRightmost, colorGray, 10);
			Core.line(mRgba, crosshairUpmost, crosshairDownmost, colorGray, 10);
			break;

		case ModeManager.MODE_FLOOR_COLOR_PICKED:
			mColorBlobDetector.process(mRgba);
			detectedFloorContours = mColorBlobDetector.getFloorContours();
			if (detectedFloorContours == null)
				return mRgba;
			detectedFloorContours.removeAll(Collections.singleton(null));
			Imgproc.drawContours(mRgba, detectedFloorContours, -1, colorRed, 4);
			break;

		case ModeManager.MODE_TUTORIAL_1:
			mColorBlobDetector.process(mRgba);
			detectedShoesContours = mColorBlobDetector.getShoesContours();
			// if (detectedShoesContours == null)
			// return mRgba;
			detectedShoesContours.removeAll(Collections.singleton(null));
			setRendererContourMassCenter();

			detectedFloorContours = mColorBlobDetector.getFloorContours();
			detectedFloorContours.removeAll(Collections.singleton(null));
			Imgproc.drawContours(mRgba, detectedFloorContours, -1, colorRed, 4);
			if (destinationTarget != null)
				Core.circle(mRgba, destinationTarget, 10, colorRed, -1);
			
			break;
			
		default:
			break;
		}

		

		Rect optFlowRect = new Rect();
		optFlowRect.x = imageOpenCvWidth / 2 - squareMetric / 2;
		optFlowRect.y = imageOpenCvHeight / 2 - squareMetric / 2;
		optFlowRect.width = squareMetric;
		optFlowRect.height = squareMetric;

		optFlowMatRgba = mRgba.submat(optFlowRect);

		if (mMOP2PtsPrev.rows() == 0) {
			Imgproc.cvtColor(optFlowMatRgba, mOpFlowCurr,
					Imgproc.COLOR_RGBA2GRAY);
			mOpFlowCurr.copyTo(mOpFlowPrev);

			Imgproc.goodFeaturesToTrack(mOpFlowPrev, mMOPopFlowPrev, 50, 0.01,
					20);
			mMOP2PtsPrev.fromArray(mMOPopFlowPrev.toArray());
			mMOP2PtsPrev.copyTo(mMOP2PtsSafe);
		} else {

			mOpFlowCurr.copyTo(mOpFlowPrev);
			Imgproc.cvtColor(optFlowMatRgba, mOpFlowCurr,
					Imgproc.COLOR_RGBA2GRAY);

			Imgproc.goodFeaturesToTrack(mOpFlowCurr, mMOPopFlowCurr, 50, 0.01,
					20);
			mMOP2PtsCurr.fromArray(mMOPopFlowCurr.toArray());
			mMOP2PtsSafe.copyTo(mMOP2PtsPrev);
			mMOP2PtsCurr.copyTo(mMOP2PtsSafe);

			Video.calcOpticalFlowPyrLK(mOpFlowPrev, mOpFlowCurr, mMOP2PtsPrev,
					mMOP2PtsCurr, status, err);

			cornersPrev = mMOP2PtsPrev.toList();
			cornersCurr = mMOP2PtsCurr.toList();
			byteStatus = status.toList();

			double dis_X_uf = 0;
			double dis_Y_uf = 0;

			for (int i = 0; i < byteStatus.size() - 1; i++) {
				if (byteStatus.get(i) == 1) {
					Point pt = cornersCurr.get(i);
					Point pt2 = cornersPrev.get(i);

					pt.x += optFlowRect.x;
					pt.y += optFlowRect.y;

					pt2.x += optFlowRect.x;
					pt2.y += optFlowRect.y;

					Core.circle(mRgba, pt, 5, colorRed);

					dis_X_uf += pt.x - pt2.x;
					dis_Y_uf += pt.y - pt2.y;
				}
			}
			
			if ( dis_X_uf > 0 && dis_X_uf < motionThX) {
				dis_X_uf = 0;
			}
			if ( dis_X_uf < 0 && dis_X_uf > (-1*motionThX)) {
				dis_X_uf = 0;
			}
			if ( dis_Y_uf > 0 && dis_Y_uf < motionThY) {
				dis_Y_uf = 0;
			}
			if ( dis_Y_uf < 0 && dis_Y_uf > (-1*motionThY)) {
				dis_Y_uf = 0;
			}
			
			filterX.pushValue((int)dis_X_uf);
			filterY.pushValue((int)dis_Y_uf);
			
			float dis_X = filterX.getValue();
			float dis_Y = filterY.getValue();
			
			motionEventListener.notifyMotion(dis_X, dis_Y);
		}
		
		return mRgba;
	}

	private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
		Mat pointMatRgba = new Mat();
		Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
		Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL,
				4);

		return new Scalar(pointMatRgba.get(0, 0));
	}
	
	/** Initialize blackbackground drawable resource */
	private void initBlackBackground() {
		Resources res = getResources();
		blackBackground = res.getDrawable(R.drawable.black_bg);
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
		mColorPickHelpNotifTextView = (TextView) mColorPickLayout.findViewById(R.id.color_pick_help_notif_text);

		// If shoes' color hasn't been picked, show color_pick_help_notif_shoe
		if (!shoeColorPicked) {
			mColorPickHelpNotifTextView.setText(R.string.color_pick_help_notif_shoe);
		} else {
			// If shoes' color has been picked, show color_pick_help_notif_floor
			mColorPickHelpNotifTextView.setText(R.string.color_pick_help_notif_floor);
		}

		// Set background to transparent
		blackBackground.setAlpha(0);
		mColorPickLayout.setBackgroundDrawable(blackBackground);

		// Set opencvmode to crosshair mode, so opencv will draw a crosshair in
		// the center of image
		ModeManager.getModeManager().setCurrentMode(ModeManager.MODE_COLOR_PICK_CROSSHAIR);
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
			ModeManager.getModeManager().setCurrentMode(ModeManager.MODE_MAIN_MENU);
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
//		mOpenCvCameraView.setMaxFrameSize(500, 500);
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
	@SuppressLint("NewApi")
	private void restoreOrCreateMainMenu() {
		mMainMenuBackground = new ImageView(this);
		mMainMenuBackground.setImageResource(R.drawable.black_bg);
		mMainMenuBackground.setAlpha(0f);
		mMainMenuBackground.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		mMainMenuBackground.setScaleType(ImageView.ScaleType.FIT_XY);
		mFrameLayout.addView(mMainMenuBackground);

		mMainMenuTitle = new ImageView(this);
		mMainMenuTitle.setImageResource(R.drawable.main_menu_title2);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(screenOpenGLWidth / 3, screenOpenGLWidth / 8);
		lp.setMargins(screenOpenGLWidth / 5, screenOpenGLHeight / 10, 0, 1);
		mMainMenuTitle.setLayoutParams(lp);
		mFrameLayout.addView(mMainMenuTitle);

		LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		mMainMenuButtonListView = layoutInflater.inflate(R.layout.main_menu, null);
		FrameLayout.LayoutParams lp1 = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		lp1.setMargins(0, 300, 0, 0);
		mMainMenuButtonListView.setLayoutParams(lp1);
		mFrameLayout.addView(mMainMenuButtonListView);
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
	
	public double[] findBugNextDest() {
		int randomWidth;
		int randomHeight;
		int result;
		do {
			// We assume that the screenPixelWidth is the same with the opengl
			// coordinate
			int minX = imageOpenCvWidth / 4;
			int maxX = 3 * imageOpenCvWidth / 4;
			randomWidth = Global.randInt(minX, maxX);

			// We only find the destination in the upper part of the frame
			int minY = (int) (0 + OpenGLBug.radius * openCVGLRatioY);
			int maxY = 2 * imageOpenCvHeight / 3;
			randomHeight = Global.randInt(minY, maxY);

			// +1: outside the contour -1: inside the contour 0:lies on the
			// edge;
			result = isPointInFloor(randomWidth, randomHeight);
		} while (result != 1);

		destinationTarget = new org.opencv.core.Point(randomWidth, randomHeight);
		// Convert coordinates between opencv and opengl
		double resultDestX = (double) randomWidth / imageOpenCvWidth;
		// Revert y-axis ratio between opencv and opengl
		double resultDestY = 1 - (double) randomHeight / imageOpenCvHeight;
		double[] resultArray = { resultDestX, resultDestY };
		return resultArray;
	}

	public int isPointInFloor(int openCvWidth, int openCvHeight) {
		return isPointInFloor(openCvWidth, openCvHeight, false);
	}

	public int isPointInFloor(int openCvWidth, int openCvHeight,
			boolean calculateDist) {
		org.opencv.core.Point pt = new org.opencv.core.Point(openCvWidth,
				openCvHeight);
		MatOfPoint2f detectedFloorContour2f = new MatOfPoint2f();
		detectedFloorContours.get(0).convertTo(detectedFloorContour2f,
				CvType.CV_32FC2);
		int result = (int) Imgproc.pointPolygonTest(detectedFloorContour2f, pt,
				calculateDist);
		return result;
	}

	public void updateScore(int diff) {
		score = score + diff;
		updateScoreUI();
		int gameMode = ModeManager.getModeManager().getCurrentMode();
		switch(gameMode) {
		case ModeManager.MODE_TUTORIAL_1:
			if (score > 5) {
				
			}
			break;
		default:
			break;
		}
		
	}
	
	private void setRendererContourMassCenter() {
		if (ModeManager.getModeManager().getCurrentMode() != ModeManager.MODE_INITIAL) {
			List<Moments> mu = new ArrayList<Moments>(
					detectedShoesContours.size());
			List<OpenGLFire> mFireList = new ArrayList<OpenGLFire>(
					detectedShoesContours.size());
			for (int i = 0; i < detectedShoesContours.size(); i++) {
				Moments detectedMoment = Imgproc.moments(
						detectedShoesContours.get(i), false);
				mu.add(detectedMoment);
				double ratioX = detectedMoment.get_m10()
						/ (detectedMoment.get_m00() * imageOpenCvWidth);
				// Reverse the y axis because opencv uses y-down-axis while
				// opengl
				// uses y-up-axis
				double ratioY = 1 - detectedMoment.get_m01()
						/ (detectedMoment.get_m00() * imageOpenCvHeight);
				int x = (int) ratioX * imageOpenCvWidth;
				int y = (int) ratioY * imageOpenCvHeight;
				Core.circle(mRgba, new org.opencv.core.Point(x, y), 4, colorRed);
				mFireList.add(new OpenGLFire(ratioX, ratioY));
			}
			OpenGLRenderer.mFireList = mFireList;
		}
	}
	
	public void onClickAboutStartButton(View v) {
		mFrameLayout.removeView(mAboutView);
		restoreOrCreateMainMenu();
	}
	
	private void updateScoreUI() {

	}

	public void onClickMainMenuStartGame(View v) {
		mFrameLayout.removeView(mMainMenuTitle);
		mFrameLayout.removeView(mMainMenuButtonListView);
		mFrameLayout.removeView(mMainMenuBackground);

		// OpenGL shouldn't render anything right after clicking start game.
		ModeManager.getModeManager().setCurrentMode(ModeManager.MODE_INITIAL);

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
		mColorPickHelpNotif = mColorPickLayout.findViewById(R.id.overlay_color_pick_help);

		// Gets a reference to the color pick confirm buttons1 viewstub (shoe
		// color pick confirm buttons)
		mColorPickHelpConfirmButt1 = mColorPickLayout.findViewById(R.id.overlay_color_pick_confirm_button1);

		// Gets a reference to the color pick confirm buttons2 viewstub (floor
		// color pick confirm buttons)
		mColorPickHelpConfirmButt2 = mColorPickLayout.findViewById(R.id.overlay_color_pick_confirm_button2);

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
		if (!shoeColorPicked) {
			shoeColorPicked = true;
			mColorBlobDetector.setShoeHsvColor(mShoeColorPickHsv);
			ModeManager.getModeManager().setCurrentMode(ModeManager.MODE_SHOE_COLOR_PICKED);
			mColorPickHelpNotifTextView.setVisibility(View.GONE);
			mColorPickHelpNotifTextView.setText(R.string.color_pick_help_notif_confirm_shoe);
			mColorPickHelpNotifTextView.setTextColor(Color.WHITE);
			mColorPickHelpNotifTextView.setVisibility(View.VISIBLE);
			mColorPickHelpConfirmButt1.setVisibility(View.VISIBLE);
			mColorPickBottomBar.setVisibility(View.GONE);
		} else {
			floorColorPicked = true;
			mColorBlobDetector.setFloorHsvColor(mFloorColorPickHsv);
			ModeManager.getModeManager().setCurrentMode(ModeManager.MODE_FLOOR_COLOR_PICKED);
			mColorPickHelpNotifTextView.setVisibility(View.GONE);
			mColorPickHelpNotifTextView.setText(R.string.color_pick_help_notif_confirm_floor);
			mColorPickHelpNotifTextView.setTextColor(Color.WHITE);
			mColorPickHelpNotifTextView.setVisibility(View.VISIBLE);
			mColorPickHelpConfirmButt2.setVisibility(View.VISIBLE);
			mColorPickBottomBar.setVisibility(View.GONE);
		}

	}

	/** Button Close/Cancel clicked in the process of color picking */
	public void onClickColorPickCameraClose(View v) {
		// Set background to semi-transparent black
		blackBackground.setAlpha(200);
		mColorPickLayout.setBackgroundDrawable(blackBackground);

		// Goes back to the Color Pick Add rMode
		initializeInstructionMode();

		// Set mode to default
		ModeManager.getModeManager().setCurrentMode(ModeManager.MODE_INITIAL);

		// Set not picked shoes&floors' color yet
		shoeColorPicked = false;
		floorColorPicked = false;
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
		// To enter the ColorPickCameraMode
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
		ModeManager.getModeManager().setCurrentMode(ModeManager.MODE_MAIN_MENU);
	}

	/** Shoe color pick confirm cancel button clicked */
	public void onClickShoeColorPickConfirmCancel(View v) {
		mColorPickBottomBar.setVisibility(View.VISIBLE);
		mColorPickHelpNotifTextView.setVisibility(View.GONE);
		mColorPickHelpNotifTextView.setText(R.string.color_pick_help_notif_shoe);
		mColorPickHelpNotifTextView.setTextColor(Color.WHITE);
		mColorPickHelpNotifTextView.setVisibility(View.VISIBLE);
		mColorPickHelpConfirmButt1.setVisibility(View.GONE);
		ModeManager.getModeManager().setCurrentMode(ModeManager.MODE_COLOR_PICK_CROSSHAIR);

		// Set not picked shoes' color yet
		shoeColorPicked = false;
	}

	/** Shoe color pick confirm ok button clicked */
	public void onClickShoeColorPickConfirmOk(View v) {
		ModeManager.getModeManager().setCurrentMode(ModeManager.MODE_COLOR_PICK_CROSSHAIR);
		mColorPickHelpConfirmButt1.setVisibility(View.GONE);
		// To enter the ColorPickCameraMode
		initializeColorPickCameraMode();
	}

	/** Floor color pick confirm cancel button clicked */
	public void onClickFloorColorPickConfirmCancel(View v) {
		mColorPickBottomBar.setVisibility(View.VISIBLE);
		mColorPickHelpNotifTextView.setVisibility(View.GONE);
		mColorPickHelpNotifTextView.setText(R.string.color_pick_help_notif_floor);
		mColorPickHelpNotifTextView.setTextColor(Color.WHITE);
		mColorPickHelpNotifTextView.setVisibility(View.VISIBLE);
		mColorPickHelpConfirmButt2.setVisibility(View.GONE);
		ModeManager.getModeManager().setCurrentMode(ModeManager.MODE_COLOR_PICK_CROSSHAIR);
	}

	/** Floor color pick confirm ok button clicked */
	public void onClickFloorColorPickConfirmOk(View v) {
		mFrameLayout.removeView(mColorPickLayout);
		ModeManager.getModeManager().setCurrentMode(ModeManager.MODE_TUTORIAL_1);
		mHandler.postDelayed(mTutorial1InstructionUpdator, 2000);
	}
	
	public void onClickTutorial1InstructionOk(View v) {
        mFrameLayout.removeView(mTutorial1InstructionLayout);
        ModeManager.getModeManager().setCurrentMode(ModeManager.MODE_TUTORIAL_1);
	}
	
	public void onClickTutorial2InstructionOk(View v) {
		mFrameLayout.removeView(mTutorial2InstructionLayout);
		ModeManager.getModeManager().setCurrentMode(ModeManager.MODE_TUTORIAL_2);
	}

	protected Runnable mTutorial1InstructionUpdator = new Runnable() {
		@Override
		public void run() {
			// Inflates the Overlay Layout to be displayed above the Camera View
			LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
					.getSystemService(LAYOUT_INFLATER_SERVICE);
			mTutorial1InstructionLayout = (RelativeLayout) layoutInflater
					.inflate(R.layout.tutorial1_instruction, null, false);

			// Set background to semi-transparent black
			blackBackground.setAlpha(200);
			mTutorial1InstructionLayout.setBackgroundDrawable(blackBackground);

			mFrameLayout.addView(mTutorial1InstructionLayout);

			// Set mode to default
			ModeManager.getModeManager().setCurrentMode(ModeManager.MODE_INITIAL);
			// Clear fire list to prevent fire rendering
			OpenGLRenderer.mFireList.clear();
		}
	};
        
	protected Runnable mTutorial2InstructionUpdator = new Runnable() {
		@Override
		public void run() {
			// Inflates the Overlay Layout to be displayed above the Camera View
			LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
					.getSystemService(LAYOUT_INFLATER_SERVICE);
			mTutorial2InstructionLayout = (RelativeLayout) layoutInflater
					.inflate(R.layout.tutorial2_instruction, null, false);

			// Set background to semi-transparent black
			blackBackground.setAlpha(200);
			mTutorial1InstructionLayout.setBackgroundDrawable(blackBackground);

			mFrameLayout.addView(mTutorial2InstructionLayout);

			// Set mode to default
			ModeManager.getModeManager().setCurrentMode(ModeManager.MODE_INITIAL);
			// Clear fire list to prevent fire rendering
			OpenGLRenderer.mFireList.clear();
		}
	};

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if (Sensor.TYPE_ACCELEROMETER == event.sensor.getType()) {
			// Log.d(TAG, "accelerometer z:" + arg0.values[2]);
			int gameMode = ModeManager.getModeManager().getCurrentMode();
			switch (gameMode) {
			case ModeManager.MODE_COLOR_PICK_CROSSHAIR:
				if (event.values[2] < 8.5f) {
					mColorPickHelpNotifTextView.setVisibility(View.GONE);
					mColorPickHelpNotifTextView.setText(R.string.color_pick_help_notif_horizon);
					mColorPickHelpNotifTextView.setTextColor(Color.RED);
					mColorPickHelpNotifTextView.setVisibility(View.VISIBLE);
					ModeManager.getModeManager().setCurrentMode(ModeManager.MODE_COLOR_PICK_HOLD_WRONGLY);
					mColorPickCameraButton.setEnabled(false);
				}
				break;
			case ModeManager.MODE_COLOR_PICK_HOLD_WRONGLY:
				if (event.values[2] > 8.5f) {
					mColorPickHelpNotifTextView.setVisibility(View.GONE);
					if (!shoeColorPicked) {
						mColorPickHelpNotifTextView.setText(R.string.color_pick_help_notif_shoe);
					} else {
						mColorPickHelpNotifTextView.setText(R.string.color_pick_help_notif_floor);
					}
					mColorPickHelpNotifTextView.setTextColor(Color.WHITE);
					mColorPickHelpNotifTextView.setVisibility(View.VISIBLE);
					ModeManager.getModeManager().setCurrentMode(ModeManager.MODE_COLOR_PICK_CROSSHAIR);
					mColorPickCameraButton.setEnabled(true);
				}
			default:
				// Do nothing
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
	
}

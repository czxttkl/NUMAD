package edu.neu.mhealth.debug;

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
import org.opencv.video.Video;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class MainActivity extends Activity implements OnTouchListener,
		CvCameraViewListener2 {
	private static final String TAG = "OCVSample::Activity";

	private boolean mIsColorSelected = false;
	private Mat mRgba;
	private Mat mGray;
	private Scalar mBlobColorRgba;
	private Scalar mBlobColorHsv;
	private ColorDetector mDetector;
	private Mat mSpectrum;
	private Size SPECTRUM_SIZE;
	private Scalar CONTOUR_COLOR;

	private CameraView mOpenCvCameraView;
	private ConfigureView configureView;
	private BugManager bugManager;
	private Bug bug1;
	private int initalX = 0;
	private int initalY = 0;
	private int steps = 0;
	private int xOffset = 0;
	private int yOffset = 0;

	private int screenWidth;
	private int screenHeight;

	private int cameraWidth;
	private int cameraHeight;

	private List<MatOfPoint> contourFloor;
	private List<MatOfPoint> contourShoe;
	private MatOfPoint cornersMOP;
	private List<Point> corners;
	
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

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
				mOpenCvCameraView.setOnTouchListener(MainActivity.this);
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

		screenHeight = metrics.heightPixels;
		screenWidth = metrics.widthPixels;

		FrameLayout layout = new FrameLayout(this);

		mOpenCvCameraView = new CameraView(this);
		mOpenCvCameraView.setLayoutParams(new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
		mOpenCvCameraView.enableFpsMeter();

		cameraWidth = mOpenCvCameraView.getWidth();
		cameraHeight = mOpenCvCameraView.getHeight();

		configureView = new ConfigureView(this, screenWidth, screenHeight);

		bug1 = new Bug(this, 20, 30);
		bug1.setImageDrawable(getResources().getDrawable(R.drawable.bug));


		
		bugManager = BugManager.getBugManager();
		bugManager.addBug(bug1);

		layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		layout.addView(mOpenCvCameraView);
		layout.addView(configureView);
//		layout.addView(bug1);

		setContentView(layout);

		Log.e(TAG, "screen size: " + screenHeight + " " + screenWidth);
		Log.e(TAG, "camera size: " + cameraHeight + " " + cameraWidth);

	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {
		mRgba = new Mat(height, width, CvType.CV_8UC4);
		mGray = new Mat(height, width, CvType.CV_8UC1);
		mSpectrum = new Mat();
		mBlobColorRgba = new Scalar(255);
		mBlobColorHsv = new Scalar(255);
		SPECTRUM_SIZE = new Size(200, 64);
		CONTOUR_COLOR = new Scalar(255, 0, 0, 255);
		
		mOpFlowCurr = new Mat();
		mOpFlowPrev = new Mat();
		mMOPopFlowPrev = new MatOfPoint();
		mMOPopFlowCurr = new MatOfPoint();
		mMOP2PtsCurr = new MatOfPoint2f();
		mMOP2PtsPrev = new MatOfPoint2f();
		mMOP2PtsSafe = new MatOfPoint2f();
		status = new MatOfByte();
		err = new MatOfFloat();
	}

	public void onCameraViewStopped() {
		mRgba.release();
		mGray.release();
	}

	public boolean onTouch(View v, MotionEvent event) {

		// int x = (int)event.getX() - xOffset;
		// int y = (int)event.getY() - yOffset;

		Log.e(TAG, "touch coordinate: " + event.getX() + "  " + event.getY());

		int x = configureView.getFloorPosition().x;
		int y = configureView.getFloorPosition().y;

		// Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

		mIsColorSelected = true;

		return false; // don't need subsequent touch events
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();

		int cols = mRgba.cols();
		int rows = mRgba.rows();
		xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
		yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;
		
//		Log.e(TAG, "bug position: " + bug1.getPosition().x + "   " + bug1.getPosition().y);

		bug1.setLimit(mOpenCvCameraView.getWidth(), mOpenCvCameraView.getHeight());
		bug1.setOffset(xOffset, yOffset);

		this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				configureView.invalidate();
			}
		});

		/*
		if (mIsColorSelected) {

			int x = configureView.getFloorPosition().x - xOffset;
			int y = configureView.getFloorPosition().y - yOffset;
			Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");
			contourFloor = findObjectAt(x, y);

			Log.e(TAG, "contourFloor count: " + contourFloor.size());
			Imgproc.drawContours(mRgba, contourFloor, -1, CONTOUR_COLOR);

			x = configureView.getShoesPosition().x - xOffset;
			y = configureView.getShoesPosition().y - yOffset;

//			Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

			contourShoe = findObjectAt(x, y);

			Log.e(TAG, "contourShoe count: " + contourShoe.size());
			Imgproc.drawContours(mRgba, contourShoe, -1, CONTOUR_COLOR);

			configureView.disableDrawing();

			steps++;
			if (steps > 10) {
				this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						bugManager.moveBugs();
					}
				});

				steps = 0;
			}
			

			bugShoeCollisionCheck();			
		}
		*/
//		cornersMOP = new MatOfPoint();
//		Imgproc.goodFeaturesToTrack(mGray, cornersMOP, 50, 0.01, 30.0);
//		int y_corners = cornersMOP.rows();
//		corners = cornersMOP.toList();
//		
//		for (int i = 0; i < y_corners; i++) {
//			Core.circle(mRgba, corners.get(i), 8, new Scalar(255, 0, 0));
//		}

		
		if (mMOP2PtsPrev.rows() == 0) {
			Log.e(TAG, "optical unavia");
			Imgproc.cvtColor(mRgba, mOpFlowCurr, Imgproc.COLOR_RGBA2GRAY);
			mOpFlowCurr.copyTo(mOpFlowPrev);
			
			Imgproc.goodFeaturesToTrack(mOpFlowPrev, mMOPopFlowPrev, 50, 0.01, 20);
			mMOP2PtsPrev = new MatOfPoint2f(mMOPopFlowPrev.toArray());
			mMOP2PtsPrev.copyTo(mMOP2PtsSafe);
		} else {
			Log.e(TAG, "start optical flow");

			mOpFlowCurr.copyTo(mOpFlowPrev);
			Imgproc.cvtColor(mRgba, mOpFlowCurr, Imgproc.COLOR_RGBA2GRAY);
			
			Imgproc.goodFeaturesToTrack(mOpFlowCurr, mMOPopFlowCurr, 50, 0.01, 20);
			mMOP2PtsCurr = new MatOfPoint2f(mMOPopFlowCurr.toArray());
			mMOP2PtsSafe.copyTo(mMOP2PtsPrev);
			mMOP2PtsCurr.copyTo(mMOP2PtsSafe);
			
			Video.calcOpticalFlowPyrLK(mOpFlowPrev, mOpFlowCurr, mMOP2PtsPrev, mMOP2PtsCurr, status, err);
			
			cornersPrev = mMOP2PtsPrev.toList();
			cornersCurr = mMOP2PtsCurr.toList();
			byteStatus = status.toList();
			
			for (int i = 0; i < byteStatus.size() - 1; i++) {
				if (byteStatus.get(i) == 1) {
					Point pt = cornersCurr.get(i);
					Point pt2 = cornersPrev.get(i);
					
					Core.circle(mRgba, pt, 5, new Scalar(255, 0, 0));
					Core.line(mRgba, pt, pt2, new Scalar(255, 0, 0));
				}
			}
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

	private List<MatOfPoint> findObjectAt(int x, int y) {

		int cols = mRgba.cols();
		int rows = mRgba.rows();

		Log.i(TAG, "Cols and rows: (" + cols + ", " + rows + ")");

		if ((x < 0) || (y < 0))
			return null;
		if (x > cols)
			x = cols;
		if (y > rows)
			y = rows;

		Rect touchedRect = new Rect();

		touchedRect.x = (x > 4) ? x - 4 : 0;
		touchedRect.y = (y > 4) ? y - 4 : 0;

		touchedRect.width = (x + 4 < cols) ? x + 4 - touchedRect.x : cols
				- touchedRect.x;
		touchedRect.height = (y + 4 < rows) ? y + 4 - touchedRect.y : rows
				- touchedRect.y;

		Mat touchedRegionRgba = mRgba.submat(touchedRect);

		Mat touchedRegionHsv = new Mat();
		Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv,
				Imgproc.COLOR_RGB2HSV_FULL);

		// Calculate average color of touched region
		mBlobColorHsv = Core.sumElems(touchedRegionHsv);
		int pointCount = touchedRect.width * touchedRect.height;
		for (int i = 0; i < mBlobColorHsv.val.length; i++)
			mBlobColorHsv.val[i] /= pointCount;

		mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

		Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", "
				+ mBlobColorRgba.val[1] + ", " + mBlobColorRgba.val[2] + ", "
				+ mBlobColorRgba.val[3] + ")");

		mDetector = new ColorDetector();
		mDetector.setHsvColor(mBlobColorHsv);

		Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

		touchedRegionRgba.release();
		touchedRegionHsv.release();

		mDetector.process(mRgba);
		

		// Mat colorLabel = mRgba.submat(4, 68, 4, 68);
		// colorLabel.setTo(mBlobColorRgba);
		//
		// Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 +
		// mSpectrum.cols());
		// mSpectrum.copyTo(spectrumLabel);

		return mDetector.getContours();

	}

	private void bugShoeCollisionCheck() {

		Log.e(TAG, "bug position: " + bug1.getPosition().x + "   " + bug1.getPosition().y);

		if (contourShoe.size() > 0) {
			MatOfPoint2f shoeContour2f = new MatOfPoint2f();
			contourShoe.get(0).convertTo(shoeContour2f, CvType.CV_32FC2);
			double bugDistToShoe = Imgproc.pointPolygonTest(shoeContour2f,
					bug1.getPosition(), true);
			Log.e(TAG, "bug distance to shoe: " + bugDistToShoe + " contour num: " + contourShoe.size());
		}
		
		if (contourFloor.size() > 0) {
			MatOfPoint2f floorContour2f = new MatOfPoint2f();
			contourFloor.get(0).convertTo(floorContour2f, CvType.CV_32FC2);
			double bugDistToFloor = Imgproc.pointPolygonTest(floorContour2f, bug1.getPosition(), true);
			Log.e(TAG, "bug distance to floor: " + bugDistToFloor + " contour num: " + contourFloor.size());
		}			

	}
}

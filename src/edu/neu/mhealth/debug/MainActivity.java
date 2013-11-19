package edu.neu.mhealth.debug;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import android.R.color;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.sax.RootElement;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends Activity implements
		OnTouchListener, CvCameraViewListener2 {
	private static final String TAG = "ColorBlob::Activity";

	private boolean mIsColorSelected = false;
	private int mEnableProcessing = 0;

	private Mat mRgba;
	private Mat mIntermediateRGBMat;
	private Mat mIntermediateGreyMat;
	private Mat mZerosMat;
	private Mat mGray;
	
	List<MatOfPoint> contours;
	List<MatOfPoint> firstSeveral;
	private int several = 10;
	List<Double> moments;
	List<MatOfPoint> hull;
	Mat hierarchy;
	
	private Scalar mBlobColorRgba;
	private Scalar mBlobColorHsv;
	private BlobDetector mDetector;
	private Mat mSpectrum;
	private Size SPECTRUM_SIZE;
	private Scalar CONTOUR_COLOR;

	private BugManager bugManager;
	private Bug bug1;
	private Bug bug2;
	private int initalX = 0;
	private int initalY = 0;

	private List<android.hardware.Camera.Size> mResolutionList;
	private CameraView mOpenCvCameraView;
	private boolean resolutionReseted = false;
	private int steps = 0;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
				mOpenCvCameraView
						.setOnTouchListener(MainActivity.this);
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
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		RelativeLayout layout = new RelativeLayout(this);

		mOpenCvCameraView = new CameraView(this);
		mOpenCvCameraView.setLayoutParams(new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
		mOpenCvCameraView.enableFpsMeter();

		bug1 = new Bug(getApplicationContext(), 20, 30);
		bug1.setImageDrawable(getResources().getDrawable(R.drawable.bug));

		bugManager = BugManager.getBugManager();
		bugManager.addBug(bug1);

		layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		layout.addView(mOpenCvCameraView);
		layout.addView(bug1);

		setContentView(layout);

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
		mIntermediateRGBMat = new Mat(height, width, CvType.CV_8UC4);
		mGray = new Mat(height, width, CvType.CV_8UC1);
		mIntermediateGreyMat = new Mat(height, width, CvType.CV_8UC1);
		mZerosMat = new Mat(height,width, CvType.CV_8UC1, new Scalar(0, 0, 0, 0));

		mDetector = new BlobDetector();
		mSpectrum = new Mat();
		mBlobColorRgba = new Scalar(255);
		mBlobColorHsv = new Scalar(255);
		SPECTRUM_SIZE = new Size(200, 64);
		CONTOUR_COLOR = new Scalar(255, 0, 0, 255);

	}

	public void onCameraViewStopped() {
		mRgba.release();
	}

	public boolean onTouch(View v, MotionEvent event) {
		int cols = mRgba.cols();
		int rows = mRgba.rows();

		int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
		int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;
		
		Log.i(TAG, "view width: " + mOpenCvCameraView.getWidth());
		Log.i(TAG, "view height: " + mOpenCvCameraView.getHeight());

		int x = (int) event.getX() - xOffset;
		int y = (int) event.getY() - yOffset;

		Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

		if ((x < 0) || (y < 0) || (x > cols) || (y > rows))
			return false;

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

		mDetector.setHsvColor(mBlobColorHsv);

		Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

		mIsColorSelected = true;

		touchedRegionRgba.release();
		touchedRegionHsv.release();

		mEnableProcessing++;

		return false; // don't need subsequent touch events
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

		android.hardware.Camera.Size resolution = mOpenCvCameraView
				.getResolution();
		String caption = Integer.valueOf(resolution.width).toString() + "x"
				+ Integer.valueOf(resolution.height).toString();
		Log.e(TAG, caption);

		mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();

		hull = new ArrayList<MatOfPoint>();
		contours = new ArrayList<MatOfPoint>();
		moments = new ArrayList<Double>();

		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,
				new Size(3, 3));
		hierarchy = new Mat();

		Imgproc.GaussianBlur(mGray, mIntermediateGreyMat, new Size(7, 7), 3.0);
		Imgproc.threshold(mIntermediateGreyMat, mIntermediateGreyMat, -1, 255,
				Imgproc.THRESH_OTSU);

		Mat temp = mIntermediateGreyMat.clone();
		Imgproc.findContours(temp, contours, hierarchy, Imgproc.RETR_TREE,
				Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

		hierarchy.release();
		
		for (int idx = 0; idx < contours.size(); idx++) {
			MatOfInt hullitem = new MatOfInt();
			Imgproc.convexHull(contours.get(idx), hullitem, false);
			List<Point> oldPoints = contours.get(idx).toList();
			List<Point> newPoints = new ArrayList<Point>();
			List<Integer> index = hullitem.toList();
			for (int i = 0; i < index.size(); i++) {
				newPoints.add(oldPoints.get(index.get(i)));
			}
			MatOfPoint newContour = new MatOfPoint();
			newContour.fromList(newPoints);
			hull.add(newContour);
		}

		Collections.sort(hull, new Comparator<MatOfPoint>() {

			@Override
			public int compare(MatOfPoint lhs, MatOfPoint rhs) {
				// TODO Auto-generated method stub
				double left = Imgproc.boundingRect(lhs).area();
				double right = Imgproc.boundingRect(rhs).area();

				if (left > right) {
					return -1;
				} else if (left < right) {
					return 1;
				} else {
					return 0;
				}
			}
		});

		firstSeveral = new ArrayList<MatOfPoint>();
		several = hull.size() > several ? several : hull.size();
		for (int i = 0; i < several; i++) {
			firstSeveral.add(hull.get(i));
		}



		Imgproc.drawContours(mRgba, hull, -1, new Scalar(Math.random() * 255,
				Math.random() * 255, Math.random() * 255));
			

		if (mIsColorSelected) {
			mDetector.process(mRgba);
			List<MatOfPoint> contours = mDetector.getContours();
			Log.e(TAG, "Contours count: " + contours.size());
			Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

			Mat colorLabel = mRgba.submat(4, 68, 4, 68);
			colorLabel.setTo(mBlobColorRgba);

			Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70,
					70 + mSpectrum.cols());
			mSpectrum.copyTo(spectrumLabel);

			steps++;
			if (steps > mOpenCvCameraView.getFps() / 2) {
				this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						bugManager.moveBugs();
					}
				});
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
}

package edu.neu.mhealth.debug;

import java.io.File;
import java.io.IOException;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import edu.neu.mhealth.debug.helper.Global;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class CameraActivity extends Activity implements CvCameraViewListener2 {

	/* Basic Variables */
	private final String TAG = Global.APP_LOG_TAG;
	private long cameraStartTime;
	private static final Scalar RECT_COLOR_RED = new Scalar(255, 0, 0, 255);
	private static final Scalar RECT_COLOR_GREEN = new Scalar(0, 255, 0, 255);

	/* OpenCv Variables */
	private Mat mRgba;
	private Mat mGray;
	private Mat toBeDetectedMat;
	private Mat detectResult;
	private Mat nullMat;
	
	private Rect captureRect;
	private Point captureRectPt1;
	private Point captureRectPt2;
	
	private boolean srcCaptured = false;
	private boolean useGrayChannel = false;
	private int resultMatRows;
	private int resultMatCols;
	private CameraBridgeViewBase mOpenCvCameraView;
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	/*
	 * Activity Callbacks
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_camera);
		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HellpOpenCvView);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null) {
			mOpenCvCameraView.disableView();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this,
				mLoaderCallback);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null) {
			mOpenCvCameraView.disableView();
		}
	}

	/*
	 * Opencv Callbacks
	 */
	@Override
	public void onCameraViewStarted(int width, int height) {
		//Initialize Mats which will be used in onCameraFrame
		toBeDetectedMat = new Mat();
		nullMat = new Mat();
		
		cameraStartTime = System.currentTimeMillis();
		
		//Capture area is 200*200
		captureRectPt1 = new Point(width/2 - 100, height/2 - 100);
		captureRectPt2 = new Point(width/2 + 100, height/2 + 100);
		captureRect = new Rect(captureRectPt1, captureRectPt2);
	
		resultMatRows = height - 200 + 1;
		resultMatCols = width - 200 + 1;
		
		Log.i(TAG, "onCameraViewStarted:" + width + "*" + height);
	}

	@Override
	public void onCameraViewStopped() {

	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		//It's actually bgra mat!
		mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();
		if (System.currentTimeMillis() - cameraStartTime < 5000) {
			 Core.rectangle(mRgba, captureRectPt1, captureRectPt2, RECT_COLOR_RED, 3);
		} else {
			if (!srcCaptured) {
				File path = Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
				String filename = "captured.png";
				File file = new File(path, filename);
				filename = file.toString();
				
				if (!useGrayChannel) {
					mRgba.submat(captureRect).copyTo(toBeDetectedMat);
					Mat toBeDetectedMatRGBA = new Mat();
					Imgproc.cvtColor(toBeDetectedMat, toBeDetectedMatRGBA, Imgproc.COLOR_BGRA2RGBA);
					Highgui.imwrite(filename, toBeDetectedMatRGBA);
				} else {
					mGray.submat(captureRect).copyTo(toBeDetectedMat);
					Highgui.imwrite(filename, toBeDetectedMat);
				}
				
				// toBeDetectedMat = Highgui.imread(filename, Highgui.CV_LOAD_IMAGE_GRAYSCALE);		
				detectResult = new Mat(resultMatRows, resultMatCols, CvType.CV_32FC1);
				srcCaptured = true;
			} else {
				// Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_RGBA2GRAY);
				// Create result mat
				detectResult.create(resultMatRows, resultMatCols, CvType.CV_32FC1);
				// Choose one method for template match
				int matchMethod = Imgproc.TM_SQDIFF_NORMED;
				// Match template
				if (!useGrayChannel) {
					Imgproc.matchTemplate(mRgba, toBeDetectedMat, detectResult,
							matchMethod);
				} else {
					Imgproc.matchTemplate(mGray, toBeDetectedMat, detectResult,
							matchMethod);
				}
				// Normalize
				Core.normalize(detectResult, detectResult, 0, 1, Core.NORM_MINMAX, -1, nullMat);
				// Create a point for match location
				Point matchLoc;
				MinMaxLocResult minMaxLocResult = Core.minMaxLoc(detectResult, nullMat);
				// For SQDIFF and SQDIFF_NORMED, the best matches are lower values.
				// For all the other methods, the higher the better.
				if (matchMethod == Imgproc.TM_SQDIFF
						|| matchMethod == Imgproc.TM_SQDIFF_NORMED) {
					matchLoc = minMaxLocResult.minLoc;
					Log.i(TAG, "detected min value:" + minMaxLocResult.minVal);
				} else {
					matchLoc = minMaxLocResult.maxLoc;
				}
				// Start draw rectangle
				Point pt2 = new Point(matchLoc.x + toBeDetectedMat.cols(),
						matchLoc.y + toBeDetectedMat.rows());
				Core.rectangle(mRgba, matchLoc, pt2, RECT_COLOR_GREEN, 3);
				//Release detectResult 
				detectResult.release();
			}
		}
		return mRgba;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.camera, menu);
	    return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	     	case R.id.pick_another_target:
	     		cameraStartTime = System.currentTimeMillis();
	     		srcCaptured = false;
	     		return true;
	     	case R.id.pick_channel:
	     		cameraStartTime = System.currentTimeMillis();
	     		srcCaptured = false;
	     		if(item.getTitle().equals("Use gray channel")) {
	     			useGrayChannel = true;
	     			item.setTitle("Use rgba channel");
	     		} else {
	     			useGrayChannel = false;
	     			item.setTitle("Use gray channel");
	     		}
	     		return true;
	        default:
	        	return super.onOptionsItemSelected(item);
	        }
	}
	
}

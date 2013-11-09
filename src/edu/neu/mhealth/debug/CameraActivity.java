package edu.neu.mhealth.debug;

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
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import edu.neu.mhealth.debug.helper.Global;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.WindowManager;

public class CameraActivity extends Activity implements CvCameraViewListener2 {

	/*Basic Variables*/
	private final String TAG = Global.APP_LOG_TAG;
	private static final Scalar RECT_COLOR = new Scalar(0, 255, 0, 255);
	
	/*OpenCv Variables*/
	private Mat mRgba;
	private Mat toBeDetectedMat;
	private Mat detectResult;
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
	 *   Activity Callbacks
	 *   
	 *   */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null) {
			mOpenCvCameraView.disableView();
		}
	}

	
	
	/*
	 *   Opencv Callbacks
	 *   
	 *   */
	@Override
	public void onCameraViewStarted(int width, int height) {
		Bitmap toBeDetectedBitmap = null;
		try {
			toBeDetectedBitmap = BitmapFactory.decodeStream(getAssets().open("shoes.jpg"));
			toBeDetectedMat = new Mat();
			Utils.bitmapToMat(toBeDetectedBitmap, toBeDetectedMat);
			Log.e(TAG, "load detect object image success. Mat:" + toBeDetectedMat.rows() + "*" + toBeDetectedMat.cols());
		} catch (IOException e) {
			Log.e(TAG, "load detect object image failed");
		}
	}

	@Override
	public void onCameraViewStopped() {
		
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		mRgba = inputFrame.rgba();
		//Choose one method for template match
		int matchMethod = Imgproc.TM_CCORR_NORMED;
		//Create result mat
		int resultRows = mRgba.rows() - toBeDetectedMat.rows() + 1;
		int resultCols =  mRgba.cols() - toBeDetectedMat.cols() + 1;
//		Log.i(TAG, "Create result mat:" + resultCols + "*" + resultRows); 
		Log.i(TAG, "mRgba mat:" + mRgba.rows() + "*" + mRgba.cols()); 
		detectResult = new Mat(resultRows, resultCols, CvType.CV_32FC1);
//		detectResult.create(resultCols, resultRows, CvType.CV_32FC1);
		//Match template
		Imgproc.matchTemplate(mRgba, toBeDetectedMat, detectResult, matchMethod);
		//Normalize
		Core.normalize( detectResult, detectResult, 0, 1, Core.NORM_MINMAX, -1, null );
		// Create a point for match location
		Point matchLoc;
		MinMaxLocResult minMaxLocResult = Core.minMaxLoc( detectResult, null );
		// For SQDIFF and SQDIFF_NORMED, the best matches are lower values. 
		// For all the other methods, the higher the better
		if( matchMethod  == Imgproc.TM_SQDIFF || matchMethod == Imgproc.TM_SQDIFF_NORMED ){ 
			matchLoc = minMaxLocResult.minLoc; 
		}
		else { 
			matchLoc = minMaxLocResult.maxLoc; 
		}
		// Start draw rectangle
		Point pt2 = new Point(matchLoc.x + toBeDetectedMat.cols(), matchLoc.y + toBeDetectedMat.rows());
		Log.i(TAG, "point1:" + matchLoc.x + "," + matchLoc.y + "  point2:" + pt2.x + "," + pt2.y);
		Core.rectangle(mRgba, matchLoc, pt2, RECT_COLOR, 3);
		return mRgba;
	}

	
}

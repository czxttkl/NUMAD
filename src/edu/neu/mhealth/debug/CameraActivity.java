package edu.neu.mhealth.debug;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import edu.neu.mhealth.debug.helper.Global;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class CameraActivity extends Activity implements CvCameraViewListener2 {

	/*Basic Variables*/
	/** Debug Tag */
	private final String TAG = Global.APP_LOG_TAG;
	/** The game activity's framelayout. Use this to handle adding/removing surfaceviews*/
	FrameLayout mFrameLayout;
	
	/*OpenCv Variables*/
	private CameraBridgeViewBase mOpenCvCameraView;
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS: {
					Log.i(TAG, "OpenCV loaded successfully");
					restoreOrCreateGLSurfaceView();
					restoreOrCreateJavaCameraView();
				}
				break;
				default: {
					super.onManagerConnected(status);
				}
				break;
			}
		}

	};
	
	
	/*OpenGl Variables*/
	/** OpenGL surface view for displaying bugs*/
	MyGLSurfaceView mGLSurfaceView;
	
	/*
	 *   Activity Callbacks
	 *   
	 *   */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_camera);	
		mFrameLayout = (FrameLayout)findViewById(R.id.MyFrameLayout);
	}

	@Override
	protected void onPause() {
		if (mOpenCvCameraView != null) {
			mOpenCvCameraView.disableView();
			saveAndRemoveSurfaceViews();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
		super.onResume();
	}
	
	/*
	 *   Opencv Callbacks
	 *   
	 *   */
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
	 *   Save/Restore States
	 *   
	 *   */
	private void saveAndRemoveSurfaceViews() {
		mFrameLayout.removeAllViewsInLayout();
	}
	
	/**
	 * Restore or create SurfaceView for bugs.
	 * This method is called after OpenCV library is loaded successfully.
	 */
	private void restoreOrCreateGLSurfaceView() {
		mGLSurfaceView = new MyGLSurfaceView(this);
		//CameraView must be added after GLSurfaceView so that GLSurfaceView could appear upon CameraView
		mFrameLayout.addView(mGLSurfaceView, 0);
	}
	
	/**
	 * Restore or create SurfaceView for opencv CameraView.
	 * This method is called after OpenCV library is loaded successfully.
	 */
	private void restoreOrCreateJavaCameraView() {
		mOpenCvCameraView = new JavaCameraView(CameraActivity.this, CameraBridgeViewBase.CAMERA_ID_ANY);
		mOpenCvCameraView.enableFpsMeter();
		//CameraView must be added after GLSurfaceView so that GLSurfaceView could appear upon CameraView
		mFrameLayout.addView(mOpenCvCameraView, 1);
		mOpenCvCameraView.setCvCameraViewListener(CameraActivity.this);
		mOpenCvCameraView.enableView();
	}
	
}

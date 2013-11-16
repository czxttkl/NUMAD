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
	private final String TAG = Global.APP_LOG_TAG;
	FrameLayout mFrameLayout;
	MyGLSurfaceView mGLSurfaceView;
	
	/*OpenCv Variables*/
	private CameraBridgeViewBase mOpenCvCameraView;
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS: {
					Log.i(TAG, "OpenCV loaded successfully");
//					mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.JavaCameraView);
					restoreOrCreateGLSurfaceView();
					mOpenCvCameraView = new JavaCameraView(CameraActivity.this, CameraBridgeViewBase.CAMERA_ID_ANY);
					mFrameLayout.addView(mOpenCvCameraView);
					mOpenCvCameraView.setCvCameraViewListener(CameraActivity.this);
					mOpenCvCameraView.enableView();
//					mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
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
	private GLSurfaceView mGLView;
	
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
			saveGLSurfaceView();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
//		findViewById(R.id.)
//		mGLView = new MyGLSurfaceView(this);
//		mFrameLayout.addView(mGLView);
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
//		if (mOpenCvCameraView != null) {
//			mOpenCvCameraView.disableView();
//		}
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
	private void saveGLSurfaceView() {
//		mFrameLayout.removeView(mGLSurfaceView);
//		mFrameLayout.removeView(mOpenCvCameraView);
		mFrameLayout.removeAllViewsInLayout();
	}
	
	private void restoreOrCreateGLSurfaceView() {
		
//		mGLSurfaceView = (MyGLSurfaceView)findViewById(R.id.MyGLSurfaceView);
//		if (mGLSurfaceView == null) {
		mGLSurfaceView = new MyGLSurfaceView(this);
		mFrameLayout.addView(mGLSurfaceView);
//		}
//		mGLSurfaceView.setVisibility(SurfaceView.VISIBLE);
	}
	
}

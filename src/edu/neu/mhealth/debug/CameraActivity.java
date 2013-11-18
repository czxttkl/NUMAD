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
import android.view.Menu;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;

public class CameraActivity extends Activity implements CvCameraViewListener2, SensorEventListener   {

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
					restoreOrCreateJavaCameraView();
					restoreOrCreateGLSurfaceView();
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
	private MyGLSurfaceView mGLSurfaceView;
	
	/*Sensor Variables*/
	private final float MAX_ROATE_DEGREE = 1.0f;
	private SensorManager mSensorManager;
	private Sensor mOrientationSensor;
    private float mDirection;
    private float mTargetDirection;
    private AccelerateInterpolator mInterpolator;
    protected final Handler mHandler = new Handler();
    private boolean mStopDrawing;
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
		
		mDirection = 0.0f;
        mTargetDirection = 0.0f;
        mInterpolator = new AccelerateInterpolator();
        mStopDrawing = true;
		  // sensor manager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	}

	@Override
	protected void onPause() {
		if (mOpenCvCameraView != null) {
			mOpenCvCameraView.disableView();
			saveAndRemoveSurfaceViews();
		}
		
		mStopDrawing = true;
        if (mOrientationSensor != null) {
            mSensorManager.unregisterListener(this);
        }
        
		super.onPause();
	}

	@Override
	protected void onResume() {
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
		
		if (mOrientationSensor != null) {
			mSensorManager.registerListener(this, mOrientationSensor, SensorManager.SENSOR_DELAY_GAME);
		}
		mStopDrawing = false;
	    mHandler.postDelayed(mRotationUpdater, 20);
	    
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
		mFrameLayout.addView(mGLSurfaceView);
		mGLSurfaceView.setZOrderMediaOverlay(true);
		mGLSurfaceView.setZOrderOnTop(true);
	}
	
	/**
	 * Restore or create SurfaceView for opencv CameraView.
	 * This method is called after OpenCV library is loaded successfully.
	 */
	private void restoreOrCreateJavaCameraView() {
		mOpenCvCameraView = new JavaCameraView(CameraActivity.this, CameraBridgeViewBase.CAMERA_ID_ANY);
		mOpenCvCameraView.enableFpsMeter();
		//CameraView must be added after GLSurfaceView so that GLSurfaceView could appear upon CameraView
		mFrameLayout.addView(mOpenCvCameraView);
		mOpenCvCameraView.setCvCameraViewListener(CameraActivity.this);
		mOpenCvCameraView.enableView();
	}

	/*
	 *   Sensor Callbacks
	 *   
	 *   */
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		float direction = arg0.values[0] * -1.0f;
        mTargetDirection = normalizeDegree(direction);
	}
	
    protected Runnable mRotationUpdater = new Runnable() {
        @Override
        public void run() {
            if (!mStopDrawing) {
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
                            + ((to - mDirection) * mInterpolator.getInterpolation(Math
                                    .abs(distance) > MAX_ROATE_DEGREE ? 0.4f : 0.3f)));
//                    float rotateDiff =  mDirection - mDirectionNew;
                    updateDirection(mDirectionNew);
                    mDirection = mDirectionNew;
                }

                mHandler.postDelayed(mRotationUpdater, 20);
            }
        }
    };
	
    private float normalizeDegree(float degree) {
        return (degree + 720) % 360;
    }
    
    private void updateDirection(float mDirectionNew) {
//    	mGLSurfaceView.mRenderer.rotateDegree = mGLSurfaceView.mRenderer.rotateDegree + rotateDiff;
    	mGLSurfaceView.mRenderer.globalRotateDegree = mDirectionNew;
    	
    }
}

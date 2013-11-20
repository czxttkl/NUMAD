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
	private float speedX;
	private float speedY;
	private float linearAccX;
	private float linearAccY;
	private float linearAccZ;

	private long lastTimeActiveX = 0;
	

	// private final long PACE_TWO_OPPOSITE_PEAK_INTERVAL = 2000;

	/*
	 * Activity Callbacks
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_camera);
		mFrameLayout = (FrameLayout) findViewById(R.id.MyFrameLayout);
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
		mOpenCvCameraView = new JavaCameraView(CameraActivity.this,
				CameraBridgeViewBase.CAMERA_ID_ANY);
		mOpenCvCameraView.enableFpsMeter();
		// CameraView must be added after GLSurfaceView so that GLSurfaceView
		// could appear upon CameraView
		mFrameLayout.addView(mOpenCvCameraView);
		mOpenCvCameraView.setCvCameraViewListener(CameraActivity.this);
		mOpenCvCameraView.enableView();
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
		mHandler.postDelayed(mEyeLocationUpdater, 20);
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

	private int count = 0;
	private float[] linearAccYArray = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	private float linearAccYAve = 0;
	private boolean moved = false;
	private boolean couldRedetect = true;
	private boolean peaked = false;
	private int direction = 0;
	private final int LEFT = -1;
	private final int RIGHT = 1;
	private int flip = 0;
	// private long lastTimeExceedPositiveThresholdX = 0;
	// private long lastTimeExceedNegativeThresholdX = 0;
	// private long lastTimeExceedPositiveThresholdY = 0;
	// private long lastTimeExceedNegativeThresholdY = 0;
	private long lastTimeActivePosY = 0;
	private long lastTimeActiveNegY = 0;
	private final long ONE_PACE_INTERVAL = 1000;
	private long moveTime;
	private final float ACCELEROMETER_THRESHOLD = 0.4f;// }
	private final float SPEED_PEAK_THRESHOLD = 5.0f;
	private final float SPEED_CONSTANT = 0.01f;
	@Override
	public void onSensorChanged(SensorEvent arg0) {
		if (Sensor.TYPE_ORIENTATION == arg0.sensor.getType()) {
			float direction = arg0.values[0] * -1.0f;
			mTargetDirection = normalizeDegree(direction);
		} else {
			if (Sensor.TYPE_LINEAR_ACCELERATION == arg0.sensor.getType()) {
				long now = System.currentTimeMillis();
				linearAccX = arg0.values[0];
				linearAccY = arg0.values[1];
				linearAccZ = arg0.values[2];

				count = (int) (now % 15);
				linearAccYArray[count] = linearAccY;
				linearAccYAve = average(linearAccYArray);
				// Log.d(TAG, "liear:" + linearAccX + "," + linearAccY);
				// Log.d(TAG, "distanceY:" + mGLSurfaceView.mRenderer.eyeX +
				// ", speedY" + speedY);

				// if (Math.abs(linearAccX) > ACCELEROMETER_THRESHOLD) {
				// lastTimeActiveX = now;
				//
				if (!moved && couldRedetect) {
						if ((direction >=0 || now - moveTime > 4000) && linearAccYAve > ACCELEROMETER_THRESHOLD) {
							direction = LEFT;
							moved = true;
							couldRedetect = false;
							moveTime = now;
							Log.d(TAG, "czx !moved. turn left:" + linearAccYAve);
						}

						if ((direction <=0 || now - moveTime > 4000) && linearAccYAve < -ACCELEROMETER_THRESHOLD) {
							direction = RIGHT;
							moved = true;
							couldRedetect = false;
							moveTime = now;
							Log.d(TAG, "czx !moved. turn right:" + linearAccYAve);
						}
					
				} else {
					
					if (moved) {
						speedY = speedY + linearAccYAve;
//						if (Math.abs(speedY) > SPEED_PEAK_THRESHOLD) {
//							moveTime = now;
//						}
						if (direction == LEFT) {
							if (now - moveTime > 300 && speedY < 3f) {
								speedY = 0;
								moved = false;
								moveTime = now;
								Log.d(TAG, "czx speed 0. stop turn left");
							}
						}
						if (direction == RIGHT) {
							if (now - moveTime > 300 && speedY > -3f) {
								moved = false;
								speedY = 0;
								moveTime = now;
								Log.d(TAG, "czx speed 0. stop turn right");
							}
						}
						
						if (now - moveTime > 1500 || Math.abs(speedY) > 30 ) {
							moved = false;
							speedY = 0;
							couldRedetect = true;
							moveTime = now;
						}
					}
					Log.d(TAG, "czx speedY:" + speedY + ",linearAccYAve:" + linearAccYAve);
					
					if(!moved && !couldRedetect) {
//						Log.d(TAG, "czx stop move: linearaccyave:" + linearAccYAve);
						if (direction == LEFT && linearAccY > -ACCELEROMETER_THRESHOLD) {
							couldRedetect = true;
							Arrays.fill(linearAccYArray, 0);
							Log.d(TAG, "czx could redetect after left. lineary > -threshold. :" + linearAccYAve);
						}
						if (direction == RIGHT && linearAccY < ACCELEROMETER_THRESHOLD) {
							couldRedetect = true;
							Arrays.fill(linearAccYArray, 0);
							Log.d(TAG, "czx could redetec after right. lineary < threshold:" + linearAccYAve);
						}
					}
					
					
//					else {
//						
//
//							if (flip == 0
//									&& linearAccYAve < -ACCELEROMETER_THRESHOLD) {
//								flip = flip + 1;
//								Log.d(TAG, "czx left flip 1");
//							}
//
//							if (flip == 1
//									&& linearAccYAve > -ACCELEROMETER_THRESHOLD) {
//								flip = flip + 1;
//								Log.d(TAG, "czx left flip 2");
//							}
//
//							if (flip == 2) {
//								speedY = 0;
//								flip = 0;
//								moved = false;
//								direction = 0;
//								Log.d(TAG, "czx left flip 2:refresh");
//							}
//						}
//
//						
//
//							if (flip == 0
//									&& linearAccYAve > ACCELEROMETER_THRESHOLD) {
//								flip = flip + 1;
//								Log.d(TAG, "czx right flip 1");
//							}
//
//							if (flip == 1
//									&& linearAccYAve < ACCELEROMETER_THRESHOLD) {
//								flip = flip + 1;
//								Log.d(TAG, "czx right flip 2");
//							}
//
//							if (flip == 2) {
//								speedY = 0;
//								flip = 0;
//								moved = false;
//								direction = 0;
//								Log.d(TAG, "czx right flip 2:refresh");
//							}
//						}
//
//					}
				}

				// if (now - Math.abs(lastTimeActiveY) > ONE_PACE_INTERVAL) {
				// linearAccY = 0;
				// speedY = 0;
				// }
				//
				// if (now - lastTimeActiveX > ONE_PACE_INTERVAL) {
				// linearAccX = 0;
				// speedX = 0;
				// }

				// speedX = speedX + linearAccX * SPEED_CONSTANT;
				// speedY = speedY + linearAccY * SPEED_CONSTANT;

				mGLSurfaceView.mRenderer.eyeY = mGLSurfaceView.mRenderer.eyeY
						+ speedX;
				mGLSurfaceView.mRenderer.eyeX = mGLSurfaceView.mRenderer.eyeX
						- speedY * SPEED_CONSTANT;
				// if (now - lastTimeExceedPositiveThresholdX >
				// PACE_ONE_PEAK_INTERVAL
				// || now - lastTimeExceedNegativeThresholdX >
				// PACE_ONE_PEAK_INTERVAL) {
				// speedX = 0;
				// mGLSurfaceView.mRenderer.eyeY = mGLSurfaceView.mRenderer.eyeY
				// + speedX * SPEED_CONSTANT;
				//
				// }
				//
				// if (now - lastTimeExceedPositiveThresholdY >
				// PACE_ONE_PEAK_INTERVAL
				// || now - lastTimeExceedNegativeThresholdY >
				// PACE_ONE_PEAK_INTERVAL) {
				// speedY = 0;
				// mGLSurfaceView.mRenderer.eyeX = mGLSurfaceView.mRenderer.eyeX
				// + speedY * SPEED_CONSTANT;
				// }
				//
				// if (linearAccX > ACCELEROMETER_PEAK_THRESHOLD) {
				// speedX = speedX - linearAccX;
				// lastTimeExceedPositiveThresholdX = now;
				// mGLSurfaceView.mRenderer.eyeY = mGLSurfaceView.mRenderer.eyeY
				// + speedX * SPEED_CONSTANT;
				// }
				//
				// if (linearAccX < -ACCELEROMETER_PEAK_THRESHOLD) {
				// speedX = speedX - linearAccX;
				// lastTimeExceedNegativeThresholdX = now;
				// mGLSurfaceView.mRenderer.eyeY = mGLSurfaceView.mRenderer.eyeY
				// + speedX * SPEED_CONSTANT;
				// }
				//
				// if (linearAccY > ACCELEROMETER_PEAK_THRESHOLD) {
				// speedY = speedY + linearAccY;
				// lastTimeExceedPositiveThresholdY = now;
				// mGLSurfaceView.mRenderer.eyeX = mGLSurfaceView.mRenderer.eyeX
				// + speedY * SPEED_CONSTANT;
				// }
				//
				// if (linearAccY < -ACCELEROMETER_PEAK_THRESHOLD) {
				// lastTimeExceedNegativeThresholdY = now;
				// mGLSurfaceView.mRenderer.eyeX = mGLSurfaceView.mRenderer.eyeX
				// + speedY * SPEED_CONSTANT;
				// }

				// if (Math.abs(speedX) > SPEED_PEAK_THRESHOLD) {
				// if (speedX < 0) {
				// speedX = -SPEED_PEAK_THRESHOLD;
				// } else {
				// speedX = SPEED_PEAK_THRESHOLD;
				// }
				// // mGLSurfaceView.mRenderer.eyeY =
				// (mGLSurfaceView.mRenderer.eyeY + speedX) * 0.01f;
				// }
				//
				// if (Math.abs(speedY) > SPEED_PEAK_THRESHOLD) {
				// if (speedY < 0) {
				// speedY = -SPEED_PEAK_THRESHOLD;
				// } else {
				// speedY = SPEED_PEAK_THRESHOLD;
				// }
				// // mGLSurfaceView.mRenderer.eyeX =
				// (mGLSurfaceView.mRenderer.eyeX + speedY) * 0.01f;
				// }

				// mGLSurfaceView.mRenderer.eyeY =
				// (mGLSurfaceView.mRenderer.eyeY + speedX) * 0.01f;
				// mGLSurfaceView.mRenderer.eyeX =
				// (mGLSurfaceView.mRenderer.eyeX + speedY) * 0.01f;
				// if (linearAccY > PACE_PEAK_THRESHOLD) {
				// if (now - lastTimeExceedPositiveThresholdY >
				// PACE_ONE_PEAK_INTERVAL)
				// lastTimeExceedPositiveThresholdY = now;
				// }
				//
				// if (linearAccY < -PACE_PEAK_THRESHOLD) {
				// if (now - lastTimeExceedNegativeThresholdY >
				// PACE_ONE_PEAK_INTERVAL)
				// lastTimeExceedNegativeThresholdY = now;
				// }
				//
				// if (linearAccX > PACE_PEAK_THRESHOLD) {
				// if (now - lastTimeExceedPositiveThresholdX >
				// PACE_ONE_PEAK_INTERVAL)
				// lastTimeExceedPositiveThresholdX = now;
				// }
				//
				// if (linearAccX < -PACE_PEAK_THRESHOLD) {
				// if (now - lastTimeExceedNegativeThresholdX >
				// PACE_ONE_PEAK_INTERVAL)
				// lastTimeExceedNegativeThresholdX = now;
				// }
				//
				// if (lastTimeExceedPositiveThresholdY -
				// lastTimeExceedNegativeThresholdY <
				// PACE_TWO_OPPOSITE_PEAK_INTERVAL ) {
				// if (lastTimeExceedPositiveThresholdY -
				// lastTimeExceedNegativeThresholdY > 0) {
				// Log.d(TAG, "move left");
				// } else {
				// Log.d(TAG, "move right");
				// }
				// lastTimeExceedPositiveThresholdY = 0;
				// lastTimeExceedNegativeThresholdY = 0;
				// }

				// Log.d(TAG, System.currentTimeMillis() + ":" + linearAccX +
				// "," + linearAccY + "," + linearAccZ);
			}
		}
	}

	protected Runnable mEyeLocationUpdater = new Runnable() {
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
					// float rotateDiff = mDirection - mDirectionNew;
					mDirection = mDirectionNew;
					updateOpenGLEyeLocation(mDirection);
				}

				mHandler.postDelayed(mEyeLocationUpdater, 20);
			}
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
		// mGLSurfaceView.mRenderer.rotateDegree =
		// mGLSurfaceView.mRenderer.rotateDegree + rotateDiff;
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

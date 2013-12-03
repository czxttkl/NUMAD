package edu.neu.mhealth.debug.helper;

import edu.neu.mhealth.debug.CameraActivity;
import edu.neu.mhealth.debug.MainActivity;
import edu.neu.mhealth.debug.MyGLSurfaceView;
import android.os.AsyncTask;

public class InitRenderTask extends AsyncTask<Object, Integer, Boolean>{
	MainActivity mMainActivity;
	
	public InitRenderTask(MainActivity mCameraActivity) {
		this.mMainActivity = mCameraActivity;
	}

	@Override
	protected Boolean doInBackground(Object... params) {
//		mCameraActivity.mGLSurfaceView = new MyGLSurfaceView(mCameraActivity);
		mMainActivity.mGLSurfaceView.setMyRenderer();
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		mMainActivity.restoreOrCreateGLSurfaceView2();
	}
}

package edu.neu.mhealth.debug.helper;

import edu.neu.mhealth.debug.CameraActivity;
import edu.neu.mhealth.debug.MyGLSurfaceView;
import android.os.AsyncTask;

public class InitRenderTask extends AsyncTask<Object, Integer, Boolean>{
	CameraActivity mCameraActivity;
	
	public InitRenderTask(CameraActivity mCameraActivity) {
		this.mCameraActivity = mCameraActivity;
	}

	@Override
	protected Boolean doInBackground(Object... params) {
//		mCameraActivity.mGLSurfaceView = new MyGLSurfaceView(mCameraActivity);
		mCameraActivity.mGLSurfaceView.setMyRenderer();
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		mCameraActivity.restoreOrCreateGLSurfaceView2();
	}
}

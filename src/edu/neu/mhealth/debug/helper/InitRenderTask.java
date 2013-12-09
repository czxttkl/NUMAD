package edu.neu.mhealth.debug.helper;

import java.io.File;
import java.io.IOException;

import edu.neu.mhealth.debug.CameraActivity;
import edu.neu.mhealth.debug.MainActivity;
import edu.neu.mhealth.debug.MyGLSurfaceView;
import edu.neu.mhealth.debug.file.FileHelper;
import android.os.AsyncTask;
import android.os.Environment;

public class InitRenderTask extends AsyncTask<Object, Integer, Boolean>{
	MainActivity mMainActivity;
	
	public InitRenderTask(MainActivity mCameraActivity) {
		this.mMainActivity = mCameraActivity;
	}

	@Override
	protected Boolean doInBackground(Object... params) {
//		mCameraActivity.mGLSurfaceView = new MyGLSurfaceView(mCameraActivity);
		if (!Prefs.getObjFileSaved(mMainActivity)) {
			File sdCard= Environment.getExternalStorageDirectory();
			File dir = new File(sdCard.getAbsolutePath() + "/debug/");
			dir.mkdirs();
			
			try {
				FileHelper.moveAssetsToSdCard(mMainActivity.getAssets().open("memorymap_vertices"), dir.getAbsolutePath() + "/memorymap_vertices");
				FileHelper.moveAssetsToSdCard(mMainActivity.getAssets().open("memorymap_texture"), dir.getAbsolutePath() + "/memorymap_texture");
				FileHelper.moveAssetsToSdCard(mMainActivity.getAssets().open("memorymap_normal"), dir.getAbsolutePath() + "/memorymap_normal");
				
				Prefs.setObjFileSaved(mMainActivity, true);
			} catch (IOException e) {
				
			}
		}
		
		mMainActivity.mGLSurfaceView.setMyRenderer();
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		mMainActivity.restoreOrCreateGLSurfaceView2();
	}
}

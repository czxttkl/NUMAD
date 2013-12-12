package edu.neu.mhealth.debug.opengl;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Observable;
import java.util.Observer;

import android.R.integer;
import android.util.Log;
import edu.neu.mhealth.debug.MainActivity;
import edu.neu.mhealth.debug.helper.Global;
import edu.neu.mhealth.debug.helper.ModeManager;
import edu.neu.mhealth.debug.helper.Prefs;
import edu.neu.mhealth.debug.sensor.LinearAccEventListener;
import edu.neu.mhealth.debug.sensor.MotionEventListener;
import edu.neu.mhealth.debug.sensor.MotionMetrics;

public class OpenGLBugManager implements Observer {

	private MainActivity mCameraActivityInstance;

	/** Hold all the bugs that should be counted and calculated in an arraylist */
	public List<OpenGLBug> mBugList = new ArrayList<OpenGLBug>();

	public static OpenGLBugManager mOpenGLBugManager;

	public static OpenGLBugManager getOpenGLBugManager() {
		if (mOpenGLBugManager == null) {
			mOpenGLBugManager = new OpenGLBugManager();
		}
		return mOpenGLBugManager;
	}

	public static void finish() {
		mOpenGLBugManager = null;
	}
	
	public void setCameraActivityInstance(MainActivity mCameraActivity) {
		mCameraActivityInstance = mCameraActivity;
	}

	public void setMode(int mode) {
		switch (mode) {
		case ModeManager.MODE_MAIN_MENU:
			clearList();
			generateMainMenuBug();
			break;

		case ModeManager.MODE_TUTORIAL_1:
			clearList();
			mBugList.add(generateFireBug());
			break;

		case ModeManager.MODE_BEFORE_TUTORIAL_1:
			clearList();
			break;

		case ModeManager.MODE_TUTORIAL_2:
			clearList();
			mBugList.add(generateFireBug());
			mBugList.add(generateFireBug());
			break;
			
		case ModeManager.MODE_REAL_GAME:
			mBugList.add(generateFireBug());
			mBugList.add(generateFireBug());
			break;
			
		default:
			clearList();
		}
	}

	/** Generate one main menu bug if necessary */
	public void generateMainMenuBug() {
		if (mBugList.size() != 1) {
			mBugList.clear();
			int randomHeight = Global.rd.nextInt(mCameraActivityInstance.screenOpenGLHeight / 3);
			OpenGLMainMenuBug menuBug = new OpenGLMainMenuBug(mCameraActivityInstance.screenOpenGLWidth - OpenGLBug.radius, mCameraActivityInstance.screenOpenGLHeight / 4 + randomHeight, -1, 1,
					OpenGLRenderer.SCALE_RATIO);
			mBugList.add(menuBug);
		}
	}

	public void clearList() {
		mBugList.clear();
	}

	/** Return the bug's next destination (in opengl coordinate) */
	public int[] findBugNextDest() {
		double[] resultArray = mCameraActivityInstance.findBugNextDest();
		int[] destination = new int[2];
		destination[0] = (int) (OpenGLRenderer.screenOpenGLWidth * resultArray[0]);
		destination[1] = (int) (OpenGLRenderer.screenOpenGLHeight * resultArray[1]);
		return destination;
	}

	/** Generate one fire bug */
	public OpenGLFireBug generateFireBug() {
		// bugs will show up from left or right flank side. So the width is
		// either 0+radius OR screenwidht-radius
		int randomWidth;
		if (Global.rd.nextInt(2) == 0) {
			randomWidth = OpenGLBug.radius;
		} else {
			randomWidth = OpenGLRenderer.screenOpenGLWidth - OpenGLBug.radius;
		}
		int randomHeight = Global.randInt(OpenGLBug.radius, OpenGLRenderer.screenOpenGLHeight / 2);

		int[] destination = findBugNextDest();
		int[] speed = calculateSpeedTowardsDest(destination[0], destination[1], randomWidth, randomHeight);

		OpenGLFireBug tutorial1Bug = new OpenGLFireBug(randomWidth, randomHeight, speed[0], speed[1], OpenGLRenderer.SCALE_RATIO, true, destination[0], destination[1], 0);

		return tutorial1Bug;
	}

	/**
	 * Determine the speed towards the target destination. It makes sure that abs(speedX) and abs(speedY) is at least 1
	 */
	public static int[] calculateSpeedTowardsDest(int destX, int destY, int x, int y) {
		int speedX;
		int speedY;

		int xDiff = destX - x;
		int yDiff = destY - y;
		if (xDiff == 0) {
			speedX = 0;
		} else {
			speedX = Math.abs(xDiff) > OpenGLBug.BOUNCING_STEP ? xDiff / OpenGLBug.BOUNCING_STEP : xDiff / Math.abs(xDiff);
		}

		if (yDiff == 0) {
			speedY = 0;
		} else {
			speedY = Math.abs(yDiff) > OpenGLBug.BOUNCING_STEP ? yDiff / OpenGLBug.BOUNCING_STEP : yDiff / Math.abs(yDiff);
		}
		int[] speed = { speedX, speedY };

		speed = limitBoostSpeed(speed);

		return speed;
	}

	/**
	 * Check if the bug is now out of the rectangle(-screenOpenGLWidth, 2*screenOpenGLHeight, 3*screenOpenGLWidth, 3*screenOpenGLHeight)
	 */
	protected static boolean isBugOutOfBoundary(int x, int y) {
		if (x > OpenGLRenderer.screenOpenGLWidth * 2 || x < -OpenGLRenderer.screenOpenGLWidth || y > 2 * OpenGLRenderer.screenOpenGLHeight || y < -OpenGLRenderer.screenOpenGLHeight)
			return true;
		else
			return false;
	}

	/**
	 * Check if the bug is out of the rectangle(0, screenOpenGLHeight, screenOpenGLWidth, screenOpenGLHeight)
	 */
	public boolean isBugOutOfScreen(int x, int y) {
		if (x > getOpenGlWidth() || x < 0 || y > getOpenGlHeight() || y < 0)
			return true;
		else
			return false;
	}

	/**
	 * Determine whether the bug is burned by return the max distance between the bug and two fire flames
	 */
	public boolean ifFireHitsBug(int tmpX, int tmpY) {
		for (OpenGLFire mOpenGLFire : mCameraActivityInstance.mOpenGLRenderer.mFireList) {
			long xDiff = tmpX - (int) (mOpenGLFire.ratioX * getOpenGLBugManager().getOpenGlWidth());
			long yDiff = tmpY - (int) (mOpenGLFire.ratioY * getOpenGLBugManager().getOpenGlHeight());
			double distance = Math.sqrt(xDiff * xDiff + yDiff * yDiff);
			if (distance < 3 * OpenGLBug.radius) {
				return true;
			}
		}
		return false;
	}

	public boolean isPointInFloor(int openGlX, int openGlY) {
		int openCvX = (int) (openGlX * mCameraActivityInstance.openCVGLRatioX);
		int openCvY = mCameraActivityInstance.imageOpenCvHeight - (int) (openGlY * mCameraActivityInstance.openCVGLRatioY);
		int result = mCameraActivityInstance.isPointInFloor(openCvX, openCvY);
		if (result == 1)
			return true;
		else
			return false;
	}

	public int distToContour(int openGlX, int openGlY) {
		int openCvX = (int) (openGlX * mCameraActivityInstance.openCVGLRatioX);
		int openCvY = mCameraActivityInstance.imageOpenCvHeight - (int) (openGlY * mCameraActivityInstance.openCVGLRatioY);
		int result = mCameraActivityInstance.isPointInFloor(openCvX, openCvY, true);
		return result;
	}

	public ListIterator<OpenGLBug> getListIterator() {
		return mBugList.listIterator();
	}

	public int getBugListSize() {
		return mBugList.size();
	}

	public void addScore(int score) {
		mCameraActivityInstance.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mCameraActivityInstance.addScore(1);
			}
		});
	}

	public static int[] limitBoostSpeed(int[] speed) {
		int gameMode = ModeManager.getModeManager().getCurrentMode();
		switch (gameMode) {
		case ModeManager.MODE_TUTORIAL_1:
			if (Math.abs(speed[0]) > 8 || Math.abs(speed[1]) > 8) {
				speed[0] = speed[0] / 2;
				speed[1] = speed[1] / 2;
			}
			break;

		case ModeManager.MODE_TUTORIAL_2:
			if (Math.abs(speed[0] * speed[1]) <= 4) {
				speed[0] = speed[0] * 2;
				speed[1] = speed[1] * 2;
			}

			if (Math.abs(speed[0]) > 10 || Math.abs(speed[1]) > 10) {
				speed[0] = speed[0] / 2;
				speed[1] = speed[1] / 2;
			}
		default:
			break;
		}

		return speed;
	}

	public void freezeBug() {
		if (mBugList.size() == 0)
			return;
		int randomNum = Global.rd.nextInt(mBugList.size());
		OpenGLBug mOpenGLBug = mBugList.get(randomNum);
		mOpenGLBug.freezing = true;
	}

	public void unfreezeBugs() {
		for (OpenGLBug mOpenGLBug : mBugList) {
			if (mOpenGLBug.freezing) {
				mOpenGLBug.freezing = false;
			}
		}
	}
	
	public void killFrozenBugs() {
		for (OpenGLBug mOpenGLBug : mBugList) {
			if (mOpenGLBug.freezing) {
				if (!Prefs.getTutorialed(mCameraActivityInstance)) {
					mCameraActivityInstance.mHandler.postDelayed(mCameraActivityInstance.mStartRealGame, 0);
				}
				
				mOpenGLBug.freezing = false;
				mOpenGLBug.burning = true;
				mOpenGLBug.burningStepCounter = 0;
				mOpenGLBug.shouldPause = true;
				OpenGLBugManager.getOpenGLBugManager().addScore(1);
			}
		}
	}

	public void setOpenGLBugRelativeSpeed(float speedX, float speedY) {
		int openGLSpeedX = (int) (speedX/100);
		int openGLSpeedY = (int) (speedY/100);
		if (Math.abs(openGLSpeedX) > 4) {
			openGLSpeedX = openGLSpeedX/Math.abs(openGLSpeedX)*4;
		}
		if (Math.abs(openGLSpeedY) > 4) {
			openGLSpeedY = openGLSpeedY/Math.abs(openGLSpeedY)*4;
		}
		OpenGLBug.relativeSpeedX = openGLSpeedX;
		OpenGLBug.relativeSpeedY = -openGLSpeedY;
		
		Log.e(Global.APP_LOG_TAG, "speedX,speedY: " + speedX + "," + speedY + "    openglspeedx,speedY:" + openGLSpeedX + "," + openGLSpeedY);
	}
	
	public int getOpenCvWidth() {
		return mCameraActivityInstance.imageOpenCvWidth;
	}

	public int getOpenCvHeight() {
		return mCameraActivityInstance.imageOpenCvHeight;
	}

	public int getOpenGlWidth() {
		return mCameraActivityInstance.screenOpenGLWidth;
	}

	public int getOpenGlHeight() {
		return mCameraActivityInstance.screenOpenGLHeight;
	}

	@Override
	public void update(Observable observable, Object data) {
		// / if game mode changed
		if (observable instanceof ModeManager.ModeEventListener) {
			Integer gameMode = (Integer) data;
			setMode(gameMode);
		}

		if (observable instanceof LinearAccEventListener) {
			killFrozenBugs();
		}
		
		if (observable instanceof MotionEventListener) {
			MotionMetrics motion = (MotionMetrics)data;
		
			setOpenGLBugRelativeSpeed(motion.getMotionX(), motion.getMotionY());
		}

	}

}

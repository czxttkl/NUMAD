package edu.neu.mhealth.debug.opengl;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import android.util.Log;
import edu.neu.mhealth.debug.CameraActivity;
import edu.neu.mhealth.debug.OpenGLRenderer;
import edu.neu.mhealth.debug.helper.Global;

public class OpenGLBugManager {

	public final static int MODE_MAIN_MENU = 1;
	public final static int MODE_TUTORIAL_1 = 2;
	public final static int MODE_DEFAULT = 0;
	
	private static CameraActivity mCameraActivityInstance;

	/** Hold all the bugs that should be counted and calculated in an arraylist */
	public static List<OpenGLBug> mBugList = new ArrayList<OpenGLBug>();

	public static void setCameraActivityInstance(CameraActivity mCameraActivity) {
		mCameraActivityInstance = mCameraActivity;
	}

	public static void setMode(int mode) {
		switch(mode) {
		case MODE_MAIN_MENU:
			OpenGLBugManager.generateMainMenuBug();
			break;
		case MODE_TUTORIAL_1:
			 mBugList.clear();
             OpenGLBug mTutorial1Bug = generateTutorial1Bug();
             mBugList.add(mTutorial1Bug);
			break;
		default:
			OpenGLBugManager.clearList();
		}
	}
	
	/** Generate one main menu bug if necessary */
	public static void generateMainMenuBug() {
		if (mBugList.size() != 1) {
			mBugList.clear();
			int randomHeight = Global.rd.nextInt(mCameraActivityInstance.screenPixelHeight / 3);
			OpenGLMainMenuBug menuBug = new OpenGLMainMenuBug(mCameraActivityInstance.screenPixelWidth - OpenGLBug.radius, mCameraActivityInstance.screenPixelHeight / 4 + randomHeight, -1, 1,
					OpenGLRenderer.SCALE_RATIO);
			mBugList.add(menuBug);
		}
	}

	public static void clearList() {
		mBugList.clear();
	}
	
	/** Return the bug's next destination (in opengl coordinate) */
	public static int[] findBugNextDest() {
		double[] resultArray = mCameraActivityInstance.findBugNextDest();
		int[] destination = new int[2];
		destination[0] = (int) (OpenGLRenderer.screenOpenGLWidth * resultArray[0]);
		destination[1] = (int) (OpenGLRenderer.screenOpenGLHeight * resultArray[1]);
		return destination;
	}

	/** Generate one tutorial 1 bug */
	public static OpenGLFireBug generateTutorial1Bug() {
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
	 * Determine the speed towards the target destination. It makes sure that
	 * abs(speedX) and abs(speedY) is at least 1
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
		
		speed = limitSpeed(speed);
		
		return speed;
	}

	/**
	 * Check if the bug is now out of the rectangle(-screenOpenGLWidth,
	 * 2*screenOpenGLHeight, 3*screenOpenGLWidth, 3*screenOpenGLHeight)
	 */
	protected static boolean isBugOutOfBoundary(int x, int y) {
		if (x > OpenGLRenderer.screenOpenGLWidth * 2 || x < -OpenGLRenderer.screenOpenGLWidth || y > 2 * OpenGLRenderer.screenOpenGLHeight || y < -OpenGLRenderer.screenOpenGLHeight)
			return true;
		else
			return false;
	}

	/**
	 * Check if the bug is out of the rectangle(0, screenOpenGLHeight,
	 * screenOpenGLWidth, screenOpenGLHeight)
	 */
	public static boolean isBugOutOfScreen(int x, int y) {
		if (x > OpenGLRenderer.screenOpenGLWidth || x < 0 || y > OpenGLRenderer.screenOpenGLHeight || y < 0)
			return true;
		else
			return false;
	}

	/**
	 * Determine whether the bug is burned by return the max distance between
	 * the bug and two fire flames
	 */
	public static boolean ifFireHitsBug(int tmpX, int tmpY) {
		for (OpenGLFire mOpenGLFire : OpenGLRenderer.mFireList) {
			long xDiff = tmpX - (int) (mOpenGLFire.ratioX * OpenGLRenderer.screenOpenGLWidth);
			long yDiff = tmpY - (int) (mOpenGLFire.ratioY * OpenGLRenderer.screenOpenGLHeight);
			double distance = Math.sqrt(xDiff * xDiff + yDiff * yDiff);
			if (distance < 3 * OpenGLBug.radius) {
				return true;
			}
		}
		return false;
	}

	public static boolean isPointInFloor(int openGlX, int openGlY) {
		int openCvX = (int) (openGlX * mCameraActivityInstance.openCVGLRatioX);
		int openCvY = mCameraActivityInstance.screenOpenCvHeight - (int) (openGlY * mCameraActivityInstance.openCVGLRatioY);
		int result = mCameraActivityInstance.isPointInFloor(openCvX, openCvY);
		if (result == 1)
			return true;
		else
			return false;
	}
	
	public static int distToContour(int openGlX, int openGlY) {
		int openCvX = (int) (openGlX * mCameraActivityInstance.openCVGLRatioX);
		int openCvY = mCameraActivityInstance.screenOpenCvHeight - (int) (openGlY * mCameraActivityInstance.openCVGLRatioY);
		int result = mCameraActivityInstance.isPointInFloor(openCvX, openCvY, true);
		return result;
	}
	
	public static ListIterator<OpenGLBug> getListIterator() {
		return mBugList.listIterator();
	}

	public static int getBugListSize() {
		return mBugList.size();
	}

	public static void updateScore(int score) {
		mCameraActivityInstance.updateScore(1);
	}
	
	public static int[] limitSpeed(int[] speed) {
		if (speed[0] > 10 || speed[1] > 10) { 
			speed[0] = speed[0] / 2;
			speed[1] = speed[1] / 2;
		}
		return speed;
	}
}

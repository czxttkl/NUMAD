package edu.neu.mhealth.debug.opengl;

import java.util.ListIterator;

import edu.neu.mhealth.debug.helper.Global;

import android.util.Log;

public class OpenGLFireBug extends OpenGLBug {

	public OpenGLFireBug(int x, int y, int speedX, int speedY, float scaleRatio) {
		super(x, y, speedX, speedY, scaleRatio);
	}

	public OpenGLFireBug(int x, int y, int speedX, int speedY, float scaleRatio, boolean bouncing, int bounceDestX, int bounceDestY, int bounceStepCounter) {
		super(x, y, speedX, speedY, scaleRatio, bouncing, bounceDestX, bounceDestY, bounceStepCounter);
	}

	@Override
	public void refresh(ListIterator<OpenGLBug> mOpenGLBugIterator) {
		int polarityX;
		int polarityY;
		int tmpX;
		int tmpY;
		polarityX = speedX >= 0 ? 1 : -1;
		polarityY = speedY >= 0 ? 1 : -1;
		tmpX = x + speedX + OpenGLBug.relativeSpeedX;
		tmpY = y + speedY + OpenGLBug.relativeSpeedY;

		// If the bug runs out of the boundary. we remove it
		if (OpenGLBugManager.isBugOutOfBoundary(tmpX, tmpY)) {
			// Mark the bug shouldBeRemoved
			mOpenGLBugIterator.remove();
			return;
		}

		// If the bug is burning to the end, remove it and also add a new bug
		if (burning) {
			burningStepCounter++;
			if (burningStepCounter == OpenGLBug.BURNING_STEP) {
				mOpenGLBugIterator.remove();
				if (OpenGLBugManager.getBugListSize() < 3) {
					OpenGLBug mTutorial1Bug = OpenGLBugManager.generateTutorial1Bug();
					mOpenGLBugIterator.add(mTutorial1Bug);
				}
				return;
			}
		}

		// If the bug runs out of the screen, we generate a new one
		if (OpenGLBugManager.isBugOutOfScreen(x, y)) {
			if (OpenGLBugManager.getBugListSize() < 3) {
				OpenGLFireBug mTutorial1Bug = OpenGLBugManager.generateTutorial1Bug();
				mOpenGLBugIterator.add(mTutorial1Bug);
			}
		} else {
			// If the bug is still in the screen AND it is not burning and bouncing
			if (!burning) {
				if (OpenGLBugManager.ifFireHitsBug(tmpX, tmpY)) {
					burning = true;
					shouldPause = true;
					OpenGLBugManager.updateScore(1);
				} else {
					// If the bug is not burned by the fire and not bouncing, we check if it is in the floor contour
					if (!bouncing) {
						int distanceToContour = OpenGLBugManager.distToContour(x, y);
						Log.d(Global.APP_LOG_TAG, "radius:" + OpenGLBug.radius + " thresHeight:" + thresHeight + "now y:" + y + " distanceToContour:" + distanceToContour);
						
						// If the bug is not in the floor, it should bounce
						if (distanceToContour < 0) {
							bouncing = true;
							bounceStepCounter = 0;
							int[] destination = OpenGLBugManager.findBugNextDest();
							int[] speed = OpenGLBugManager.calculateSpeedTowardsDest(destination[0], destination[1], x, y);
							speedX = speed[0];
							speedY = speed[1];
							tmpX = x + speedX + OpenGLBug.relativeSpeedX;
							tmpY = y + speedY + OpenGLBug.relativeSpeedY;
						}
						
						// If the bug is in the floor, and it could get out of screen
						if (distanceToContour < OpenGLBug.radius && distanceToContour >= 0 && y >= thresHeight) {
							// We simply keep tmpX tmpY
						}
						
						// If the bug is in the floor, but it needs to return
						if (distanceToContour < OpenGLBug.radius && distanceToContour >=0 && y < thresHeight) {
							int[] destination = OpenGLBugManager.findBugNextDest();
							int[] speed = OpenGLBugManager.calculateSpeedTowardsDest(destination[0], destination[1], x, y);
							speedX = speed[0];
							speedY = speed[1];
							tmpX = x + speedX + OpenGLBug.relativeSpeedX;
							tmpY = y + speedY + OpenGLBug.relativeSpeedY;
						}

						// If the bug is approaching to the edges.
						if (distanceToContour < 2 * OpenGLBug.radius && distanceToContour >= OpenGLBug.radius) {
							speedX = speedX / 2 + polarityX;
							speedY = speedY / 2 + polarityY;
							tmpX = x + speedX + OpenGLBug.relativeSpeedX;
							tmpY = y + speedY + OpenGLBug.relativeSpeedY;
						}

					}
				}
			} // if it is not burning
		}

		if (bouncing) {
			bounceStepCounter++;
			// If bounce step counter hits OpenGLBug.BOUNCING_STEP, we
			// clear the bouncing information.
			// We wouldn't update its (x,y) info. However we would keep
			// its (speedX, speedY)info because that matters with head
			// rotation
			if (bounceStepCounter == OpenGLBug.BOUNCING_STEP) {
				bouncing = false;
				bounceStepCounter = 0;
				shouldPause = true;
			}
		}

		// Halt handle
		// We need to update the bug if it is in the valid region and shouldPause==false.
		if (!shouldPause) {
			x = tmpX;
			y = tmpY;
			lastRefreshTime = System.currentTimeMillis();
			if (!bouncing && Global.rd.nextInt(100) > 98) {
				shouldPause = true;
			}
		} else {
			// Make the bug move if it halts for a while
			if (!burning && System.currentTimeMillis() - lastRefreshTime > Global.rd.nextInt(3000)) {
				shouldPause = false;
			}
		}

	}
}

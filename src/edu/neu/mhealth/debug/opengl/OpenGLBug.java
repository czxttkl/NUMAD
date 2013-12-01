package edu.neu.mhealth.debug.opengl;

public class OpenGLBug {
	/** The radius of the square occupied by the bug (in pixels) */
	public static int radius = 50; 
	
	/** The x coordinate of the bug's position in the screen*/
	public int x = 0;
	
	/** The y coordinate of the bug's position in the screen*/
	public int y = 0;
	
	/** Horizontal speed of the bug*/
	public int speedX = 0;
	
	/** Vertical speed of the bug*/
	public int speedY = 0;
	
	/** Indicate whether this bug should halt for the reality simulation reason. */
	public boolean shouldPause;
	
	/** Horizontal relative speed of the bug with users' movement */
	public static int relativeSpeedX = 0;
	
	/** Vertical relative speed of the bug with users' movement */
	public static int relativeSpeedY = 0;
	
	/** Bouncing steps. Used for split the speed and other bouncing animations */
	public final static int BOUNCING_STEP = 50;
	
	/** Last time we change the bug */
	public long lastRefreshTime = 0;
	
	/** Indicate whether this bug is bouncing now */
	public boolean bouncing = false;
	
	/** the destination's x value that the bug is bouncing to */
	public int bounceDestX;
	
	/** the destination's y value that the bug is bouncing to */
	public int bounceDestY;
	
	/** The bouncing steps counter. Used for split bouncing animations */
	public int bounceStepCounter;
	
	/** Indicate whether this bug should be removed. (e.g. it runs out of the boundary) */
	public boolean shouldBeRemoved = false;
	
	public OpenGLBug(int x, int y, int speedX, int speedY) {
		this.x = x;
		this.y = y;
		this.speedX = speedX;
		this.speedY = speedY;
	}
	
	public OpenGLBug(int x, int y, int speedX, int speedY, boolean bouncing, int bounceDestX, int bounceDestY, int bounceStepCounter) {
		this.x = x;
		this.y = y;
		this.speedX = speedX;
		this.speedY = speedY;
		this.bouncing = bouncing;
		this.bounceDestX = bounceDestX;
		this.bounceDestY = bounceDestY;
		this.bounceStepCounter = bounceStepCounter;
	}
}

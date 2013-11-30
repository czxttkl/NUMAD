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
	
	/** Last time we change the bug */
	public long lastRefreshTime = 0;
	
	public OpenGLBug(int x, int y, int speedX, int speedY) {
		this.x = x;
		this.y = y;
		this.speedX = speedX;
		this.speedY = speedY;
	}
}

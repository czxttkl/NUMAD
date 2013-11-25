package edu.neu.mhealth.debug;

public class OpenGLBug {
	/** the radius of the square occupied by the bug (in pixels) */
	public static int radius = 50; 
	/** the x coordinate of the bug's position in the screen*/
	public int x = 0;
	/** the y coordinate of the bug's position in the screen*/
	public int y = 0;
	/**  horizontal speed of the bug*/
	public int speedX = 0;
	/** vertical speed of the bug*/
	public int speedY = 0;
	
	public OpenGLBug(int x, int y, int speedX, int speedY) {
		this.x = x;
		this.y = y;
		this.speedX = speedX;
		this.speedY = speedY;
	}
}

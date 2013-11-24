package edu.neu.mhealth.debug;

public class OpenGLBug {
	/** square radius in pixels */
	public static int radius = 50; 
	/** the x coordinate*/
	public int x = 0;
	/** the y coordinate*/
	public int y = 0;
	/**  horizontal speed*/
	public int speedX = 0;
	/** vertical speed*/
	public int speedY = 0;
	
	public OpenGLBug(int x, int y, int speedX, int speedY) {
		this.x = x;
		this.y = y;
		this.speedX = speedX;
		this.speedY = speedY;
	}
}

package edu.neu.mhealth.debug.opengl;

public class OpenGLFire {
	/**
	 * Because the units of width are different between OpenCV and OpenGL, we
	 * record ratio for the position according to the screen
	 */
	public float ratioX;

	/**
	 * Because the units of height are different between OpenCV and OpenGL, we
	 * record ratio for the position according to the screen
	 */
	public float ratioY;

	public OpenGLFire(float ratioX, float ratioY) {
		this.ratioX = ratioX;
		this.ratioY = ratioY;
	}
}

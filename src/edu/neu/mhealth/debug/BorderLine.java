package edu.neu.mhealth.debug;

public class BorderLine {
	public static final int TYPE_UPPER_BOUND = 1;
	public static final int TYPE_LOWER_BOUND = 2;
	public static final int TYPE_X_LEFT_BOUND = 3;
	public static final int TYPE_X_RIGHT_BOUND = 4;
	public float a;
	public float b;
	public int y_factor;
	public int type;
	
	/**
	 * Store border lines
	 * y = ax + b
	 * y_factor = 1 when type == TYPE_UPPER_BOUND || TYPE_LOWER_BOUND
	 * y_factor = 0 when type == TYPE_X_LEFT_BOUND || TYPE_X_RIGHT_BOUND
	 * 
	 * if type == TYPE_X_LEFT_BOUND || TYPE_X_RIGHT_BOUND, we store the x-intersection as variable b. 
	 * At this point, y_factor = 0, a = 1;
	 */
	public BorderLine(int type, float a, float b, int y_factor) {
		this.type = type;
		this.a = a;
		this.b = b;
		this.y_factor = y_factor;
	}
	
}

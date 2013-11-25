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
	
	public BorderLine(int type, float a, float b, int y_factor) {
		this.type = type;
		this.a = a;
		this.b = b;
		this.y_factor = y_factor;
	}
	
}

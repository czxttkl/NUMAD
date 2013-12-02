package edu.neu.mhealth.debug.helper;

public class ModeManager {

	public static final int MODE_COLOR_PICK_CROSSHAIR = 123;
	public static final int MODE_SHOE_COLOR_PICKED = 124;
	public static final int MODE_COLOR_PICK_HOLD_WRONGLY = 125;
	public static final int MODE_FLOOR_COLOR_PICKED = 126;
	public static final int MODE_TUTORIAL_1 = 127;
	
	private static ModeManager modeManager;
	private int currentMode;
	private int previousMode;
	
	private ModeManager() {
		currentMode = 0;
		previousMode = 0;
	}
	
	public static ModeManager getModeManager() {
		
		if (modeManager == null) {
			modeManager = new ModeManager();
		}
		
		return modeManager;
	}

	public int getCurrentMode() {
		return currentMode;
	}

	public void setCurrentMode(int currentMode) {
		this.currentMode = currentMode;
	}

	public int getPreviousMode() {
		return previousMode;
	}

	public void setPreviousMode(int previousMode) {
		this.previousMode = previousMode;
	}
	

}

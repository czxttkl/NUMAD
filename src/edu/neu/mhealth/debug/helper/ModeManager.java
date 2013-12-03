package edu.neu.mhealth.debug.helper;

import java.util.Observable;
import java.util.Observer;

import android.util.Log;

public class ModeManager {

	private static final String TAG = "ModeManager";
	public static final int MODE_INITIAL = 0;
	public static final int MODE_COLOR_PICK_CROSSHAIR = 123;
	public static final int MODE_SHOE_COLOR_PICKED = 124;
	public static final int MODE_COLOR_PICK_HOLD_WRONGLY = 125;
	public static final int MODE_FLOOR_COLOR_PICKED = 126;
	public static final int MODE_TUTORIAL_1 = 127;
	
	private static ModeManager modeManager;
	private ModeEventListener eventListener;
	private int currentMode;
	private int previousMode;
	
	private ModeManager() {
		currentMode = -1;
		previousMode = -1;
		eventListener = new ModeEventListener();
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
		Log.e(TAG, "mode manager set mode");
		this.eventListener.notifyModeUpdate(currentMode);
	}

	public int getPreviousMode() {
		return previousMode;
	}

	public void setPreviousMode(int previousMode) {
		this.previousMode = previousMode;
	}
	
	public void addObserver(Observer ob) {
		eventListener.addObserver(ob);
	}
	
	public class ModeEventListener extends Observable {
		
		public void notifyModeUpdate(int currentMode) {
			setChanged();
			notifyObservers(currentMode);
		}
	}

}

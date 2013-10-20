/***
 * Excerpted from "Hello, Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband3 for more book information.
 ***/
package edu.neu.zhengxingchen.madcourse.dabble;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;

public class Prefs extends PreferenceActivity {

	// Option names and default values
	private static final String OPT_MUSIC = "music";
	private static final boolean OPT_MUSIC_DEF = true;
	private static final String OPT_HINTS = "hints";
	private static final boolean OPT_HINTS_DEF = true;
	private static final String OPT_HIGHSCORE = "highscore";
	private static final int OPT_HIGHSCORE_DEF = 0;
	private static final String OPT_DABBLESTRING = "dabblestring";
	private static final String OPT_DABBLESTRING_DEF = "";
	private static final String OPT_DABBLEARRAY = "dabblearray";
	private static final String OPT_DABBLEARRAY_DEF = "";
	private static final String OPT_GAMESAVE = "gamesave";
	private static final boolean OPT_GAMESAVE_DEF = false;
	private static final String OPT_STARTTIME = "starttime";
	private static final long OPT_STARTTIME_DEF = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Music.start(this);
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		// SharedPreferences mSharedPreferences = PreferenceManager
		// .getDefaultSharedPreferences(getCallingActivity().);
		// mSharedPreferences.registerOnSharedPreferenceChangeListener(mListener);
	}

	/** Get the current value of the music option */

	public static boolean getMusic(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(OPT_MUSIC, OPT_MUSIC_DEF);
	}

	/** Get the current value of the hints option */

	public static boolean getHints(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(OPT_HINTS, OPT_HINTS_DEF);
	}

	public static int getHighScore(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getInt(
				OPT_HIGHSCORE, OPT_HIGHSCORE_DEF);
	}
	

	public static void setHighScore(Context context, int highscore) {
		if (getHighScore(context) < highscore)
			PreferenceManager.getDefaultSharedPreferences(context).edit()
					.putInt(OPT_HIGHSCORE, highscore).commit();
	}

	
	public static void setSavedDabbleString(Context context, String dabbleString) {
//		Log.d("dabble", "set saved dabble string:" + dabbleString);
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(OPT_DABBLESTRING,dabbleString).commit();
	}
	
	public static void setSavedDabbleArray(Context context, String dabbleArray) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(OPT_DABBLEARRAY,dabbleArray).commit();
	
	}
	
	public static void setSavedDabbleStartTime(Context context, long starttime) {
		PreferenceManager.getDefaultSharedPreferences(context).edit()
		.putLong(OPT_STARTTIME, starttime).commit();
	}
	
	public synchronized static void setSaved(Context context, boolean saved) {
		PreferenceManager.getDefaultSharedPreferences(context).edit()
		.putBoolean(OPT_GAMESAVE, saved).commit();
	}
	
	public synchronized static boolean getSaved(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
				OPT_GAMESAVE, OPT_GAMESAVE_DEF);
	}
	
	public static String getSavedDabbleString(Context context) {
		String dabbleString = PreferenceManager.getDefaultSharedPreferences(context).getString(
				OPT_DABBLESTRING, OPT_DABBLESTRING_DEF);
		Log.d("dabble", "set saved dabble string:" + dabbleString);
		return dabbleString;
	}
	
	public static String getSavedDabbleArray(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(
				OPT_DABBLEARRAY, OPT_DABBLEARRAY_DEF);
	}
	
	public static long getSavedDabbleStartTime(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getLong(
				OPT_STARTTIME, OPT_STARTTIME_DEF);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		finish();
		return true;
	}
	
	

}

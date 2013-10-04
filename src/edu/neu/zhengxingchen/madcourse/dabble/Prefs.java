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

public class Prefs extends PreferenceActivity {
	// Option names and default values
	private static final String OPT_MUSIC = "music";
	private static final boolean OPT_MUSIC_DEF = true;
	private static final String OPT_HINTS = "hints";
	private static final boolean OPT_HINTS_DEF = true;
	private static final String OPT_HIGHSCORE = "highscore";
	private static final int OPT_HIGHSCORE_DEF = 0;

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
		return PreferenceManager.getDefaultSharedPreferences(context).getInt(OPT_HIGHSCORE, OPT_HIGHSCORE_DEF);
		
	}
	
	public static void setHighScore(Context context, int highscore) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(OPT_HIGHSCORE, highscore).commit();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		finish();
		return true;
	}




}

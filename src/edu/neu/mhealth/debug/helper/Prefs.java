package edu.neu.mhealth.debug.helper;

import android.content.Context;
import android.preference.PreferenceManager;

public class Prefs {
	public static final String KEY_SPRAY_TAPPED = "spraytapped";
	public static final String KEY_TUTORIALED = "tutorialed";
	public static final boolean KEY_SPRAY_TAPPED_DEF = false;
	public static final boolean KEY_TUTORIALED_DEF = false;

	public static boolean getSprayTapped(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_SPRAY_TAPPED, KEY_SPRAY_TAPPED_DEF);
	}

	public static void setSprayTapped(Context context, boolean result) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_SPRAY_TAPPED, result).commit();
	}

	public static boolean getTutorialed(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_TUTORIALED, KEY_TUTORIALED_DEF);
	}

	public static void setTutorialed(Context context, boolean result) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_TUTORIALED, result).commit();
	}

}

package edu.neu.mhealth.debug.helper;

import android.content.Context;
import android.preference.PreferenceManager;

public class Prefs {
	public static final String KEY_SPRAY_TAPPED = "spraytapped";
	public static final boolean KEY_SPRAY_TAPPED_DEF = false;

	
	public static boolean getSprayTapped(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(KEY_SPRAY_TAPPED, KEY_SPRAY_TAPPED_DEF);
	}

	public static void setSprayTapped(Context context, boolean result) {
		PreferenceManager.getDefaultSharedPreferences(context).edit()
		.putBoolean(KEY_SPRAY_TAPPED, result).commit();
	}
	

}

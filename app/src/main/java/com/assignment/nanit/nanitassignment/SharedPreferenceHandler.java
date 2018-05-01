package com.assignment.nanit.nanitassignment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author Thelegendery on 4/15/2018.
 */
public class SharedPreferenceHandler {

	static void saveToSharedPreferences(Context context, String key, String value) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(key, value);
		editor.apply();
	}
}

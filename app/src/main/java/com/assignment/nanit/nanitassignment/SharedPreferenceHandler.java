package com.assignment.nanit.nanitassignment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author Thelegendery on 4/15/2018.
 */
public class SharedPreferenceHandler {

	static void saveToSharedPreferences(Activity activityContext, String key, String value) {
		SharedPreferences sharedPref = activityContext.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(key, value);
		editor.apply();
	}
}

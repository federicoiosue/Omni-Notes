package it.feio.android.omninotes.utils;

import it.feio.android.checklistview.utils.DensityUtil;
import it.feio.android.omninotes.R;

import java.util.Arrays;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Fonts {
	/**
	 * Overrides all the fonts set to TextView class descendants found in the
	 * view passed as parameter
	 * 
	 * @param context
	 * @param v
	 */
	public static void overrideTextSize(Context context, SharedPreferences prefs, View v) {
		try {
			if (v instanceof ViewGroup) {
				ViewGroup vg = (ViewGroup) v;
				for (int i = 0; i < vg.getChildCount(); i++) {
					View child = vg.getChildAt(i);
					overrideTextSize(context, prefs, child);
				}
			} else if (v instanceof TextView) {
				// ((TextView) v).setTypeface(Typeface.createFromAsset(
				// context.getAssets(), "font.ttf"));
				float currentSize = DensityUtil.pxToDp(((TextView) v).getTextSize(), context);
				int index = Arrays
						.asList(context.getResources().getStringArray(
								R.array.text_size_values))
						.indexOf(
								prefs.getString("settings_text_size", "default"));
				float offset = context.getResources().getIntArray(
						R.array.text_size_offset)[index == -1 ? 0 : index];
				((TextView) v).setTextSize(currentSize + offset);
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, "Error setting font size", e);
		}
	}
}

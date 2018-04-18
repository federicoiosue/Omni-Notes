/*
 * Copyright (C) 2018 Federico Iosue (federico.iosue@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.feio.android.omninotes.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import it.feio.android.checklistview.utils.DensityUtil;
import it.feio.android.omninotes.R;

import java.util.Arrays;


public class Fonts {

    /**
     * Overrides all the fonts set to TextView class descendants found in the
     * view passed as parameter
     */
    public static void overrideTextSize(Context context, SharedPreferences prefs, View v) {
        Context privateContext = context.getApplicationContext();
        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    overrideTextSize(privateContext, prefs, child);
                }
            } else if (v instanceof TextView) {
                float currentSize = DensityUtil.pxToDp(((TextView) v).getTextSize(), privateContext);
                int index = Arrays
                        .asList(privateContext.getResources().getStringArray(
                                R.array.text_size_values))
                        .indexOf(
                                prefs.getString("settings_text_size", "default"));
                float offset = privateContext.getResources().getIntArray(
                        R.array.text_size_offset)[index == -1 ? 0 : index];
                ((TextView) v).setTextSize(currentSize + offset);
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "Error setting font size", e);
        }
    }
}

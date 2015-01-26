/*
 * Copyright (C) 2015 Federico Iosue (federico.iosue@gmail.com)
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;

import it.feio.android.checklistview.utils.DensityUtil;
import it.feio.android.omninotes.R;
import roboguice.util.Ln;


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
            Ln.e(e, "Error setting font size");
        }
    }
}

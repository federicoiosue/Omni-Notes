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

package it.feio.android.omninotes.models;

import android.view.Gravity;
import android.widget.LinearLayout.LayoutParams;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Style;
import it.feio.android.omninotes.R;


public class ONStyle {

    public static final int DURATION_INFINITE = -1;
    public static final int DURATION_SHORT = 950;
    public static final int DURATION_MEDIUM = 1650;
    public static final int DURATION_LONG = 2300;

    public static final Configuration CONFIGURATION;
    public static final Style ALERT;
    public static final Style WARN;
    public static final Style CONFIRM;
    public static final Style INFO;

    public static final int ALERT_COLOR = R.color.alert;
    public static final int WARN_COLOR = R.color.warning;
    public static final int CONFIRM_COLOR = R.color.confirm;
    public static final int INFO_COLOR = R.color.info;


    static {
        CONFIGURATION = new Configuration.Builder()
                .setDuration(DURATION_SHORT)
                .setInAnimation(R.animator.fade_in_support)
                .setOutAnimation(R.animator.fade_out_support)
                .build();
        ALERT = new Style.Builder()
                .setBackgroundColor(ALERT_COLOR)
                .setHeight(LayoutParams.MATCH_PARENT)
                .setGravity(Gravity.CENTER)
                .setTextAppearance(R.style.crouton_text)
                .setConfiguration(CONFIGURATION)
                .build();
        WARN = new Style.Builder()
                .setBackgroundColor(WARN_COLOR)
                .setHeight(LayoutParams.MATCH_PARENT)
                .setGravity(Gravity.CENTER)
                .setTextAppearance(R.style.crouton_text)
                .setConfiguration(CONFIGURATION)
                .build();
        CONFIRM = new Style.Builder()
                .setBackgroundColor(CONFIRM_COLOR)
                .setHeight(LayoutParams.MATCH_PARENT)
                .setGravity(Gravity.CENTER)
                .setTextAppearance(R.style.crouton_text)
                .setConfiguration(CONFIGURATION)
                .build();
        INFO = new Style.Builder()
                .setBackgroundColor(INFO_COLOR)
                .setHeight(LayoutParams.MATCH_PARENT)
                .setGravity(Gravity.CENTER)
                .setTextAppearance(R.style.crouton_text)
                .setConfiguration(CONFIGURATION)
                .build();
    }
}
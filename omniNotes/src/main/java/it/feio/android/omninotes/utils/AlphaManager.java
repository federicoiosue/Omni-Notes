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

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;
import android.view.animation.AlphaAnimation;


public class AlphaManager {
    
    private AlphaManager(){}

    @SuppressLint("NewApi")
    public static void setAlpha(View v, float alpha) {
        if (v != null) {
            if (Build.VERSION.SDK_INT < 11) {
                final AlphaAnimation animation = new AlphaAnimation(1F, alpha);
                animation.setFillAfter(true);
                v.startAnimation(animation);
            } else {
                v.setAlpha(alpha);
            }
        }
    }
}

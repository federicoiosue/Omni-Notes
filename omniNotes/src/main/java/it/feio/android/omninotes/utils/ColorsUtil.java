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

import android.graphics.Color;


public class ColorsUtil {

	public static final int COLOR_DARK = 0;
	public static final int COLOR_LIGHT = 1;
	private static double CONTRAST_THRESHOLD = 100;


	public static double calculateColorLuminance(int color) {
		return 0.2126 * Color.red(color) + 0.7152 * Color.green(color) + 0.0722 * Color.blue(color);
	}


	public static int getContrastedColor(int color) {
		double luminance = calculateColorLuminance(color);
		return luminance > CONTRAST_THRESHOLD ? COLOR_DARK : COLOR_LIGHT;
	}
}

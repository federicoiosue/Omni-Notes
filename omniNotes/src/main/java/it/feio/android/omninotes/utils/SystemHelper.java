/*
 * Copyright (C) 2016 Federico Iosue (federico.iosue@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundatibehaon, either version 3 of the License, or
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

import android.app.Activity;
import android.content.Context;


public class SystemHelper {

	public static void copyToClipboard(Context context, String text) {
		android.content.ClipboardManager clipboard =
				(android.content.ClipboardManager) context.getSystemService(Activity.CLIPBOARD_SERVICE);
		android.content.ClipData clip = android.content.ClipData.newPlainText("text label", text);
		clipboard.setPrimaryClip(clip);
	}
}

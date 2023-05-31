/*
 * Copyright (C) 2013-2023 Federico Iosue (federico@iosue.it)
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

import static android.content.Context.CLIPBOARD_SERVICE;

import android.app.Activity;
import android.content.Context;
import it.feio.android.omninotes.helpers.LogDelegate;
import java.io.Closeable;
import java.io.IOException;
import lombok.experimental.UtilityClass;

/**
 * Various utility methods
 */
@UtilityClass
public class SystemHelper {

  /**
   * Performs a full app restart
   */
  public static void restartApp() {
    System.exit(0);
  }


  /**
   * Performs closure of multiple closeables objects
   *
   * @param closeables Objects to close
   */
  public static void closeCloseable(Closeable... closeables) {
    for (Closeable closeable : closeables) {
      if (closeable != null) {
        try {
          closeable.close();
        } catch (IOException e) {
          LogDelegate.w("Can't close " + closeable, e);
        }
      }
    }
  }

  public static void copyToClipboard(Context context, String text) {
    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
        .getSystemService(CLIPBOARD_SERVICE);
    android.content.ClipData clip = android.content.ClipData.newPlainText("text label", text);
    clipboard.setPrimaryClip(clip);
  }

}
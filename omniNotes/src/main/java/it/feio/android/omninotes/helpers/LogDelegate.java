/*
 * Copyright (C) 2013-2020 Federico Iosue (federico@iosue.it)
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

package it.feio.android.omninotes.helpers;

import static it.feio.android.checklistview.interfaces.Constants.TAG;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_ENABLE_FILE_LOGGING;

import android.util.Log;
import com.bosphere.filelogger.FL;
import com.bosphere.filelogger.FLConfig;
import com.bosphere.filelogger.FLConst;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.exceptions.GenericException;
import it.feio.android.omninotes.utils.StorageHelper;
import java.io.File;

public class LogDelegate {

  private static Boolean fileLoggingEnabled;

  private LogDelegate () {
    // Public constructor hiding
  }

  public static void v (String message) {
    if (isFileLoggingEnabled()) {
      FL.v(message);
    } else {
      Log.v(TAG, message);
    }
  }

  public static void d (String message) {
    if (isFileLoggingEnabled()) {
      FL.d(message);
    } else {
      Log.d(TAG, message);
    }
  }

  public static void i (String message) {
    if (isFileLoggingEnabled()) {
      FL.i(message);
    } else {
      Log.i(TAG, message);
    }
  }

  public static void w (String message, Throwable e) {
    if (isFileLoggingEnabled()) {
      FL.w(message, e);
    } else {
      Log.w(TAG, message, e);
    }
  }

  public static void w (String message) {
    if (isFileLoggingEnabled()) {
      FL.w(message);
    } else {
      Log.w(TAG, message);
    }
  }

  public static void e (String message, Throwable e) {
    if (isFileLoggingEnabled()) {
      FL.e(message, e);
    } else {
      Log.e(TAG, message, e);
    }
  }

  public static void e (String message) {
    e(message, new GenericException(message));
  }

  private static boolean isFileLoggingEnabled () {
    if (fileLoggingEnabled == null) {
      fileLoggingEnabled = OmniNotes.getSharedPreferences().getBoolean(PREF_ENABLE_FILE_LOGGING, false);
      if (fileLoggingEnabled) {
        FL.init(new FLConfig.Builder(OmniNotes.getAppContext())
            .minLevel(FLConst.Level.V)
            .logToFile(true)
            .dir(new File(StorageHelper.getExternalStoragePublicDir(), "logs"))
            .retentionPolicy(FLConst.RetentionPolicy.FILE_COUNT)
            .build());
        FL.setEnabled(true);
      }
    }
    return fileLoggingEnabled;
  }
}

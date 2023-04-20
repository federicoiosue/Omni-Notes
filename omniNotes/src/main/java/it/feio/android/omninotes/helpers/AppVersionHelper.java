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

package it.feio.android.omninotes.helpers;

import static it.feio.android.omninotes.utils.ConstantsBase.PREF_CURRENT_APP_VERSION;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import com.pixplicity.easyprefs.library.Prefs;
import lombok.experimental.UtilityClass;


@UtilityClass
public class AppVersionHelper {

  public static boolean isAppUpdated(Context context) {
    return getCurrentAppVersion(context) > getAppVersionFromPreferences(context);
  }

  public static int getAppVersionFromPreferences(Context context) {
    try {
      return Prefs.getInt(PREF_CURRENT_APP_VERSION, 1);
    } catch (ClassCastException e) {
      return getCurrentAppVersion(context) - 1;
    }
  }

  public static void updateAppVersionInPreferences(Context context) {
    Prefs.edit().putInt(PREF_CURRENT_APP_VERSION, getCurrentAppVersion(context)).apply();
  }

  public static int getCurrentAppVersion(Context context) {
    try {
      return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
    } catch (NameNotFoundException e) {
      // Cannot happen, it's just this app, but, ok...
      return 1;
    }
  }

  public static String getCurrentAppVersionName(Context context) {
    try {
      return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
    } catch (NameNotFoundException e) {
      // Cannot happen, it's just this app, but, ok...
      return "1.0.0";
    }
  }

}
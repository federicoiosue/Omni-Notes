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

package it.feio.android.omninotes.helpers;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import it.feio.android.omninotes.R;
import it.feio.android.omninotes.utils.Constants;


/**
 * Class used to manage application version code and name
 */
public class AppVersionHelper {

    public static boolean isAppUpdated(Context context) throws PackageManager.NameNotFoundException {
        int currentAppVersion = getCurrentAppVersion(context);
        int savedAppVersion = getAppVersionFromPreferences(context);
        return currentAppVersion > savedAppVersion;
    }

    public static int getAppVersionFromPreferences(Context context) throws PackageManager.NameNotFoundException {
        try {
            return context.getSharedPreferences(Constants.PREFS_NAME,
                    Context.MODE_MULTI_PROCESS).getInt(Constants.PREF_CURRENT_APP_VERSION, 1);
        } catch (ClassCastException e) {
            return getCurrentAppVersion(context) - 1;
        }
    }

    public static void updateAppVersionInPreferences(Context context) throws PackageManager.NameNotFoundException {
        context.getSharedPreferences(Constants.PREFS_NAME,
                Context.MODE_MULTI_PROCESS).edit().putInt(Constants.PREF_CURRENT_APP_VERSION,
                getCurrentAppVersion(context)).apply();
    }

    public static int getCurrentAppVersion(Context context) throws PackageManager.NameNotFoundException {
        return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
    }

    public static String getCurrentAppVersionName(Context context) throws PackageManager.NameNotFoundException {
        PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        return pInfo.versionName;
    }

}

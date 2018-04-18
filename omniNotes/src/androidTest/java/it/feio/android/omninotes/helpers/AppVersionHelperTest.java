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

import android.content.pm.PackageManager;

import it.feio.android.omninotes.BaseAndroidTestCase;
import it.feio.android.omninotes.utils.Constants;


public class AppVersionHelperTest extends BaseAndroidTestCase {

    private static final String VERSION_NAME_REGEX = "\\d{1}(\\.\\d)*( Beta \\d+){0,1}";

    public void testGetCurrentAppVersion() throws PackageManager.NameNotFoundException {
        int currentAppVersion = AppVersionHelper.getCurrentAppVersion(testContext);
        assertTrue(currentAppVersion > 0);
        assertTrue(currentAppVersion < Integer.MAX_VALUE);
    }

    public void testIsAppUpdatedFalse() throws PackageManager.NameNotFoundException {
        AppVersionHelper.updateAppVersionInPreferences(testContext);
        assertFalse(AppVersionHelper.isAppUpdated(testContext));
    }

    public void testIsAppUpdatedTrue() throws PackageManager.NameNotFoundException {
        int currentAppVersion = AppVersionHelper.getCurrentAppVersion(testContext);
        prefs.edit().putInt(Constants.PREF_CURRENT_APP_VERSION, currentAppVersion - 1).commit();
        assertTrue(AppVersionHelper.isAppUpdated(testContext));
    }

    public void testGetAppVersionFromPreferences() throws PackageManager.NameNotFoundException {
        prefs.edit().clear().commit();
        assertEquals(1, AppVersionHelper.getAppVersionFromPreferences(testContext));
    }

    public void testGetCurrentAppVersionName() throws PackageManager.NameNotFoundException {
        String currentAppVersionName = AppVersionHelper.getCurrentAppVersionName(testContext);
        assertTrue(currentAppVersionName.matches(VERSION_NAME_REGEX));
    }

    public void testLegacyVersionManagementRetrieval() throws PackageManager.NameNotFoundException {
        prefs.edit().putString(Constants.PREF_CURRENT_APP_VERSION, "5.2.6 Beta 1").commit();
        int currentAppVersion = AppVersionHelper.getCurrentAppVersion(testContext);
        int saveAppVersion = AppVersionHelper.getAppVersionFromPreferences(testContext);
        assertEquals(currentAppVersion - 1, saveAppVersion);
    }

    public void testLegacyVersionManagementUpdateCheck() throws PackageManager.NameNotFoundException {
        prefs.edit().putString(Constants.PREF_CURRENT_APP_VERSION, "5.2.6 Beta 1").commit();
        assertTrue(AppVersionHelper.isAppUpdated(testContext));
    }

}

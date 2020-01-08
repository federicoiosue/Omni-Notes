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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.pm.PackageManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import it.feio.android.omninotes.BaseAndroidTestCase;
import it.feio.android.omninotes.utils.Constants;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class AppVersionHelperTest extends BaseAndroidTestCase {

  private static final String VERSION_NAME_REGEX = "\\d{1}(\\.\\d)*( Alpha| Beta)*( \\d+)*";

  @Test
  public void shouldGetCurrentAppVersion () throws PackageManager.NameNotFoundException {
    int currentAppVersion = AppVersionHelper.getCurrentAppVersion(testContext);
    assertTrue(currentAppVersion > 0);
    assertTrue(currentAppVersion < Integer.MAX_VALUE);
  }

  @Test
  public void shouldaVerifyAppUpdatedFalse () throws PackageManager.NameNotFoundException {
    AppVersionHelper.updateAppVersionInPreferences(testContext);
    assertFalse(AppVersionHelper.isAppUpdated(testContext));
  }

  @Test
  public void shouldVerifyAppUpdatedTrue () throws PackageManager.NameNotFoundException {
    int currentAppVersion = AppVersionHelper.getCurrentAppVersion(testContext);
    prefs.edit().putInt(Constants.PREF_CURRENT_APP_VERSION, currentAppVersion - 1).commit();
    assertTrue(AppVersionHelper.isAppUpdated(testContext));
  }

  @Test
  public void shouldGetAppVersionFromPreferences () throws PackageManager.NameNotFoundException {
    prefs.edit().clear().commit();
    assertEquals(1, AppVersionHelper.getAppVersionFromPreferences(testContext));
  }

  @Test
  public void shouldGetCurrentAppVersionName () throws PackageManager.NameNotFoundException {
    String currentAppVersionName = AppVersionHelper.getCurrentAppVersionName(testContext);
    assertTrue(currentAppVersionName.matches(VERSION_NAME_REGEX));
  }

  @Test
  public void shouldWorkLegacyVersionManagementRetrieval () throws PackageManager.NameNotFoundException {
    prefs.edit().putString(Constants.PREF_CURRENT_APP_VERSION, "5.2.6 Beta 1").commit();
    int currentAppVersion = AppVersionHelper.getCurrentAppVersion(testContext);
    int saveAppVersion = AppVersionHelper.getAppVersionFromPreferences(testContext);
    assertEquals(currentAppVersion - 1, saveAppVersion);
  }

  @Test
  public void shouldWorkLegacyVersionManagementUpdateCheck () throws PackageManager.NameNotFoundException {
    prefs.edit().putString(Constants.PREF_CURRENT_APP_VERSION, "5.2.6 Beta 1").commit();
    assertTrue(AppVersionHelper.isAppUpdated(testContext));
  }

}

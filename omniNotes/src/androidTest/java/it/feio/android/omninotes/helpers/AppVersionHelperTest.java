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

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import it.feio.android.omninotes.testutils.BaseAndroidTestCase;
import it.feio.android.omninotes.utils.Constants;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class AppVersionHelperTest extends BaseAndroidTestCase {

  private static final String VERSION_NAME_REGEX = "\\d{1}(\\.\\d)*( Alpha| Beta)*( \\d+)*";

  @Test
  public void checkUtilityClassWellDefined() throws Exception {
    assertUtilityClassWellDefined(AppVersionHelper.class);
  }

  @Test
  public void getCurrentAppVersion() {
    int currentAppVersion = AppVersionHelper.getCurrentAppVersion(testContext);
    assertTrue(currentAppVersion > 0);
    assertTrue(currentAppVersion < Integer.MAX_VALUE);
  }

  @Test
  public void isAppUpdated_false() {
    AppVersionHelper.updateAppVersionInPreferences(testContext);

    assertFalse(AppVersionHelper.isAppUpdated(testContext));
  }

  @Test
  public void isAppUpdated_true() {
    int currentAppVersion = AppVersionHelper.getCurrentAppVersion(testContext);
    prefs.edit().putInt(Constants.PREF_CURRENT_APP_VERSION, currentAppVersion - 1).commit();

    assertTrue(AppVersionHelper.isAppUpdated(testContext));
  }

  @Test
  public void getAppVersionFromPreferences() {
    prefs.edit().clear().commit();

    assertEquals(1, AppVersionHelper.getAppVersionFromPreferences(testContext));
  }

  @Test
  public void getCurrentAppVersionName() {
    var currentAppVersionName = AppVersionHelper.getCurrentAppVersionName(testContext);

    assertTrue(currentAppVersionName.matches(VERSION_NAME_REGEX));
  }

  @Test
  public void shouldWorkLegacyVersionManagementRetrieval() {
    prefs.edit().putString(Constants.PREF_CURRENT_APP_VERSION, "5.2.6 Beta 1").commit();

    var currentAppVersion = AppVersionHelper.getCurrentAppVersion(testContext);
    var saveAppVersion = AppVersionHelper.getAppVersionFromPreferences(testContext);

    assertEquals(currentAppVersion - 1, saveAppVersion);
  }

  @Test
  public void shouldWorkLegacyVersionManagementUpdateCheck() {
    prefs.edit().putString(Constants.PREF_CURRENT_APP_VERSION, "5.2.6 Beta 1").commit();

    assertTrue(AppVersionHelper.isAppUpdated(testContext));
  }

}
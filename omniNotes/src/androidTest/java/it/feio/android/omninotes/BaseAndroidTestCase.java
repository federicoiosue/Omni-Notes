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

package it.feio.android.omninotes;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static java.util.Locale.ENGLISH;
import static org.junit.Assert.assertFalse;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.rule.GrantPermissionRule;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.utils.Constants;
import java.util.Locale;
import org.junit.BeforeClass;
import org.junit.Rule;


public class BaseAndroidTestCase {

  protected static final Locale PRESET_LOCALE = new Locale(ENGLISH.toString());
  protected static DbHelper dbHelper;
  protected static Context testContext;
  protected static SharedPreferences prefs;

  @Rule
  public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
      ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, RECORD_AUDIO
  );

  @BeforeClass
  public static void setUpBeforeClass () {
    testContext = ApplicationProvider.getApplicationContext();
    prefs = testContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS);

    dbHelper = DbHelper.getInstance(testContext);
    cleanDatabase();
    assertFalse("Database MUST be writable", dbHelper.getDatabase(true).isReadOnly());

    Locale.setDefault(PRESET_LOCALE);
    Configuration config = testContext.getResources().getConfiguration();
    config.locale = PRESET_LOCALE;
  }

  private static void cleanDatabase () {
    dbHelper.getDatabase(true).delete(DbHelper.TABLE_NOTES, null, null);
    dbHelper.getDatabase(true).delete(DbHelper.TABLE_CATEGORY, null, null);
    dbHelper.getDatabase(true).delete(DbHelper.TABLE_ATTACHMENTS, null, null);
  }

}

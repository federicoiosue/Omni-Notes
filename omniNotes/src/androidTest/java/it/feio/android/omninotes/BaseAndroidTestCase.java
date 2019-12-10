/*
 * Copyright (C) 2013-2019 Federico Iosue (federico@iosue.it)
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

import static org.junit.Assert.assertFalse;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.utils.Constants;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;


public class BaseAndroidTestCase {

  private final static String DB_PATH_REGEX = ".*it\\.feio\\.android\\.omninotes.*\\/databases\\/test_omni-notes.*";
  private final static String DB_PREFIX = "test_";

  protected static DbHelper dbHelper;
  protected static Context testContext;
  protected static SharedPreferences prefs;

  @Rule
  public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
      Manifest.permission.ACCESS_COARSE_LOCATION,
      Manifest.permission.READ_EXTERNAL_STORAGE,
      Manifest.permission.WRITE_EXTERNAL_STORAGE,
      Manifest.permission.RECORD_AUDIO
  );

  @BeforeClass
  public static void setUpBeforeClass () {
    testContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    prefs = testContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS);
    dbHelper = DbHelper.getInstance(testContext);
    prefs = testContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
//		assertTrue("Database used for tests MUST not be the default one but prefixed by '" + DB_PREFIX + "'", dbHelper
//				.getDatabase().getPath().matches(DB_PATH_REGEX));
    assertFalse("Database MUST be writable", dbHelper.getDatabase().isReadOnly());
    cleanDatabase();
  }

  @AfterClass
  public static void tearDownAfterClass () {
    testContext.deleteDatabase(DbHelper.getInstance().getDatabaseName());
  }

  protected static void cleanDatabase () {
    dbHelper.getDatabase(true).delete(DbHelper.TABLE_NOTES, null, null);
    dbHelper.getDatabase(true).delete(DbHelper.TABLE_CATEGORY, null, null);
    dbHelper.getDatabase(true).delete(DbHelper.TABLE_ATTACHMENTS, null, null);
  }

}

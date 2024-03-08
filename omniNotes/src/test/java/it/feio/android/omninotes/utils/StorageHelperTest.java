/*
 * Copyright (C) 2013-2024 Federico Iosue (federico@iosue.it)
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

import static org.junit.Assert.*;

import android.os.Environment;
import it.feio.android.omninotes.BaseUnitTest;
import java.io.File;
import org.junit.After;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class StorageHelperTest extends BaseUnitTest {

  private static final File EXTERNAL_STORAGE_DIRECTORY = new File("/tmp/" + StorageHelperTest.class.getSimpleName());

  @After
  public void tearDown() {
    EXTERNAL_STORAGE_DIRECTORY.deleteOnExit();
  }

  @Test
  public void getOrCreateBackupDir() {
    try (MockedStatic<Environment> env = Mockito.mockStatic(Environment.class)) {
      env.when(Environment::getExternalStorageDirectory).thenReturn(EXTERNAL_STORAGE_DIRECTORY);

      var backupDir = StorageHelper.getOrCreateBackupDir("backup");

      assertTrue(backupDir.exists());
    }
  }



}
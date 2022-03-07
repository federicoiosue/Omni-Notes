/*
 * Copyright (C) 2013-2021 Federico Iosue (federico@iosue.it)
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
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Environment.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class StorageHelperTest extends BaseUnitTest {

  private static final File EXTERNAL_STORAGE_DIRECTORY = new File("/tmp/" + StorageHelperTest.class.getSimpleName());

  @After
  public void tearDown() {
    EXTERNAL_STORAGE_DIRECTORY.deleteOnExit();
  }

  @Test
  public void getOrCreateBackupDir() {
    PowerMockito.stub(PowerMockito.method(Environment.class, "getExternalStorageDirectory"))
        .toReturn(EXTERNAL_STORAGE_DIRECTORY);

    File backupDir = StorageHelper.getOrCreateBackupDir("backup");

    assertTrue(backupDir.exists());
  }



}
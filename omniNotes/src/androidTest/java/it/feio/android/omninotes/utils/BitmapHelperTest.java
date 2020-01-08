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

package it.feio.android.omninotes.utils;

import static it.feio.android.omninotes.utils.ConstantsBase.MIME_TYPE_IMAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Looper;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.internal.runner.junit4.statement.UiThreadStatement;
import it.feio.android.omninotes.BaseAndroidTestCase;
import it.feio.android.omninotes.models.Attachment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BitmapHelperTest extends BaseAndroidTestCase {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  private Attachment attachment;

  @Before
  public void setUp () throws IOException {
    File bitmapFile = tempFolder.newFile("bitmapFile.bmp");

    Bitmap bmp = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);
    FileOutputStream out = new FileOutputStream(bitmapFile);
    bmp.compress(Bitmap.CompressFormat.PNG, 100, out);

    attachment = new Attachment(Uri.fromFile(bitmapFile), MIME_TYPE_IMAGE);
  }

  @Test
  public void getBitmapFromAttachment_backgroundThread () {
    Bitmap bmp = BitmapHelper.getBitmapFromAttachment(testContext, attachment, 100, 100);
    assertNotEquals("Thread MUST be a background one", Looper.getMainLooper(), Looper.myLooper());
    assertNotNull("Bitmap should not be null", bmp);
  }

  @Test
  public void getBitmapFromAttachment_mainThread () throws Throwable {
    UiThreadStatement.runOnUiThread(() -> {
      Bitmap bmp = BitmapHelper.getBitmapFromAttachment(testContext, attachment, 100, 100);
      assertEquals("Thread MUST be a the main one", Looper.getMainLooper(), Looper.myLooper());
      assertNotNull("Bitmap should not be null", bmp);
    });
  }

}
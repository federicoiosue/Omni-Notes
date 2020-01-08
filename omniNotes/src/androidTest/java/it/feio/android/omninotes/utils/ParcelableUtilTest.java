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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import android.net.Uri;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import it.feio.android.omninotes.BaseAndroidTestCase;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ParcelableUtilTest extends BaseAndroidTestCase {

  Note testNote;
  private byte[] marshalledNote;

  @Before
  public void setUp () {
    testNote = new Note();
    testNote.setAttachmentsList(Collections.singletonList(new Attachment(Uri.EMPTY, "")));
    marshalledNote = ParcelableUtil.marshall(testNote);
  }

  @Test
  public void unmarshall () {
    assertEquals(testNote, ParcelableUtil.unmarshall(marshalledNote, Note.CREATOR));
  }

  @Test
  public void marshall () {
    assertArrayEquals(marshalledNote, ParcelableUtil.marshall(testNote));
  }
}
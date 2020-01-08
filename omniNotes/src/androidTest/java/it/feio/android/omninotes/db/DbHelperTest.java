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

package it.feio.android.omninotes.db;

import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import it.feio.android.omninotes.BaseAndroidTestCase;
import it.feio.android.omninotes.models.Note;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class DbHelperTest extends BaseAndroidTestCase {

  @Test
  public void testGetNotesByTag () {
    Note note = new Note();
    note.setTitle("title with #tag inside");
    note.setContent("useless content");
    dbHelper.updateNote(note, true);
    Note note1 = new Note();
    note1.setTitle("simple title");
    note1.setContent("content with #tag");
    dbHelper.updateNote(note1, true);
    Note note2 = new Note();
    note2.setTitle("title without tags in it");
    note2.setContent("some \n #tagged content");
    dbHelper.updateNote(note2, true);
    assertEquals(2, dbHelper.getNotesByTag("#tag").size());
    assertEquals(1, dbHelper.getNotesByTag("#tagged").size());
  }

  @Test
  public void getNotesByPatternEscaped () {
    Note note1 = new Note();
    note1.setTitle("title one");
    note1.setContent("content with _ (underscore) inside");
    dbHelper.updateNote(note1, true);
    Note note2 = new Note();
    note2.setTitle("title two");
    note2.setContent("useless content");
    dbHelper.updateNote(note2, true);
    Note note3 = new Note();
    note3.setTitle("title three");
    note3.setContent("content with % (percentage) inside");
    dbHelper.updateNote(note3, true);
    assertEquals(1, dbHelper.getNotesByPattern("_").size());
    assertEquals(1, dbHelper.getNotesByPattern("%").size());
  }

}

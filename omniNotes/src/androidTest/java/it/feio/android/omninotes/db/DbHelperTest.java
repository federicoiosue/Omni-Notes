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

package it.feio.android.omninotes.db;

import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import it.feio.android.omninotes.testutils.BaseAndroidTestCase;
import it.feio.android.omninotes.models.Note;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class DbHelperTest extends BaseAndroidTestCase {

  @Test
  public void testGetNotesByTag() {
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
  public void testGetNotesByTag_excludeLocked() {
    var titleTag = "#titleTag";
    var contentTag = "#tag";

    var note1 = new Note();
    note1.setTitle("simple note with " + titleTag);
    note1.setContent("content with " + contentTag);
    dbHelper.updateNote(note1, true);
    var note2 = new Note();
    note2.setTitle("protected note with " + titleTag);
    note2.setContent("content with same tag " + contentTag);
    note2.setLocked(true);
    dbHelper.updateNote(note2, true);

    var result = dbHelper.getNotesByTag(contentTag);
    assertEquals(1, result.size());
    assertEquals(note1.getTitle(), result.get(0).getTitle());
    assertEquals(2, dbHelper.getNotesByTag(titleTag).size());
  }

  @Test
  public void getNotesByPatternEscaped() {
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

  @Test
  public void getNotesByPattern_excludeLocked() {
    var note1 = new Note();
    note1.setTitle("title one");
    note1.setContent("This note is clear");
    dbHelper.updateNote(note1, true);
    var note2 = new Note();
    note2.setTitle("title two");
    note2.setContent("This note is protected");
    note2.setLocked(true);
    dbHelper.updateNote(note2, true);

    var result = dbHelper.getNotesByPattern("This note is ");

    assertEquals(1, result.size());
    assertEquals(note1.getTitle(), result.get(0).getTitle());
  }

  @Test
  public void getTags() {
    var note1 = new Note();
    note1.setTitle("title1");
    note1.setContent("#tag1");
    dbHelper.updateNote(note1, true);
    var note2 = new Note();
    note2.setTitle("title2#");
    note2.setContent("#tag2");
    note2.setLocked(true);
    dbHelper.updateNote(note2, true);

    var tags = dbHelper.getTags();

    assertEquals(1, tags.size());
    assertEquals(note1.getContent(), tags.get(0).getText());
  }

}

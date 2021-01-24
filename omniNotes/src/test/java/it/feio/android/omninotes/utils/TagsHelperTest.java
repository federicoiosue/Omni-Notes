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

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import androidx.core.util.Pair;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.Tag;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.junit.Before;
import org.junit.Test;


public class TagsHelperTest {

  private static Tag TAG1 = new Tag("#mixed", 1);
  private static Tag TAG2 = new Tag("#tags", 1);
  private static Tag TAG3 = new Tag("#tag", 1);
  private static Tag TAG4 = new Tag("#numberedAfter123", 1);
  private static Tag TAG_INVALID = new Tag("#123numbered", 1);

  private Note note;


  @Before
  public void setup() {
    note = new Note();
    note.setContent(
        "Random content with " + TAG1.getText() + " " + TAG_INVALID.getText() + " " + TAG2.getText()
            + "(and another with similar prefix: " + TAG3.getText() + ") and " + TAG4.getText());
  }

  @Test
  public void retrievesTagsFromNote() {
    HashMap<String, Integer> tags = TagsHelper.retrieveTags(note);
    assertEquals(tags.size(), 4);
    assertTrue(tags.containsKey(TAG1.getText()) && tags.containsKey(TAG2.getText()) && tags
        .containsKey(TAG3.getText())
        && tags.containsKey(TAG4.getText()));
    assertFalse(tags.containsKey(TAG_INVALID.getText()));
  }

  @Test
  public void retrievesTagsFromNoteMultilanguage() {
    note.setContent("#привет");
    HashMap<String, Integer> tags = TagsHelper.retrieveTags(note);
    assertTrue(tags.containsKey("#привет"));

    note.setContent("#中华人民共和国");
    tags = TagsHelper.retrieveTags(note);
    assertTrue(tags.containsKey("#中华人民共和国"));
  }

  @Test
  public void getPreselectedTagsArray() {
    final Tag anotherTag = new Tag("#anotherTag", 1);
    Note anotherNote = new Note();
    anotherNote.setContent(TAG1.getText() + " " + TAG2.getText() + " " + anotherTag);
    note.setContent(note.getContent().replace(TAG4.toString(), ""));

    List<Tag> tags = Arrays.asList(TAG1, TAG2, TAG3, TAG4, anotherTag);
    List<Note> notes = Arrays.asList(note, anotherNote);

    Integer[] preselectedTags = TagsHelper.getPreselectedTagsArray(notes, tags);

    assertEquals(preselectedTags.length, 4);
    for (Integer preselectedTag : preselectedTags) {
      assertNotEquals((int) preselectedTag, tags.indexOf(TAG4));
    }
  }

  @Test
  public void removesTags_noteCheck() {
    String title = TagsHelper.removeTags(note.getTitle(), singletonList(new Tag(TAG3.getText(), 4)));
    String content = TagsHelper.removeTags(note.getContent(), singletonList(new Tag(TAG3.getText(), 4)));
    note.setTitle(title);
    note.setContent(content);

    HashMap<String, Integer> tags = TagsHelper.retrieveTags(note);

    assertTrue(tags.containsKey(TAG1.getText()));
    assertTrue(tags.containsKey(TAG2.getText()));
    assertFalse(tags.containsKey(TAG_INVALID.getText()));
    assertFalse(tags.containsKey(TAG3.getText()));
  }

  @Test
  public void addsTagsToNote() {
    String newTag = "#addedTag";
    List<Tag> tags = new ArrayList<>();
    tags.add(new Tag(newTag, 1));
    tags.add(TAG2);
    Pair<String, List<Tag>> newTags = TagsHelper.addTagToNote(tags, new Integer[]{0, 1}, note);
    assertTrue(newTags.first.contains(newTag));
    assertFalse(newTags.first.contains(TAG2.getText()));
  }

  @Test
  public void TestTagWithComma() {
    String newTag = "#comma,comma";
    List<Tag> tags = new ArrayList<>();
    tags.add(new Tag(newTag, 1));
    Pair<String, List<Tag>> newTags = TagsHelper.addTagToNote(tags, new Integer[]{0, 1}, note);
    HashMap<String, Integer> tags1 = TagsHelper.retrieveTags(note);
    assertTrue(newTags.first.contains(newTag));
    assertFalse(tags1.containsKey(newTag));
  }

  @Test
  public void removeTags_specialCharsKeeped () {
    String text = "<>[],-.(){}!?\n\t text";
    String testString = text + " " + TAG1.getText();

    String result = TagsHelper.removeTags(testString, singletonList(TAG1));

    assertEquals(text, result);
  }

  @Test
  public void removeTagFromWord() {
    String word = TAG3 + "(and";

    String result = TagsHelper.removeTagFromWord(word, TAG3);

    assertEquals("(and", result);
  }

}

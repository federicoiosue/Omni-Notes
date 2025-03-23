package it.feio.android.omninotes.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.StatsSingleNote;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NotesHelperInstrumentationTest {

  private Context context;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
  }

  @Test
  public void testHaveSameId() {
    Note note1 = new Note();
    note1.set_id(1L);
    Note note2 = new Note();
    note2.set_id(1L);
    Note note3 = new Note();
    note3.set_id(2L);

    assertTrue(NotesHelper.haveSameId(note1, note2));
    assertFalse(NotesHelper.haveSameId(note1, note3));
  }

  @Test
  public void testMergeNotes() {
    Note note1 = new Note();
    note1.setTitle("Title 1");
    note1.setContent("Content 1");

    Note note2 = new Note();
    note2.setTitle("Title 2");
    note2.setContent("Content 2");

    List<Note> notes = Arrays.asList(note1, note2);
    Note mergedNote = NotesHelper.mergeNotes(notes, false);

    assertNotNull(mergedNote);
    assertEquals("Title 1", mergedNote.getTitle());
    assertTrue(mergedNote.getContent().contains("Content 1"));
    assertTrue(mergedNote.getContent().contains("Content 2"));
  }

  @Test
  public void testGetNoteInfo() {
    Note note = new Note();
    note.setContent("This is a test note with some words.");

    StatsSingleNote info = NotesHelper.getNoteInfo(note);

    assertNotNull(info);
    assertEquals(8, info.getWords());
    assertEquals(29, info.getChars());
  }

  @Test
  public void testAddAttachments() {
    Note note = new Note();
    note.setAttachmentsList(new ArrayList<>());

    ArrayList<Attachment> attachments = new ArrayList<>();
    NotesHelper.addAttachments(false, note, attachments);

    assertEquals(0, attachments.size());
  }
}
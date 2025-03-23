package it.feio.android.omninotes.helpers.count;

import static org.junit.Assert.assertEquals;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.testutils.BaseAndroidTestCase;

import org.junit.Test;

public class DefaultWordCounterIntegrationTest extends BaseAndroidTestCase {

  private final String CHECKED_SYM = it.feio.android.checklistview.interfaces.Constants.CHECKED_SYM;
  private final String UNCHECKED_SYM = it.feio.android.checklistview.interfaces.Constants.UNCHECKED_SYM;

  @Test
  public void testCountWordsWithSimpleText() {
    Note note = new Note();
    note.set_id(1L);
    note.setTitle("Hello World");
    note.setContent("This is a test note.");
    assertEquals(7, new DefaultWordCounter().countWords(note));
  }

  @Test
  public void testCountWordsWithChecklist() {
    String content = CHECKED_SYM + "Task 1\n" + UNCHECKED_SYM + "Task 2";
    Note note = new Note();
    note.set_id(1L);
    note.setTitle("Checklist");
    note.setContent(content);
    note.setChecklist(true);
    assertEquals(3, new DefaultWordCounter().countWords(note));
  }

  @Test
  public void testCountCharsWithSimpleText() {
    Note note = new Note();
    note.set_id(1L);
    note.setTitle("Hello World");
    note.setContent("This is a test note.");
    assertEquals(26, new DefaultWordCounter().countChars(note));
  }

  @Test
  public void testCountCharsWithChecklist() {
    String content = CHECKED_SYM + "Task 1\n" + UNCHECKED_SYM + "Task 2";
    Note note = new Note();
    note.set_id(1L);
    note.setTitle("Checklist");
    note.setContent(content);
    note.setChecklist(true);
    assertEquals(19, new DefaultWordCounter().countChars(note));
  }
}
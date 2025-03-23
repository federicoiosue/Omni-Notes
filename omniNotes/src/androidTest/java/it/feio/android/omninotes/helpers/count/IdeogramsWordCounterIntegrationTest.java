package it.feio.android.omninotes.helpers.count;

import static org.junit.Assert.assertEquals;

import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.testutils.BaseAndroidTestCase;

import org.junit.Test;

public class IdeogramsWordCounterIntegrationTest extends BaseAndroidTestCase {

  private final String CHECKED_SYM = it.feio.android.checklistview.interfaces.Constants.CHECKED_SYM;
  private final String UNCHECKED_SYM = it.feio.android.checklistview.interfaces.Constants.UNCHECKED_SYM;

  @Test
  public void testCountCharsWithSimpleNote() {
    Note note = new Note();
    note.setTitle("这是中文测试");
    note.setContent("這是中文測試\nこれは日本語のテストです");
    IdeogramsWordCounter counter = new IdeogramsWordCounter();
    assertEquals(24, counter.countChars(note));
  }

  @Test
  public void testCountCharsWithChecklistNote() {
    Note note = new Note();
    note.setTitle("这是中文测试");
    note.setContent(CHECKED_SYM + "這是中文測試\n" + UNCHECKED_SYM + "これは日本語のテストです");
    note.setChecklist(true);
    IdeogramsWordCounter counter = new IdeogramsWordCounter();
    assertEquals(24, counter.countChars(note));
  }

  @Test
  public void testCountWordsWithSimpleNote() {
    Note note = new Note();
    note.setTitle("这是中文测试");
    note.setContent("這是中文測試\nこれは日本語のテストです");
    IdeogramsWordCounter counter = new IdeogramsWordCounter();
    assertEquals(24, counter.countWords(note));
  }

  @Test
  public void testCountWordsWithChecklistNote() {
    Note note = new Note();
    note.setTitle("这是中文测试");
    note.setContent(CHECKED_SYM + "這是中文測試\n" + UNCHECKED_SYM + "これは日本語のテストです");
    note.setChecklist(true);
    IdeogramsWordCounter counter = new IdeogramsWordCounter();
    assertEquals(24, counter.countWords(note));
  }
}
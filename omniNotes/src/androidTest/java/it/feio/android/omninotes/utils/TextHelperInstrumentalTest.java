package it.feio.android.omninotes.utils;

import android.content.Context;
import android.text.Spanned;
import android.text.SpannedString;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.pixplicity.easyprefs.library.Prefs;
import it.feio.android.omninotes.models.Note;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TextHelperInstrumentalTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        // Initialize Prefs with the correct context
        Prefs.Builder builder = new Prefs.Builder();
        builder.setContext(context).build();
    }

    @Test
    public void testParseTitleAndContent() {
        Note note = new Note();
        note.setTitle("Test Title");
        note.setContent("Test Content");
        note.setLocked(false);
        note.setChecklist(false);

        Spanned[] result = TextHelper.parseTitleAndContent(context, note);
        assertEquals("Test Title", result[0].toString());
        assertEquals("Test Content", result[1].toString());
    }

    @Test
    public void testCapitalize() {
        String input = "test";
        String expected = "Test";
        assertEquals(expected, TextHelper.capitalize(input));
    }

    @Test
    public void testCheckIntentCategory() {
        String sqlCondition = "category_id = 5";
        assertEquals("5", TextHelper.checkIntentCategory(sqlCondition));
    }

    @Test
    public void testGetDateText() {
        Note note = new Note();
        note.setCreation(System.currentTimeMillis());
        note.setLastModification(System.currentTimeMillis());

        String dateText = TextHelper.getDateText(context, note, 0);
        assertNotNull(dateText);
    }

    @Test
    public void testGetAlternativeTitle() {
        Note note = new Note();
        note.setCreation(System.currentTimeMillis());

        Spanned spanned = new SpannedString(""); // Correct declaration of SpannedString
        String alternativeTitle = TextHelper.getAlternativeTitle(context, note, spanned);
        assertNotNull(alternativeTitle);
    }
}
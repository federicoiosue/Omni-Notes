package it.feio.android.omninotes.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.core.util.Pair;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.Tag;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TagsHelperInstrumentationTest {

    private Context context;
    private Note note;
    private List<Tag> tags;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        note = new Note();
        tags = new ArrayList<>();
        tags.add(new Tag("#test", 1));
        tags.add(new Tag("#android", 1));
        tags.add(new Tag("#instrumentation", 1));
    }

    @Test
    public void testRetrieveTags() {
        note.setContent("#test #android #instrumentation");
        Map<String, Integer> retrievedTags = TagsHelper.retrieveTags(note);
        assertEquals(3, retrievedTags.size());
        assertTrue(retrievedTags.containsKey("#test"));
        assertTrue(retrievedTags.containsKey("#android"));
        assertTrue(retrievedTags.containsKey("#instrumentation"));
    }

    @Test
    public void testAddTagToNote() {
        Integer[] selectedTags = {0, 1};
        Pair<String, List<Tag>> result = TagsHelper.addTagToNote(tags, selectedTags, note);
        assertTrue(result.first.contains("#test"));
        assertTrue(result.first.contains("#android"));
        assertFalse(result.first.contains("#instrumentation"));
    }

    @Test
    public void testRemoveTags() {
        note.setContent("#test #android #instrumentation");
        String result = TagsHelper.removeTags(note.getContent(), Arrays.asList(tags.get(0), tags.get(1)));
        assertFalse(result.contains("#test"));
        assertFalse(result.contains("#android"));
        assertTrue(result.contains("#instrumentation"));
    }

    @Test
    public void testGetTagsArray() {
        String[] tagsArray = TagsHelper.getTagsArray(tags);
        assertEquals("test (1)", tagsArray[0]);
        assertEquals("android (1)", tagsArray[1]);
        assertEquals("instrumentation (1)", tagsArray[2]);
    }

    @Test
    public void testGetPreselectedTagsArray() {
        note.setContent("#test #android");
        Integer[] preselectedTags = TagsHelper.getPreselectedTagsArray(note, tags);
        assertEquals(2, preselectedTags.length);
        assertTrue(Arrays.asList(preselectedTags).contains(0));
        assertTrue(Arrays.asList(preselectedTags).contains(1));
    }
}
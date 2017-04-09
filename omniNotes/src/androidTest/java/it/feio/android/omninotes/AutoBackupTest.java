package it.feio.android.omninotes;


import android.support.test.espresso.ViewInteraction;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.view.GravityCompat;
import android.test.suitebuilder.annotation.LargeTest;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.List;

import it.feio.android.omninotes.helpers.BackupHelper;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.StorageHelper;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AutoBackupTest extends BaseEspressoTest {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        prefs.edit().putBoolean(Constants.PREF_ENABLE_AUTOBACKUP, false).apply();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        File backupFolder = StorageHelper.getBackupDir(Constants.AUTO_BACKUP_DIR);
        FileUtils.deleteDirectory(backupFolder);
    }

    @Test
    public void autoBackupActivationTest() {
        assertFalse(prefs.getBoolean(Constants.PREF_ENABLE_AUTOBACKUP, false));
        autoBackupActivationFromPreferences();
        assertTrue(prefs.getBoolean(Constants.PREF_ENABLE_AUTOBACKUP, false));
    }

    @Test
    public void autoBackupTest() {
        createNote("A Title", "A content");
        createNote("B Title", "B content");
        createNote("C Title", "C content");
        prefs.edit().putBoolean(Constants.PREF_ENABLE_AUTOBACKUP, true).apply();
        BackupHelper.startBackupService(Constants.AUTO_BACKUP_DIR);
        createNote("D Title", "D content");

        // Waiting a little to ensure background service completes auto backup
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<Note> currentNotes = dbHelper.getAllNotes(false);
        assertEquals(4, currentNotes.size());
        for (Note currentNote : currentNotes) {
            File backupNoteFile = BackupHelper.getBackupNoteFile(StorageHelper.getBackupDir(Constants.AUTO_BACKUP_DIR), currentNote);
            assertTrue(backupNoteFile.exists());
            Note backupNote = BackupHelper.getImportNote(backupNoteFile);
            assertEquals(backupNote, currentNote);
        }
    }

    private void autoBackupActivationFromPreferences() {

        ViewInteraction imageButton4 = onView(
                allOf(withContentDescription("drawer open"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        imageButton4.perform(click());

        ViewInteraction linearLayout = onView(
                allOf(withId(R.id.settings_view),
                        withParent(allOf(withId(R.id.left_drawer),
                                withParent(withId(R.id.navigation_drawer))))));
        linearLayout.perform(scrollTo(), click());

        ViewInteraction linearLayout2 = onView(
                allOf(childAtPosition(
                        allOf(withId(android.R.id.list),
                                withParent(withClassName(is("android.widget.FrameLayout")))),
                        1),
                        isDisplayed()));
        linearLayout2.perform(click());

        ViewInteraction linearLayout3 = onView(
                allOf(childAtPosition(
                        allOf(withId(android.R.id.list),
                                withParent(withClassName(is("android.widget.FrameLayout")))),
                        0),
                        isDisplayed()));
        linearLayout3.perform(click());

        ViewInteraction linearLayout4 = onView(
                allOf(childAtPosition(
                        allOf(withId(android.R.id.list),
                                withParent(withClassName(is("android.widget.FrameLayout")))),
                        4),
                        isDisplayed()));
        linearLayout4.perform(click());

        ViewInteraction mDButton = onView(
                allOf(withId(R.id.buttonDefaultPositive), withText("Confirm"), isDisplayed()));
        mDButton.perform(click());

        ViewInteraction imageButton5 = onView(
                allOf(withContentDescription("Navigate up"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        imageButton5.perform(click());

        ViewInteraction imageButton6 = onView(
                allOf(withContentDescription("Navigate up"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        imageButton6.perform(click());

        ViewInteraction imageButton7 = onView(
                allOf(withContentDescription("Navigate up"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        imageButton7.perform(click());
    }

    private void createNote(String title, String content) {
        ViewInteraction viewInteraction = onView(
                allOf(withId(R.id.fab_expand_menu_button),
                        withParent(withId(R.id.fab)),
                        isDisplayed()));

        if (mActivityTestRule.getActivity().getDrawerLayout().isDrawerOpen(GravityCompat.START)) {
            viewInteraction.perform(click());
        }
        viewInteraction.perform(click());

        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.fab_note),
                        withParent(withId(R.id.fab)),
                        isDisplayed()));
        floatingActionButton.perform(click());

        ViewInteraction editText = onView(
                allOf(withId(R.id.detail_title),
                        withParent(allOf(withId(R.id.title_wrapper),
                                withParent(withId(R.id.detail_tile_card)))),
                        isDisplayed()));
        editText.perform(click());

        ViewInteraction editText2 = onView(
                allOf(withId(R.id.detail_title),
                        withParent(allOf(withId(R.id.title_wrapper),
                                withParent(withId(R.id.detail_tile_card)))),
                        isDisplayed()));
        editText2.perform(replaceText(title), closeSoftKeyboard());

        ViewInteraction editText3 = onView(
                withId(R.id.detail_content));
        editText3.perform(scrollTo(), replaceText(content), closeSoftKeyboard());

        ViewInteraction imageButton = onView(
                allOf(withContentDescription("drawer open"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        imageButton.perform(click());
    }
}

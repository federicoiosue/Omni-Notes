package it.feio.android.omninotes;

import android.support.test.espresso.ViewInteraction;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.apache.commons.io.FileUtils;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import it.feio.android.omninotes.helpers.BackupHelper;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.ConstantsBase;
import it.feio.android.omninotes.utils.StorageHelper;

import static android.support.test.espresso.Espresso.onData;
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
import static org.hamcrest.Matchers.anything;
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
    public void autoBackupPreferenceActivation() {
        assertFalse(prefs.getBoolean(Constants.PREF_ENABLE_AUTOBACKUP, false));
        autoBackupActivationFromPreferences();
        assertTrue(prefs.getBoolean(Constants.PREF_ENABLE_AUTOBACKUP, false));
    }

    @Test
    public void autoBackupWithNotesCheck() throws InterruptedException {
        createNote("A Title", "A content");
        enableAutobackup();
        createNote("B Title", "B content");

        // Waiting a little to ensure background service completes auto backup
        Thread.sleep(2000);

        List<Note> currentNotes = dbHelper.getAllNotes(false);
        assertEquals(2, currentNotes.size());
        for (Note currentNote : currentNotes) {
            File backupNoteFile = BackupHelper.getBackupNoteFile(StorageHelper.getBackupDir(Constants.AUTO_BACKUP_DIR)
                    , currentNote);
            assertTrue(backupNoteFile.exists());
            Note backupNote = BackupHelper.getImportNote(backupNoteFile);
            assertEquals(backupNote, currentNote);
        }

    }

    @Test
    public void everyUpdateToNotesShouldTriggerAutobackup() throws InterruptedException {

        enableAutobackup();

        createNote("C Title", "C content");

        assertAutobackupIsCorrect();

        // Category addition

        onData(anything()).inAdapterView(withId(R.id.list)).atPosition(0).perform(click());

        onView(allOf(withId(R.id.menu_category), withContentDescription(R.string.category),
                childAtPosition(
                        childAtPosition(
                                withId(R.id.toolbar),
                                1),
                        1),
                isDisplayed())).perform(click());

        onView(allOf(withId(R.id.buttonDefaultPositive), withText(R.string.add_category),
                childAtPosition(
                        childAtPosition(
                                withId(android.R.id.content),
                                0),
                        4),
                isDisplayed())).perform(click());

        onView(allOf(withId(R.id.category_title),
                childAtPosition(
                        childAtPosition(
                                withId(android.R.id.content),
                                0),
                        0),
                isDisplayed())).perform(replaceText("cat1"), closeSoftKeyboard());

        onView(allOf(withId(R.id.save), withText("Ok"), isDisplayed())).perform(click());

        navigateUp();

        assertAutobackupIsCorrect();

        // Reminder addition

        onData(anything()).inAdapterView(withId(R.id.list)).atPosition(0).perform(click());

        onView(allOf(withId(R.id.reminder_layout),
                childAtPosition(
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                1),
                        2))).perform(scrollTo(), click());

        onView(allOf(withId(R.id.done),
                childAtPosition(
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                3),
                        0),
                isDisplayed())).perform(click());

        onView(allOf(withId(R.id.done_button),
                childAtPosition(
                        childAtPosition(
                                withId(R.id.time_picker_dialog),
                                3),
                        0),
                isDisplayed())).perform(click());

        onView(allOf(withId(R.id.done),
                childAtPosition(
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                2),
                        0),
                isDisplayed())).perform(click());

        navigateUp();

        assertAutobackupIsCorrect();

        onData(anything()).inAdapterView(withId(R.id.list)).atPosition(0).perform(click());

        onView(allOf(withId(R.id.menu_attachment),
                childAtPosition(
                        childAtPosition(
                                withId(R.id.toolbar),
                                1),
                        0),
                isDisplayed())).perform(click());

        onView(allOf(withId(R.id.recording), withText(R.string.record),
                childAtPosition(
                        allOf(withId(R.id.attachment_dialog_root),
                                childAtPosition(
                                        withId(R.id.customViewFrame),
                                        0)),
                        3),
                isDisplayed())).perform(click());

        Thread.sleep(1000);

        onView(allOf(withId(R.id.recording),
                childAtPosition(
                        allOf(withId(R.id.attachment_dialog_root),
                                childAtPosition(
                                        withId(R.id.customViewFrame),
                                        0)),
                        3),
                isDisplayed())).perform(click());

        navigateUp();
        navigateUp();

        assertAutobackupIsCorrect();

    }

    private void enableAutobackup() {
        prefs.edit().putBoolean(Constants.PREF_ENABLE_AUTOBACKUP, true).apply();
        BackupHelper.startBackupService(Constants.AUTO_BACKUP_DIR);
    }

    private void assertAutobackupIsCorrect() {
        List<LinkedList<DiffMatchPatch.Diff>> autobackupDifferences = BackupHelper
                .integrityCheck(StorageHelper.getBackupDir(ConstantsBase.AUTO_BACKUP_DIR));
        assertEquals(0, autobackupDifferences.size());

    }

    private void autoBackupActivationFromPreferences() {

        onView(allOf(childAtPosition(allOf(withId(R.id.toolbar),
                childAtPosition(
                        withClassName(is("android.widget.RelativeLayout")),
                        0)),
                1),
                isDisplayed())).perform(click());

        getSettingsMenuItemView()
                .perform(scrollTo(), click());

        onView(allOf(childAtPosition(
                allOf(withId(android.R.id.list),
                        withParent(withClassName(is("android.widget.FrameLayout")))),
                1), isDisplayed()))
                .perform(click());

        onView(allOf(childAtPosition(
                allOf(withId(android.R.id.list),
                        withParent(withClassName(is("android.widget.FrameLayout")))),
                0), isDisplayed()))
                .perform(click());

        onView(allOf(childAtPosition(
                allOf(withId(android.R.id.list),
                        withParent(withClassName(is("android.widget.FrameLayout")))),
                4),
                isDisplayed())).perform(click());

        onView(allOf(withId(R.id.buttonDefaultPositive), isDisplayed())).perform(click());

        navigateUpSettings();
        navigateUpSettings();
        navigateUpSettings();
    }

    private ViewInteraction getSettingsMenuItemView() {
        boolean existsAtLeastOneCategory = dbHelper.getCategories().size() > 0;
        return existsAtLeastOneCategory ? onView(withId(R.id.drawer_tag_list)) : onView(withId(R.id.settings_view));
    }

}

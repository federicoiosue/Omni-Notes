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

package it.feio.android.omninotes.ui;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.helpers.BackupHelper;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.ConstantsBase;
import it.feio.android.omninotes.utils.StorageHelper;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@Ignore("Ignored until merging autobackup feature branch")
@LargeTest
@RunWith(AndroidJUnit4.class)
public class AutoBackupTest extends BaseEspressoTest {

  @Before
  @Override
  public void setUp () throws Exception {
    super.setUp();
    prefs.edit().putBoolean(Constants.PREF_ENABLE_AUTOBACKUP, false).apply();
  }

  @After
  public void tearDown () throws Exception {
    File backupFolder = StorageHelper.getBackupDir(Constants.AUTO_BACKUP_DIR);
    FileUtils.deleteDirectory(backupFolder);
  }

  @Test
  public void autoBackupPreferenceActivation () {
    assertFalse(prefs.getBoolean(Constants.PREF_ENABLE_AUTOBACKUP, false));
    autoBackupActivationFromPreferences();
    assertTrue(prefs.getBoolean(Constants.PREF_ENABLE_AUTOBACKUP, false));
  }

  @Test
  public void autoBackupWithNotesCheck () throws InterruptedException {
    createNote("A Title", "A content");
    enableAutobackup();
    createNote("B Title", "B content");

    // Waiting a little to ensure background service completes auto backup
    Thread.sleep(1200);

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
  public void everyUpdateToNotesShouldTriggerAutobackup () throws InterruptedException {

    enableAutobackup();

    createNote("C Title", "C content");

    assertAutobackupIsCorrect();

    // Category addition

    onData(anything()).inAdapterView(ViewMatchers.withId(R.id.list)).atPosition(0).perform(click());

    onView(allOf(withId(R.id.menu_category), withContentDescription(R.string.category),
        childAtPosition(
            childAtPosition(
                withId(R.id.toolbar),
                1),
            1),
        isDisplayed())).perform(click());

    onView(allOf(withId(R.id.buttonNegativeDP), withText(R.string.add_category),
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

    onView(allOf(withId(R.id.buttonPositive), withText("Ok"),
            childAtPosition(
                allOf(withId(R.id.button_layout),
                    childAtPosition(
                        withId(R.id.llMainContentHolder),
                        2)),
                5))).perform(click());

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
                    withId(R.id.md_customViewFrame),
                    0)),
            3),
        isDisplayed())).perform(click());

    Thread.sleep(1000);

    onView(allOf(withId(R.id.recording),
        childAtPosition(
            allOf(withId(R.id.attachment_dialog_root),
                childAtPosition(
                    withId(R.id.md_customViewFrame),
                    0)),
            3),
        isDisplayed())).perform(click());

    navigateUp();
    navigateUp();

    assertAutobackupIsCorrect();

  }

  private void enableAutobackup () {
    prefs.edit().putBoolean(Constants.PREF_ENABLE_AUTOBACKUP, true).apply();
    BackupHelper.startBackupService(Constants.AUTO_BACKUP_DIR);
  }

  private void assertAutobackupIsCorrect () {
    List<LinkedList<DiffMatchPatch.Diff>> autobackupDifferences = BackupHelper
        .integrityCheck(StorageHelper.getBackupDir(ConstantsBase.AUTO_BACKUP_DIR));
    assertTrue(autobackupDifferences.size() == 0);

  }

  private void autoBackupActivationFromPreferences () {

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

    onView(allOf(withId(R.id.buttonNegativeDP), isDisplayed())).perform(click());

    navigateUpSettings();
    navigateUpSettings();
    navigateUpSettings();
  }

  private ViewInteraction getSettingsMenuItemView () {
    boolean existsAtLeastOneCategory = dbHelper.getCategories().size() > 0;
    return existsAtLeastOneCategory ? onView(withId(R.id.drawer_tag_list)) : onView(withId(R.id.settings_view));
  }

}

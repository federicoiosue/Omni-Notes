package it.feio.android.omninotes;

import android.support.test.espresso.ViewInteraction;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.view.GravityCompat;
import android.test.suitebuilder.annotation.LargeTest;
import it.feio.android.omninotes.helpers.BackupHelper;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.ConstantsBase;
import it.feio.android.omninotes.utils.StorageHelper;
import org.apache.commons.io.FileUtils;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;


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
	public void everyUpdateToNotesShouldTriggerAutobackup() throws InterruptedException {

		enableAutobackup();

		createNote("C Title", "C content");

		assertAutobackupIsCorrect();

		// Category addition

		onData(anything()).inAdapterView(withId(R.id.list)).atPosition(0).perform(click());

		onView(allOf(withId(R.id.menu_category), withContentDescription("Category"),
				childAtPosition(
						childAtPosition(
								withId(R.id.toolbar),
								1),
						1),
				isDisplayed())).perform(click());

		onView(allOf(withId(R.id.buttonDefaultPositive), withText("Add category"),
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

		onView(allOf(withContentDescription("drawer open"),
				childAtPosition(
						allOf(withId(R.id.toolbar),
								childAtPosition(
										withClassName(is("android.widget.RelativeLayout")),
										0)),
						0),
				isDisplayed())).perform(click());

		assertAutobackupIsCorrect();

		// Reminder addition

		onData(anything()).inAdapterView(withId(R.id.list)).atPosition(0).perform(click());

		onView(allOf(withId(R.id.reminder_layout),
				childAtPosition(
						childAtPosition(
								withClassName(is("android.widget.LinearLayout")),
								1),
						2))).perform(scrollTo(), click());

		onView(allOf(withId(R.id.done), withText("Done"),
				childAtPosition(
						childAtPosition(
								withClassName(is("android.widget.LinearLayout")),
								3),
						0),
				isDisplayed())).perform(click());

		onView(allOf(withId(R.id.done_button), withText("Done"),
				childAtPosition(
						childAtPosition(
								withId(R.id.time_picker_dialog),
								3),
						0),
				isDisplayed())).perform(click());

		onView(allOf(withId(R.id.done), withText("Done"),
				childAtPosition(
						childAtPosition(
								withClassName(is("android.widget.LinearLayout")),
								2),
						0),
				isDisplayed())).perform(click());

		onView(allOf(withContentDescription("drawer open"),
				childAtPosition(
						allOf(withId(R.id.toolbar),
								childAtPosition(
										withClassName(is("android.widget.RelativeLayout")),
										0)),
						0),
				isDisplayed())).perform(click());

		assertAutobackupIsCorrect();

		onData(anything()).inAdapterView(withId(R.id.list)).atPosition(0).perform(click());

		onView(allOf(withId(R.id.menu_attachment), withContentDescription("Attachment"),
				childAtPosition(
						childAtPosition(
								withId(R.id.toolbar),
								1),
						0),
				isDisplayed())).perform(click());

		onView(allOf(withId(R.id.recording), withText("Record"),
				childAtPosition(
						allOf(withId(R.id.attachment_dialog_root),
								childAtPosition(
										withId(R.id.customViewFrame),
										0)),
						3),
				isDisplayed())).perform(click());

		Thread.sleep(1000);

		onView(allOf(withId(R.id.recording), withText("Stop"),
				childAtPosition(
						allOf(withId(R.id.attachment_dialog_root),
								childAtPosition(
										withId(R.id.customViewFrame),
										0)),
						3),
				isDisplayed())).perform(click());

		onView(allOf(withContentDescription("drawer open"),
				childAtPosition(
						allOf(withId(R.id.toolbar),
								childAtPosition(
										withClassName(is("android.widget.RelativeLayout")),
										0)),
						0),
				isDisplayed())).perform(click());

		onView(allOf(withContentDescription("drawer open"),
				childAtPosition(
						allOf(withId(R.id.toolbar),
								childAtPosition(
										withClassName(is("android.widget.RelativeLayout")),
										0)),
						0),
				isDisplayed())).perform(click());

		assertAutobackupIsCorrect();

	}

	private void enableAutobackup() {
		prefs.edit().putBoolean(Constants.PREF_ENABLE_AUTOBACKUP, true).apply();
		BackupHelper.startBackupService(Constants.AUTO_BACKUP_DIR);
	}

	private void assertAutobackupIsCorrect() {
		List<LinkedList<DiffMatchPatch.Diff>> autobackupDifferences = BackupHelper
				.integrityCheck(StorageHelper.getBackupDir(ConstantsBase.AUTO_BACKUP_DIR));
		assertTrue(autobackupDifferences.size() == 0);

	}

	private void autoBackupActivationFromPreferences() {

		ViewInteraction imageButton4 = onView(
				allOf(withContentDescription("drawer open"),
						withParent(withId(R.id.toolbar)),
						isDisplayed()));
		imageButton4.perform(click());

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

	private ViewInteraction getSettingsMenuItemView() {
		boolean existsAtLeastOneCategory = dbHelper.getCategories().size() > 0;
		return existsAtLeastOneCategory ? onView(withId(R.id.drawer_tag_list)) : onView(withId(R.id.settings_view));
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

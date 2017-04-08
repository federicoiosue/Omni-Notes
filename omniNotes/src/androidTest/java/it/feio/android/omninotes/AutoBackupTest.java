package it.feio.android.omninotes;


import android.support.test.espresso.ViewInteraction;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.view.GravityCompat;
import android.test.suitebuilder.annotation.LargeTest;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.List;
import java.util.Observable;

import it.feio.android.omninotes.helpers.BackupHelper;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.StorageHelper;
import rx.functions.Func1;

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

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        File backupFolder = StorageHelper.getBackupDir(Constants.AUTO_BACKUP_DIR);
        FileUtils.deleteDirectory(backupFolder);
    }

    @Test
    public void autoBackupTest() {

        createsSomeNotes();

        autoBackupActivationFromPreferences();

        addAnotherNoteAfterAutobackupActivation();

        // Waiting a little to ensure Eventbus post propagation
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<Note> currentNotes = dbHelper.getAllNotes(false);
        assertEquals(4, currentNotes.size());
        List<Note> backupNotes = BackupHelper.importNotes(StorageHelper.getBackupDir(Constants.AUTO_BACKUP_DIR));
        assertEquals(4, backupNotes.size());
        currentNotes.containsAll(backupNotes);
    }

    private void addAnotherNoteAfterAutobackupActivation() {

        ViewInteraction viewInteraction4 = onView(
                allOf(withId(R.id.fab_expand_menu_button),
                        withParent(withId(R.id.fab)),
                        isDisplayed()));
        viewInteraction4.perform(click());

        if (mActivityTestRule.getActivity().getDrawerLayout().isDrawerOpen(GravityCompat.START)) {
            viewInteraction4.perform(click());
        }

        ViewInteraction viewInteraction = onView(
                allOf(withId(R.id.fab_expand_menu_button),
                        withParent(withId(R.id.fab)),
                        isDisplayed()));
        viewInteraction.perform(click());

        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.fab_note),
                        withParent(withId(R.id.fab)),
                        isDisplayed()));
        floatingActionButton.perform(click());

        ViewInteraction editText8 = onView(
                allOf(withId(R.id.detail_title),
                        withParent(allOf(withId(R.id.title_wrapper),
                                withParent(withId(R.id.detail_tile_card)))),
                        isDisplayed()));
        editText8.perform(replaceText("D"), closeSoftKeyboard());

        ViewInteraction editText9 = onView(
                withId(R.id.detail_content));
        editText9.perform(scrollTo(), replaceText("D1"), closeSoftKeyboard());

        ViewInteraction imageButton8 = onView(
                allOf(withContentDescription("drawer open"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        imageButton8.perform(click());
    }

    private void autoBackupActivationFromPreferences() {

        // Automatic backup preference must be turned off before testing its reactivation
        prefs.edit().putBoolean(Constants.PREF_ENABLE_AUTOBACKUP, false).apply();

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

    private void createsSomeNotes() {
        ViewInteraction viewInteraction = onView(
                allOf(withId(R.id.fab_expand_menu_button),
                        withParent(withId(R.id.fab)),
                        isDisplayed()));
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
        editText2.perform(replaceText("A"), closeSoftKeyboard());

        ViewInteraction editText3 = onView(
                withId(R.id.detail_content));
        editText3.perform(scrollTo(), replaceText("A1"), closeSoftKeyboard());

        ViewInteraction imageButton = onView(
                allOf(withContentDescription("drawer open"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        imageButton.perform(click());

        ViewInteraction viewInteraction2 = onView(
                allOf(withId(R.id.fab_expand_menu_button),
                        withParent(withId(R.id.fab)),
                        isDisplayed()));
        viewInteraction2.perform(click());

        ViewInteraction floatingActionButton2 = onView(
                allOf(withId(R.id.fab_note),
                        withParent(withId(R.id.fab)),
                        isDisplayed()));
        floatingActionButton2.perform(click());

        ViewInteraction editText4 = onView(
                allOf(withId(R.id.detail_title),
                        withParent(allOf(withId(R.id.title_wrapper),
                                withParent(withId(R.id.detail_tile_card)))),
                        isDisplayed()));
        editText4.perform(replaceText("B"), closeSoftKeyboard());

        ViewInteraction editText5 = onView(
                withId(R.id.detail_content));
        editText5.perform(scrollTo(), replaceText("B1"), closeSoftKeyboard());

        ViewInteraction imageButton2 = onView(
                allOf(withContentDescription("drawer open"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        imageButton2.perform(click());

        ViewInteraction viewInteraction3 = onView(
                allOf(withId(R.id.fab_expand_menu_button),
                        withParent(withId(R.id.fab)),
                        isDisplayed()));
        viewInteraction3.perform(click());

        ViewInteraction floatingActionButton3 = onView(
                allOf(withId(R.id.fab_note),
                        withParent(withId(R.id.fab)),
                        isDisplayed()));
        floatingActionButton3.perform(click());

        ViewInteraction editText6 = onView(
                allOf(withId(R.id.detail_title),
                        withParent(allOf(withId(R.id.title_wrapper),
                                withParent(withId(R.id.detail_tile_card)))),
                        isDisplayed()));
        editText6.perform(replaceText("C"), closeSoftKeyboard());

        ViewInteraction editText7 = onView(
                withId(R.id.detail_content));
        editText7.perform(scrollTo(), replaceText("C1"), closeSoftKeyboard());

        ViewInteraction imageButton3 = onView(
                allOf(withContentDescription("drawer open"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        imageButton3.perform(click());
    }
}

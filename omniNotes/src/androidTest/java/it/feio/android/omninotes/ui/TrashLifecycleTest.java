/*
 * Copyright (C) 2013-2026 Federico Iosue (omninotes@iosue.it)
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
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_PASSWORD;
import static java.lang.Thread.sleep;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import com.pixplicity.easyprefs.library.Prefs;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.NavigationItem;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.adapters.NoteAdapter;
import it.feio.android.omninotes.utils.Navigation;
import it.feio.android.omninotes.utils.Security;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class TrashLifecycleTest extends BaseEspressoTest {

  private static final long WAIT_TIMEOUT = 5000;
  private static final long POLL_INTERVAL = 50;
  private static final long KEYBOARD_DELAY = 500;
  private static final String PASSWORD = "uglypassword";
  private static final String PLAIN_NOTE_TITLE = "plain note";
  private static final String LOCKED_NOTE_TITLE = "protected note";

  @Test
  public void dismissingPasswordPromptDoesNotRestoreEveryTrashedNoteOnSwipe()
      throws InterruptedException {
    createTrashedNotes();

    navigateToTrash();
    emptyTrash();
    dismissPasswordPrompt();

    assertEquals("Dismissing the password prompt must not delete any note", 2,
        dbHelper.getNotesTrashed().size());

    onView(withId(R.id.list)).perform(
        RecyclerViewActions.actionOnItemAtPosition(trashPositionOf(PLAIN_NOTE_TITLE),
            new GeneralSwipeAction(
                Swipe.FAST, GeneralLocation.CENTER, GeneralLocation.CENTER_LEFT, Press.FINGER)));

    onView(isRoot()).perform(waitUntil("only the swiped note has been restored",
        () -> dbHelper.getNotesActive().size() == 1));

    assertEquals(PLAIN_NOTE_TITLE, dbHelper.getNotesActive().get(0).getTitle());
    assertEquals(1, dbHelper.getNotesTrashed().size());
    assertEquals(LOCKED_NOTE_TITLE, dbHelper.getNotesTrashed().get(0).getTitle());
  }

  @Test
  public void correctPasswordEmptiesTrash() throws InterruptedException {
    createTrashedNotes();

    navigateToTrash();
    emptyTrash();
    onView(isRoot()).perform(waitId(R.id.password_request, WAIT_TIMEOUT));
    sleep(KEYBOARD_DELAY);
    onView(withId(R.id.password_request)).perform(replaceText(PASSWORD), closeSoftKeyboard());
    onView(allOf(withId(R.id.md_buttonDefaultPositive), isDisplayed())).perform(click());

    onView(isRoot()).perform(waitUntil("trash is emptied",
        () -> dbHelper.getNotesTrashed().isEmpty() && dbHelper.getNotesActive().isEmpty()));

    assertTrue(dbHelper.getNotesTrashed().isEmpty());
    assertTrue(dbHelper.getNotesActive().isEmpty());
  }

  private void createTrashedNotes() {
    Prefs.putString(PREF_PASSWORD, Security.md5(PASSWORD));
    Note plainNote = createTestNote(PLAIN_NOTE_TITLE, "plain content", 0);
    Note lockedNote = createTestNote(LOCKED_NOTE_TITLE, "protected content", 0);
    lockedNote.setLocked(true);
    dbHelper.updateNote(lockedNote, false);
    trashNotes(Arrays.asList(plainNote, lockedNote), true);
  }

  private void navigateToTrash() {
    openDrawer();
    onView(isRoot()).perform(waitUntil("trash item is shown in the drawer",
        () -> isTrashItemPresent(activityRule.getActivity().findViewById(R.id.drawer_nav_list))));
    onData(isTrashItem())
        .inAdapterView(allOf(withId(R.id.drawer_nav_list),
            childAtPosition(withId(R.id.left_drawer), 1)))
        .perform(scrollTo(), click());
    onView(isRoot()).perform(waitUntil("trash list is shown", () -> notesListSize() == 2));
  }

  private static boolean isTrashItemPresent(ListView navigationList) {
    ListAdapter adapter = navigationList.getAdapter();
    if (adapter == null) {
      return false;
    }
    for (int i = 0; i < adapter.getCount(); i++) {
      if (isTrashItem().matches(adapter.getItem(i))) {
        return true;
      }
    }
    return false;
  }

  private static Matcher<Object> isTrashItem() {
    return new TypeSafeMatcher<>() {

      @Override
      protected boolean matchesSafely(Object item) {
        return item instanceof NavigationItem
            && ((NavigationItem) item).getArrayIndex() == Navigation.TRASH;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("the trash navigation item");
      }
    };
  }

  private int trashPositionOf(String noteTitle) {
    List<Note> trashedNotes = ((NoteAdapter) notesList().getAdapter()).getNotes();
    for (int i = 0; i < trashedNotes.size(); i++) {
      if (noteTitle.equals(trashedNotes.get(i).getTitle())) {
        return i;
      }
    }
    throw new AssertionError("Note titled '" + noteTitle + "' is not in the trash");
  }

  private int notesListSize() {
    return notesList().getAdapter() == null ? 0 : notesList().getAdapter().getItemCount();
  }

  private RecyclerView notesList() {
    return activityRule.getActivity().findViewById(R.id.list);
  }

  private void emptyTrash() {
    openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
    onView(withText(R.string.empty_trash)).perform(click());
    onView(isRoot()).perform(waitId(R.id.md_buttonDefaultPositive, WAIT_TIMEOUT));
    onView(allOf(withId(R.id.md_buttonDefaultPositive), isDisplayed())).perform(click());
  }

  private void dismissPasswordPrompt() throws InterruptedException {
    onView(isRoot()).perform(waitId(R.id.password_request, WAIT_TIMEOUT));
    sleep(KEYBOARD_DELAY);
    onView(withId(R.id.password_request)).perform(closeSoftKeyboard());
    onView(isRoot()).perform(pressBack());
  }

  private static ViewAction waitUntil(String description, BooleanSupplier condition) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isRoot();
      }

      @Override
      public String getDescription() {
        return "wait until " + description;
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();
        long deadline = System.currentTimeMillis() + WAIT_TIMEOUT;
        while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
          uiController.loopMainThreadForAtLeast(POLL_INTERVAL);
        }
      }
    };
  }

}

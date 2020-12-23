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


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.Note;
import java.io.IOException;
import java.util.List;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MrJingleLifecycleTest extends BaseEspressoTest {

  @Test
  public void mrJingle_displayedOnFreshStart() {
    onView(allOf(withId(R.id.empty_list), withText(R.string.no_items_in_list),
        withParent(withParent(IsInstanceOf.instanceOf(android.widget.FrameLayout.class))),
        isDisplayed())).check(matches(isDisplayed()));
  }

  @Test
  public void mrJingle_hiddenOnNoteCreated() {
    createTestNote("title", "content", 0);
    onView(allOf(withId(R.id.empty_list), withText(R.string.no_items_in_list),
        withParent(withParent(IsInstanceOf.instanceOf(android.widget.FrameLayout.class))),
        isDisplayed())).check(doesNotExist());
  }

  @Test
  public void mrJingle_displayedOnLastNoteArchived() {
    createTestNote("title", "content", 0);
    archiveNotes(dbHelper.getAllNotes(false), true);

    onView(allOf(withId(R.id.empty_list), withText(R.string.no_items_in_list),
        withParent(withParent(IsInstanceOf.instanceOf(android.widget.FrameLayout.class))),
        isDisplayed())).check(matches(isDisplayed()));
  }

  @Test
  public void mrJingle_displayedOnLastNoteTrashed() {
    createTestNote("title", "content", 0);
    trashNotes(dbHelper.getAllNotes(false), true);

    onView(allOf(withId(R.id.empty_list), withText(R.string.no_items_in_list),
        withParent(withParent(IsInstanceOf.instanceOf(android.widget.FrameLayout.class))),
        isDisplayed())).check(matches(isDisplayed()));
  }

  @Test
  public void mrJingle_hiddenOnArchiveWithNotes() {
    createTestNote("title", "content", 0);
    archiveNotes(dbHelper.getAllNotes(false), true);

    navigateTo(1);

    onView(allOf(withId(R.id.empty_list), withText(R.string.no_items_in_list),
        withParent(withParent(IsInstanceOf.instanceOf(android.widget.FrameLayout.class))),
        isDisplayed())).check(doesNotExist());
  }

  @Test
  public void mrJingle_displayedOnArchiveWhenEmptied() {
    createTestNote("title", "content", 0);
    List<Note> notes = dbHelper.getAllNotes(false);
    archiveNotes(notes, true);

    navigateTo(1);

    archiveNotes(notes, false);

    onView(allOf(withId(R.id.empty_list), withText(R.string.no_items_in_list),
        withParent(withParent(IsInstanceOf.instanceOf(android.widget.FrameLayout.class))),
        isDisplayed())).check(matches(isDisplayed()));
  }

  @Test
  public void mrJingle_hiddenOnTrashWithNotes() {
    createTestNote("title", "content", 0);
    trashNotes(dbHelper.getAllNotes(false), true);

    navigateTo(1);

    onView(allOf(withId(R.id.empty_list), withText(R.string.no_items_in_list),
        withParent(withParent(IsInstanceOf.instanceOf(android.widget.FrameLayout.class))),
        isDisplayed())).check(doesNotExist());
  }

  @Test
  public void mrJingle_displayedOnTrashWhenEmptied() {
    createTestNote("title", "content", 0);
    List<Note> notes = dbHelper.getAllNotes(false);
    trashNotes(notes, true);

    navigateTo(1);

    trashNotes(notes, false);

    onView(allOf(withId(R.id.empty_list), withText(R.string.no_items_in_list),
        withParent(withParent(IsInstanceOf.instanceOf(android.widget.FrameLayout.class))),
        isDisplayed())).check(matches(isDisplayed()));
  }

   @Test
   public void mrJingle_displayedOnArchiveWhenEmptiedBySwiping() {
      createTestNote("title", "content", 0);
      List<Note> notes = dbHelper.getAllNotes(false);
      archiveNotes(notes, true);

      navigateTo(1);

      onView(withId(R.id.list)).perform(
              RecyclerViewActions.actionOnItemAtPosition(0, new GeneralSwipeAction(
                      Swipe.SLOW, GeneralLocation.BOTTOM_RIGHT, GeneralLocation.BOTTOM_LEFT,
                      Press.FINGER)));

      onView(allOf(withId(R.id.empty_list), withText(R.string.no_items_in_list),
              withParent(withParent(IsInstanceOf.instanceOf(android.widget.FrameLayout.class))),
              isDisplayed())).check(matches(isDisplayed()));
   }
}

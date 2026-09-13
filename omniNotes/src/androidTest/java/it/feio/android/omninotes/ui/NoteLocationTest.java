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

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import de.greenrobot.event.EventBus;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.async.bus.CategoriesUpdatedEvent;
import it.feio.android.omninotes.async.bus.NotesUpdatedEvent;
import it.feio.android.omninotes.models.Note;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NoteLocationTest extends BaseEspressoTest {

  private static final double LATITUDE = 43.799328;
  private static final double LONGITUDE = 11.171552;
  private static final int NOTE_LIST_UPDATE_DELAY = 1500;

  @Test
  public void coordinatesAreDisplayedWhenReopeningNote() throws InterruptedException {
    createTestNoteWithLocation(null);

    selectNoteInList(0);

    // A note with coordinates but no address saved must still display its position
    onView(allOf(withId(R.id.location), isDisplayed()))
        .check(matches(withText(LATITUDE + ", " + LONGITUDE)));
  }

  @Test
  public void addressIsDisplayedWhenReopeningNote() throws InterruptedException {
    String address = "Via Roma 1, Florence";
    createTestNoteWithLocation(address);

    selectNoteInList(0);

    // A note with a stored address must display it instead of the coordinates
    onView(allOf(withId(R.id.location), isDisplayed()))
        .check(matches(withText(address)));
  }

  @Test
  public void locationIsNotDisplayedWhenNoteHasNoCoordinates() throws InterruptedException {
    createTestNote("Note without location", "A content", 0);

    selectNoteInList(0);

    onView(withId(R.id.location)).check(matches(not(isDisplayed())));
  }

  @Test
  public void locationIsHiddenAfterRemoval() throws InterruptedException {
    createTestNoteWithLocation(null);

    selectNoteInList(0);

    onView(allOf(withId(R.id.location), isDisplayed())).perform(longClick());
    onView(isRoot()).perform(waitId(R.id.md_buttonDefaultPositive, 5000));
    onView(allOf(withId(R.id.md_buttonDefaultPositive), isDisplayed())).perform(click());

    Thread.sleep(NOTE_LIST_UPDATE_DELAY);
    onView(withId(R.id.location)).check(matches(not(isDisplayed())));
  }

  private void createTestNoteWithLocation(String address) throws InterruptedException {
    Note note = createTestNote("Note with location", "A content", 0);
    note.setLatitude(LATITUDE);
    note.setLongitude(LONGITUDE);
    note.setAddress(address);
    dbHelper.updateNote(note, false);
    // Updating again doesn't trigger any refresh event, the list must be notified explicitly
    EventBus.getDefault().post(new NotesUpdatedEvent(Collections.singletonList(note)));
    EventBus.getDefault().post(new CategoriesUpdatedEvent());

    Thread.sleep(NOTE_LIST_UPDATE_DELAY);
  }

}

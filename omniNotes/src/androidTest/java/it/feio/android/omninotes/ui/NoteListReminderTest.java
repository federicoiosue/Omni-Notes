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
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers.Visibility;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.ReminderHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class NoteListReminderTest extends BaseEspressoTest {

  private Note note;

  @Before
  @Override
  public void setUp() {
    note = createTestNote("Reminder note", "", 0);
    super.setUp();
  }

  @After
  public void removeTestReminder() {
    ReminderHelper.removeReminder(OmniNotes.getAppContext(), dbHelper.getNote(note.get_id()));
  }

  @Test
  public void singleNoteReminderRefreshesRow() {
    onView(isRoot()).perform(waitId(R.id.alarmIcon, 5000));
    onView(withId(R.id.alarmIcon)).check(matches(withEffectiveVisibility(Visibility.GONE)));

    onView(withId(R.id.list)).perform(
        RecyclerViewActions.actionOnItemAtPosition(0, longClick()));
    openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
    onView(withText(R.string.add_reminder)).perform(click());
    onView(allOf(withId(R.id.buttonPositive), isDisplayed())).perform(click());

    onView(isRoot()).perform(waitVisible(R.id.alarmIcon, 5000));
    String alarm = dbHelper.getNote(note.get_id()).getAlarm();
    assertNotNull(alarm);
    assertTrue(Long.parseLong(alarm) > System.currentTimeMillis());
  }
}

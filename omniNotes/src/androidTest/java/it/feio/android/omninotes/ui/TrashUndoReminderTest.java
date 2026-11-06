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
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.view.View;
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
import de.greenrobot.event.EventBus;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.async.bus.CategoriesUpdatedEvent;
import it.feio.android.omninotes.utils.ReminderHelper;
import java.util.Calendar;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class TrashUndoReminderTest extends BaseEspressoTest {

  private static final long UI_TIMEOUT = 5000;
  private static final long REMINDER_DELAY = 60_000;
  private static final long POLL_INTERVAL = 50;

  @Test
  public void undoTrashReArmsReminder() {
    var context = OmniNotes.getAppContext();
    Prefs.putBoolean("settings_swipe_to_trash", true);

    var note = createTestNote("Note with reminder", "A content", 0);
    note.setAlarm(Calendar.getInstance().getTimeInMillis() + REMINDER_DELAY);
    dbHelper.updateNote(note, false);
    ReminderHelper.addReminder(context, note);
    EventBus.getDefault().post(new CategoriesUpdatedEvent());

    openDrawer();
    onView(allOf(withId(R.id.title), withText(R.string.title_item_list), isDisplayed()))
        .perform(click());
    onView(isRoot()).perform(waitId(R.id.list, UI_TIMEOUT));
    onView(isRoot()).perform(new WaitForItemCount());
    assertTrue(ReminderHelper.checkReminder(context, note));

    onView(withId(R.id.list)).perform(
        RecyclerViewActions.actionOnItemAtPosition(0, new GeneralSwipeAction(
            Swipe.FAST, GeneralLocation.CENTER, GeneralLocation.CENTER_LEFT, Press.FINGER)));
    onView(isRoot()).perform(waitVisible(R.id.undobar_button, UI_TIMEOUT));

    assertFalse(ReminderHelper.checkReminder(context, note));

    onView(withId(R.id.undobar_button)).perform(click());

    assertTrue(ReminderHelper.checkReminder(context, note));
    assertFalse(dbHelper.getNote(note.get_id()).isTrashed());
  }


  private static final class WaitForItemCount implements ViewAction {

    @Override
    public Matcher<View> getConstraints() {
      return isRoot();
    }

    @Override
    public String getDescription() {
      return "wait until the notes list shows at least one item";
    }

    @Override
    public void perform(UiController uiController, View view) {
      uiController.loopMainThreadUntilIdle();
      long deadline = System.currentTimeMillis() + UI_TIMEOUT;
      while (System.currentTimeMillis() < deadline) {
        RecyclerView list = view.getRootView().findViewById(R.id.list);
        if (list != null && list.getAdapter() != null && list.getAdapter().getItemCount() > 0) {
          return;
        }
        uiController.loopMainThreadForAtLeast(POLL_INTERVAL);
      }
      throw new AssertionError("The notes list stayed empty");
    }
  }

}

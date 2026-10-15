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
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import it.feio.android.omninotes.R;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Regression tests for the custom back navigation, routed through the
 * OnBackPressedDispatcher callbacks since targetSdk 36 no longer dispatches KEYCODE_BACK.
 */
@RunWith(AndroidJUnit4.class)
public class BackNavigationTest extends BaseEspressoTest {

  private static final int NOTE_LIST_UPDATE_DELAY = 1500;

  @Test
  public void backFromListOpensDrawerWhenEnabled() {
    prefs.edit().putBoolean("settings_navdrawer_on_exit", true).apply();

    onView(isRoot()).perform(pressBack());

    onView(isRoot()).perform(waitVisible(R.id.navigation_drawer, 5000));
    onView(withId(R.id.navigation_drawer)).check(matches(isDisplayed()));
  }

  @Test
  public void backFromDetailReturnsToList() throws InterruptedException {
    createTestNote("Note with back navigation", "A content", 0);

    Thread.sleep(NOTE_LIST_UPDATE_DELAY);

    selectNoteInList(0);
    onView(withId(R.id.detail_root)).check(matches(isDisplayed()));

    onView(isRoot()).perform(pressBack());

    onView(withId(R.id.list)).check(matches(isDisplayed()));
  }

}

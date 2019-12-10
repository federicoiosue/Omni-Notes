/*
 * Copyright (C) 2013-2019 Federico Iosue (federico@iosue.it)
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

package it.feio.android.omninotes;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.startsWith;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RemindersLifecycleTest {

  @Rule
  public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

  @Test
  public void remindersLifecycle () {
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

    ViewInteraction linearLayout = onView(
        withId(R.id.reminder_layout));
    linearLayout.perform(scrollTo(), click());

    onView(allOf(withId(R.id.buttonPositive), withText("Ok"),
        isDisplayed())).perform(click());

    ViewInteraction textView = onView(withId(R.id.datetime));
    textView.check(matches(withText(startsWith(OmniNotes.getAppContext().getResources().getString(R.string.alarm_set_on)))));
  }

}

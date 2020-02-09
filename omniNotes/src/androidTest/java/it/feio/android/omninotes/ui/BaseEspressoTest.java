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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import androidx.core.view.GravityCompat;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import it.feio.android.omninotes.BaseAndroidTestCase;
import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.R;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;


public class BaseEspressoTest extends BaseAndroidTestCase {

  @Rule
  public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class, false, false);

  static Matcher<View> childAtPosition (
      final Matcher<View> parentMatcher, final int position) {

    return new TypeSafeMatcher<View>() {
      @Override
      public void describeTo (Description description) {
        description.appendText("Child at position " + position + " in parent ");
        parentMatcher.describeTo(description);
      }


      @Override
      public boolean matchesSafely (View view) {
        ViewParent parent = view.getParent();
        return parent instanceof ViewGroup && parentMatcher.matches(parent)
            && view.equals(((ViewGroup) parent).getChildAt(position));
      }
    };
  }

  @Before
  public void setUp () throws Exception {
    activityRule.launchActivity(null);
  }

  void createNote (String title, String content) {
    ViewInteraction viewInteraction = onView(
        Matchers.allOf(ViewMatchers.withId(R.id.fab_expand_menu_button),
            withParent(withId(R.id.fab)),
            isDisplayed()));

    if (activityRule.getActivity().getDrawerLayout().isDrawerOpen(GravityCompat.START)) {
      viewInteraction.perform(click());
    }
    viewInteraction.perform(click());

    onView(allOf(withId(R.id.fab_note),
        withParent(withId(R.id.fab)),
        isDisplayed())).perform(click());

    onView(allOf(withId(R.id.detail_title),
        withParent(allOf(withId(R.id.title_wrapper),
            withParent(withId(R.id.detail_tile_card)))),
        isDisplayed())).perform(click());

    onView(allOf(withId(R.id.detail_title),
        withParent(allOf(withId(R.id.title_wrapper),
            withParent(withId(R.id.detail_tile_card)))),
        isDisplayed())).perform(replaceText(title), closeSoftKeyboard());

    onView(withId(R.id.detail_content)).perform(scrollTo(), replaceText(content), closeSoftKeyboard());

    navigateUp();
  }

  void selectNoteInList (int number) {
    onData(anything())
        .inAdapterView(allOf(withId(R.id.list),
            childAtPosition(
                withClassName(is("android.widget.FrameLayout")),
                0)))
        .atPosition(number).perform(click());
  }

  void navigateUp () {
    onView(allOf(childAtPosition(allOf(withId(R.id.toolbar),
        childAtPosition(withClassName(is("android.widget.RelativeLayout")), 0)
    ), 0), isDisplayed())).perform(click());
  }

  void navigateUpSettings () {
    onView(allOf(withContentDescription(R.string.abc_action_bar_up_description),
        childAtPosition(allOf(withId(R.id.toolbar),
            childAtPosition(withClassName(is("android.widget.RelativeLayout")), 0)),
            1), isDisplayed())).perform(click());
  }

}

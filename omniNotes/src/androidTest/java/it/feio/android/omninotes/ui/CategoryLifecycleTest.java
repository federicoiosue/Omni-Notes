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
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static java.lang.Thread.sleep;
import static org.hamcrest.Matchers.allOf;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.view.View;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import it.feio.android.omninotes.R;
import java.util.Calendar;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CategoryLifecycleTest extends BaseEspressoTest {

  private String categoryName;

  @Test
  public void addNewCategory () throws InterruptedException {

    categoryName = "Cat_" + Calendar.getInstance().getTimeInMillis();

    onView(Matchers.allOf(ViewMatchers.withId(R.id.fab_expand_menu_button),
        withParent(withId(R.id.fab)),
        isDisplayed())).perform(click());

    onView(allOf(withId(R.id.fab_note),
        withParent(withId(R.id.fab)),
        isDisplayed())).perform(click());

    onView(allOf(withId(R.id.menu_category), withContentDescription(R.string.category), isDisplayed())).perform(
        click());

    onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.add_category), isDisplayed())).perform(
        click());

    sleep(1000);
    onView(withId(R.id.category_title)).perform(replaceText(categoryName), closeSoftKeyboard());

    onView(allOf(withId(R.id.save), withText("Ok"), isDisplayed())).perform(click());

    onView(allOf(withId(R.id.detail_title),
        withParent(allOf(withId(R.id.title_wrapper),
            withParent(withId(R.id.detail_tile_card)))),
        isDisplayed())).perform(click());

    onView(allOf(withId(R.id.detail_title),
        withParent(allOf(withId(R.id.title_wrapper),
            withParent(withId(R.id.detail_tile_card)))),
        isDisplayed())).perform(replaceText("Note with new category"), closeSoftKeyboard());

    onView(allOf(withContentDescription(R.string.drawer_open),
        withParent(withId(R.id.toolbar)),
        isDisplayed())).perform(click());

  }

  @Test
  public void checkCategoryCreation () throws InterruptedException {

    addNewCategory();

    onView(allOf(withContentDescription(R.string.drawer_open),
        withParent(withId(R.id.toolbar)),
        isDisplayed())).perform(click());

    onView(allOf(withId(R.id.title), withText(categoryName))).check(matches(withText(categoryName)));
  }

  @Test
  public void categoryColorChange () throws InterruptedException {

    addNewCategory();

    onView(allOf(withContentDescription(R.string.drawer_open),
        withParent(withId(R.id.toolbar)))).perform(click());

    onView(allOf(withId(R.id.title), withText(categoryName))).perform(longClick());

    onView(allOf(withId(R.id.color_chooser), isDisplayed())).check(matches(isDisplayed()));

    onView(allOf(withId(R.id.color_chooser), isDisplayed())).perform(click());

    onView(allOf(withId(R.id.md_buttonDefaultNeutral), withText("Custom"),
        childAtPosition(
            allOf(withId(R.id.md_root),
                childAtPosition(
                    withId(android.R.id.content),
                    0)),
            2))).perform(click());

    onView(allOf(withId(R.id.md_buttonDefaultNeutral), withText(R.string.md_presets_label),
        withParent(allOf(withId(R.id.md_root),
            withParent(withId(android.R.id.content)))))).perform(click());

    onView(childAtPosition(
        withId(R.id.md_grid),
        18)).perform(scrollTo(), click());

    onView(childAtPosition(
        withId(R.id.md_grid),
        9)).perform(scrollTo(), click());

    onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.md_done_label),
        childAtPosition(
            allOf(withId(R.id.md_root),
                childAtPosition(
                    withId(android.R.id.content),
                    0)),
            4))).perform(click());

    onView(allOf(withId(R.id.color_chooser), isDisplayed())).check(
        matches(withBackgroundColor(Color.parseColor("#FF263238"))));

  }

  @Test
  public void categoryDeletion () throws InterruptedException {

    addNewCategory();

    onView(allOf(withContentDescription(R.string.drawer_open),
        withParent(withId(R.id.toolbar)),
        isDisplayed())).perform(click());

    onView(allOf(withId(R.id.title), withText(categoryName))).perform(longClick());

    onView(allOf(withId(R.id.delete), withText(R.string.delete), isDisplayed())).perform(click());

    onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.confirm), isDisplayed())).perform(click());

    // Waiting a little to ensure Eventbus post propagation
    try {
      sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    onView(allOf(withId(R.id.title), withText(categoryName))).check(doesNotExist());
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private static Matcher<View> withBackgroundColor (final int backgroundColor) {
    return new TypeSafeMatcher<View>() {

      @Override
      public boolean matchesSafely (View view) {
        ColorFilter cf = new PorterDuffColorFilter(Color.parseColor("#FF263238"), PorterDuff.Mode.SRC_ATOP);
        ColorFilter cf1 = ((AppCompatImageView) view).getDrawable().getColorFilter();
        return cf.equals(cf1);
      }

      @Override
      public void describeTo (Description description) {
        description.appendText("with background color from ID: " + backgroundColor);
      }
    };
  }
}

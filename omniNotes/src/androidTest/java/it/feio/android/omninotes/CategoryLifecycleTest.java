/*
 * Copyright (C) 2018 Federico Iosue (federico.iosue@gmail.com)
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


import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.support.test.espresso.ViewInteraction;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.AppCompatImageView;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CategoryLifecycleTest extends BaseEspressoTest {

    private String categoryName;

    @Test
    public void addNewCategory() {

        categoryName = "Cat_" + Calendar.getInstance().getTimeInMillis();

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

        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.menu_category), withContentDescription(R.string.category), isDisplayed()));
        actionMenuItemView.perform(click());

        ViewInteraction mDButton = onView(
                allOf(withId(R.id.buttonDefaultPositive), withText(R.string.add_category), isDisplayed()));
        mDButton.perform(click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.category_title), isDisplayed()));
        appCompatEditText.perform(replaceText(categoryName), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.save), withText("Ok"), isDisplayed()));
        appCompatButton.perform(click());

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
        editText2.perform(replaceText("Note with new category"), closeSoftKeyboard());

        ViewInteraction navigationUp = onView(
                allOf(withContentDescription(R.string.drawer_open),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        navigationUp.perform(click());

    }

    @Test
    public void checkCategoryCreation() {

        addNewCategory();

        ViewInteraction drawerToggle = onView(
                allOf(withContentDescription(R.string.drawer_open),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        drawerToggle.perform(click());

        ViewInteraction textView = onView(allOf(withId(R.id.title), withText(categoryName)));
        textView.check(matches(withText(categoryName)));
    }

    @Test
    public void categoryColorChange() {

        addNewCategory();

        ViewInteraction drawerToggle = onView(
                allOf(withContentDescription(R.string.drawer_open),
                        withParent(withId(R.id.toolbar))));
        drawerToggle.perform(click());

        ViewInteraction categoryView = onView(allOf(withId(R.id.title), withText(categoryName)));
        categoryView.perform(longClick());

        ViewInteraction imageView = onView(allOf(withId(R.id.color_chooser), isDisplayed()));
        imageView.check(matches(isDisplayed()));

        ViewInteraction appCompatImageView = onView(allOf(withId(R.id.color_chooser), isDisplayed()));
        appCompatImageView.perform(click());

        ViewInteraction mDButton2 = onView(
                allOf(withId(R.id.buttonDefaultNeutral), withText("Custom"),
                        withParent(allOf(withId(R.id.root),
                                withParent(withId(android.R.id.content)))),
                        isDisplayed()));
        mDButton2.perform(click());

        ViewInteraction mDButton3 = onView(
                allOf(withId(R.id.buttonDefaultNeutral), withText(R.string.md_presets_label),
                        withParent(allOf(withId(R.id.root),
                                withParent(withId(android.R.id.content)))),
                        isDisplayed()));
        mDButton3.perform(click());

        ViewInteraction circleView = onView(
                childAtPosition(
                        withId(R.id.grid),
                        18));
        circleView.perform(scrollTo(), click());

        ViewInteraction circleView2 = onView(
                childAtPosition(
                        withId(R.id.grid),
                        9));
        circleView2.perform(scrollTo(), click());

        ViewInteraction mDButton4 = onView(
                allOf(withId(R.id.buttonDefaultPositive), withText(R.string.md_done_label),
                        withParent(allOf(withId(R.id.root),
                                withParent(withId(android.R.id.content)))),
                        isDisplayed()));
        mDButton4.perform(click());

        ViewInteraction appCompatImageViewColorChanged = onView(allOf(withId(R.id.color_chooser), isDisplayed()));
        appCompatImageViewColorChanged.check(matches(withBackgroundColor(Color.parseColor("#FF263238"))));

    }

    @Test
    public void categoryDeletion() {

        addNewCategory();

        ViewInteraction drawerToggle = onView(
                allOf(withContentDescription(R.string.drawer_open),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        drawerToggle.perform(click());

        ViewInteraction categoryView = onView(allOf(withId(R.id.title), withText(categoryName)));
        categoryView.perform(longClick());

        ViewInteraction deleteBtn = onView(
                allOf(withId(R.id.delete), withText(R.string.delete), isDisplayed()));
        deleteBtn.perform(click());

        ViewInteraction deleteConfirmBtn = onView(
                allOf(withId(R.id.buttonDefaultPositive), withText(R.string.confirm), isDisplayed()));
        deleteConfirmBtn.perform(click());

        // Waiting a little to ensure Eventbus post propagation
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction categoryDeletedView = onView(allOf(withId(R.id.title), withText(categoryName)));
        categoryDeletedView.check(doesNotExist());

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Matcher<View> withBackgroundColor(final int backgroundColor) {
        return new TypeSafeMatcher<View>() {

            @Override
            public boolean matchesSafely(View view) {
                ColorFilter cf = new PorterDuffColorFilter(Color.parseColor("#FF263238"), PorterDuff.Mode.SRC_ATOP);
                ColorFilter cf1 = ((AppCompatImageView) view).getDrawable().getColorFilter();
                return cf.equals(cf1);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with background color from id: " + backgroundColor);
            }
        };
    }
}

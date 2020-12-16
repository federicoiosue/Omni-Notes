package it.feio.android.omninotes.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static it.feio.android.omninotes.utils.IsEqualTrimmingAndIgnoringCase.equalToTrimmingAndIgnoringCase;
import static it.feio.android.omninotes.utils.VisibleViewMatcher.isVisible;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;

import android.os.SystemClock;
import android.view.View;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import it.feio.android.omninotes.R;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SettingsActivityTest extends BaseEspressoTest {

  @Test
  public void settingsActivityTest() {
    onView(allOf(
        withContentDescription(equalToTrimmingAndIgnoringCase("drawer open")),
        isVisible(),
        isDescendantOfA(
            allOf(withId(R.id.toolbar), isDescendantOfA(withId(R.id.drawer_layout))))))
        .perform(getClickAction());

    onView(isRoot()).perform(getSwipeAction(540, 897, 540, 0));

    waitToScrollEnd();

    onView(allOf(
        withId(R.id.settings_view),
        isVisible(),
        hasDescendant(
            allOf(
                withId(R.id.settings),
                withTextOrHint(equalToTrimmingAndIgnoringCase("SETTINGS")))),
        isDescendantOfA(
            allOf(
                withId(R.id.left_drawer),
                isDescendantOfA(
                    allOf(
                        withId(R.id.navigation_drawer),
                        isDescendantOfA(withId(R.id.drawer_layout))))))))
        .perform(getClickAction());

    onView(isRoot()).perform(getSwipeAction(540, 1002, 540, 1302));

    waitToScrollEnd();
  }

  @Test
  public void settingsMenuItemsCheck() {
    openDrawer();

    onView(allOf(
        withId(R.id.settings_view),
        isVisible(),
        hasDescendant(
            allOf(
                withId(R.id.settings),
                withTextOrHint(equalToTrimmingAndIgnoringCase("SETTINGS")))),
        isDescendantOfA(
            allOf(
                withId(R.id.left_drawer),
                isDescendantOfA(
                    allOf(
                        withId(R.id.navigation_drawer),
                        isDescendantOfA(withId(R.id.drawer_layout))))))))
        .perform(getClickAction());

    onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.scrollToPosition(2));
    onView(allOf(withId(android.R.id.title), withText(R.string.settings_screen_data)))
        .check(matches(isDisplayed()));
    onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.scrollToPosition(3));
    onView(allOf(withId(android.R.id.title), withText(R.string.settings_screen_interface))).check(
        matches(isDisplayed()));
    onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.scrollToPosition(4));
    onView(allOf(withId(android.R.id.title), withText(R.string.settings_screen_navigation))).check(
        matches(isDisplayed()));
    onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.scrollToPosition(5));
    onView(allOf(withId(android.R.id.title), withText(R.string.settings_screen_behaviors))).check(
        matches(isDisplayed()));
    onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.scrollToPosition(6));
    onView(allOf(withId(android.R.id.title), withText(R.string.settings_screen_notifications)))
        .check(
            matches(isDisplayed()));
    onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.scrollToPosition(7));
    onView(allOf(withId(android.R.id.title), withText(R.string.settings_screen_privacy)))
        .check(matches(isDisplayed()));

    onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.scrollToPosition(9));
    onView(allOf(withId(android.R.id.title), withText(R.string.settings_beta)))
        .check(matches(isDisplayed()));

    onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.scrollToPosition(11));
    onView(allOf(withId(android.R.id.title), withText(R.string.online_manual)))
        .check(matches(isDisplayed()));
    onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.scrollToPosition(12));
    onView(allOf(withId(android.R.id.title), withText(R.string.settings_tour_show_again)))
        .check(matches(isDisplayed()));

    onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.scrollToPosition(14));
    onView(allOf(withId(android.R.id.title), withText(R.string.settings_changelog)))
        .check(matches(isDisplayed()));
    onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.scrollToPosition(15));
    onView(allOf(withId(android.R.id.title), withText(R.string.settings_statistics)))
        .check(matches(isDisplayed()));
    onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.scrollToPosition(16));
    onView(allOf(withId(android.R.id.title), withText(R.string.info)))
        .check(matches(isDisplayed()));
  }

  private static Matcher<View> withTextOrHint(final Matcher<String> stringMatcher) {
    return anyOf(withText(stringMatcher), withHint(stringMatcher));
  }

  private void waitToScrollEnd() {
    SystemClock.sleep(500);
  }

}

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
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import android.view.View;
import android.widget.ListView;
import androidx.core.view.GravityCompat;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.utils.Navigation;
import java.util.function.BooleanSupplier;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DrawerMenusEspressoTest extends BaseEspressoTest {

  private static final long WAIT_TIMEOUT = 5000;
  private static final long POLL_INTERVAL = 50;

  @Test
  public void mainMenuLoaded() {
    openDrawer();

    onView(allOf(withId(R.id.title), withText(R.string.title_item_list)))
        .check(matches(isDisplayed()));
  }

  @Test
  public void categoriesMenuLoaded() {
    String categoryName = "test category";
    createCategory(categoryName);

    openDrawer();

    onView(allOf(withId(R.id.title), withText(categoryName)))
        .check(matches(isDisplayed()));
  }

  @Test
  public void clickingActiveMainMenuClosesDrawer() {
    navigateTo(Navigation.NOTES);
    waitDrawerClosed();

    assertFalse("Tapping the active menu item should close the navigation drawer",
        activityRule.getActivity().getDrawerLayout().isDrawerOpen(GravityCompat.START));
  }

  @Test
  public void clickingActiveCategoryClosesDrawer() {
    String categoryName = "test category";
    createCategory(categoryName);
    ListView categoriesList = (ListView) activityRule.getActivity()
        .findViewById(R.id.drawer_tag_list);

    openDrawer();
    waitUntil("categories drawer list is populated",
        () -> categoriesList.getAdapter() != null && categoriesList.getAdapter().getCount() > 0);
    clickCategoryAtPosition(categoriesList, 0);
    waitDrawerClosed();
    assertEquals(categoryName, activityRule.getActivity().getSupportActionBar().getTitle());

    openDrawer();
    clickCategoryAtPosition(categoriesList, 0);
    waitDrawerClosed();
    assertEquals(categoryName, activityRule.getActivity().getSupportActionBar().getTitle());

    assertFalse("Tapping the active category should close the navigation drawer",
        activityRule.getActivity().getDrawerLayout().isDrawerOpen(GravityCompat.START));
  }

  private void clickCategoryAtPosition(ListView categoriesList, int position) {
    InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
        categoriesList.performItemClick(categoriesList.getChildAt(position), position,
            categoriesList.getAdapter().getItemId(position)));
  }

  private void waitDrawerClosed() {
    waitUntil("navigation drawer is closed",
        () -> !activityRule.getActivity().getDrawerLayout()
            .isDrawerOpen(GravityCompat.START));
  }

  private void waitUntil(String description, BooleanSupplier condition) {
    onView(isRoot()).perform(new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isRoot();
      }

      @Override
      public String getDescription() {
        return "wait until " + description;
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();
        long deadline = System.currentTimeMillis() + WAIT_TIMEOUT;
        while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
          uiController.loopMainThreadForAtLeast(POLL_INTERVAL);
        }
      }
    });
  }

}

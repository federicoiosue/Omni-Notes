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
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static it.feio.android.omninotes.utils.ConstantsBase.INTENT_CATEGORY;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;

import android.content.Intent;
import android.graphics.Color;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import it.feio.android.omninotes.CategoryActivity;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.testutils.BaseAndroidTestCase;
import java.util.Calendar;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class CategoryColorPersistenceTest extends BaseAndroidTestCase {

  private static final int ORIGINAL_COLOR = Color.parseColor("#FFF44336");

  @Rule
  public ActivityTestRule<CategoryActivity> categoryActivityRule = new ActivityTestRule<>(
      CategoryActivity.class, false, false);

  @Test
  public void browsingColorTabsWithoutSelectionKeepsCategoryColor() {
    var category = createColoredCategory();

    openColorChooser(category);

    onView(allOf(withId(R.id.md_buttonDefaultNeutral), withText("Custom"),
        BaseEspressoTest.childAtPosition(
            allOf(withId(R.id.md_root),
                BaseEspressoTest.childAtPosition(
                    withId(android.R.id.content),
                    0)),
            2))).perform(click());

    onView(allOf(withId(R.id.md_buttonDefaultNeutral), withText(R.string.md_presets_label),
        withParent(allOf(withId(R.id.md_root),
            withParent(withId(android.R.id.content)))))).perform(click());

    onView(allOf(withId(R.id.md_buttonDefaultPositive), withText(R.string.md_done_label),
        BaseEspressoTest.childAtPosition(
            allOf(withId(R.id.md_root),
                BaseEspressoTest.childAtPosition(
                    withId(android.R.id.content),
                    0)),
            4))).perform(click());

    onView(allOf(withId(R.id.save), withText("Ok"), isDisplayed())).perform(click());

    assertEquals("Browsing the color picker without picking a color must keep the stored color",
        String.valueOf(ORIGINAL_COLOR), storedColor(category));
  }

  private Category createColoredCategory() {
    var category = new Category();
    category.setName("Cat_" + Calendar.getInstance().getTimeInMillis());
    category.setDescription("testing category");
    category.setColor(String.valueOf(ORIGINAL_COLOR));
    return dbHelper.updateCategory(category);
  }

  private void openColorChooser(Category category) {
    var intent = new Intent();
    intent.putExtra(INTENT_CATEGORY, category);
    categoryActivityRule.launchActivity(intent);

    onView(allOf(withId(R.id.color_chooser), isDisplayed())).perform(click());
  }

  private String storedColor(Category category) {
    return dbHelper.getCategories().stream()
        .filter(stored -> category.getId().equals(stored.getId()))
        .map(Category::getColor)
        .findFirst()
        .orElseThrow(() -> new AssertionError("Category not found"));
  }

}

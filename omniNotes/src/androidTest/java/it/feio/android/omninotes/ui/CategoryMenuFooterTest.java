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
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.widget.ListAdapter;
import android.widget.ListView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import it.feio.android.omninotes.R;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * The categories drawer list appends a "Settings" footer, so the ListView wraps the adapter in a
 * HeaderViewListAdapter whose getItem() returns null for the footer position. The item click
 * handler dereferenced that null and crashed (issue #1012)
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class CategoryMenuFooterTest extends BaseEspressoTest {

  @Test
  public void clickingSettingsFooterDoesNotCrash() {
    createCategory("test category");
    openDrawer();

    ListView list = (ListView) activityRule.getActivity().findViewById(R.id.drawer_tag_list);
    ListAdapter adapter = list.getAdapter();
    assertNotNull("The categories drawer list must have an adapter", adapter);
    int footerPosition = adapter.getCount() - 1;
    assertTrue("The categories drawer list must contain the settings footer",
        list.getFooterViewsCount() > 0);
    assertNull("The last position must be the footer, not a category",
        adapter.getItem(footerPosition));

    InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
        list.performItemClick(list.getChildAt(footerPosition), footerPosition,
            adapter.getItemId(footerPosition)));

    onView(allOf(withId(R.id.title), withText("test category"))).check(matches(isDisplayed()));
  }
}

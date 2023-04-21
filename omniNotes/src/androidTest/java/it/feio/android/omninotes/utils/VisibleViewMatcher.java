/*
 * Copyright (C) 2013-2023 Federico Iosue (federico@iosue.it)
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

package it.feio.android.omninotes.utils;

import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;

import android.view.View;
import androidx.test.espresso.matcher.ViewMatchers.Visibility;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Custom ViewMatcher to match a view that has a "Visible" state but that is not necessarily
 * displayed to the user.
 * <p>
 * Specifically, it matches with views that have "Visible" visibility and positive height and width.
 * A typical example is when a long form has a visible view at the bottom, but the UI needs to be
 * scrolled to reach it.
 */
public final class VisibleViewMatcher extends TypeSafeMatcher<View> {

  public VisibleViewMatcher() {
    super(View.class);
  }

  public static VisibleViewMatcher isVisible() {
    return new VisibleViewMatcher();
  }

  @Override
  protected boolean matchesSafely(View target) {
    return withEffectiveVisibility(Visibility.VISIBLE).matches(target) &&
        target.getWidth() > 0 && target.getHeight() > 0;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText(
        "view has effective visibility VISIBLE and has width and height greater than zero");
  }
}
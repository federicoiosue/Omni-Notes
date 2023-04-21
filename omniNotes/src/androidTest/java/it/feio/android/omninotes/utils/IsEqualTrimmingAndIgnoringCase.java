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

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * Custom BaseMatcher to match strings ignoring case as well as leading and trailing spaces
 */
public class IsEqualTrimmingAndIgnoringCase extends BaseMatcher<String> {

  private final String string;

  public IsEqualTrimmingAndIgnoringCase(String string) {
    if (string == null) {
      throw new IllegalArgumentException(
          "Non-null value required by IsEqualTrimmingAndIgnoringCase()");
    }
    this.string = string;
  }

  public boolean matchesSafely(String item) {
    return string.trim().equalsIgnoreCase(item.trim());
  }

  private void describeMismatchSafely(String item, Description mismatchDescription) {
    mismatchDescription.appendText("was ").appendText(item);
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("equalToTrimmingAndIgnoringCase(")
        .appendValue(string)
        .appendText(")");
  }

  public static IsEqualTrimmingAndIgnoringCase equalToTrimmingAndIgnoringCase(String string) {
    return new IsEqualTrimmingAndIgnoringCase(string);
  }

  @Override
  public boolean matches(Object item) {
    return item != null && matchesSafely(item.toString());
  }

  @Override
  final public void describeMismatch(Object item, Description description) {
    if (item == null) {
      super.describeMismatch(item, description);
    } else {
      describeMismatchSafely(item.toString(), description);
    }
  }
}
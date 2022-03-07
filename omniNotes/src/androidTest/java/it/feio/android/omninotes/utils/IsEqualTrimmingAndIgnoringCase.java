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
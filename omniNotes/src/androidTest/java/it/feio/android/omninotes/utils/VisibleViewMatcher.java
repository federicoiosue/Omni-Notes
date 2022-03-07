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
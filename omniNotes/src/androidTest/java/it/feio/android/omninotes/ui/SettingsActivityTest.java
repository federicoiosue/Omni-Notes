package it.feio.android.omninotes.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
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
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.CoordinatesProvider;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;
import androidx.test.espresso.action.Tap;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.utils.ClickWithoutDisplayConstraint;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SettingsActivityTest {

  @Rule
  public ActivityTestRule<MainActivity> mActivityTestRule =
      new ActivityTestRule<>(MainActivity.class);

  @Test
  public void settingsActivityTest () {
    ViewInteraction android_widget_ImageButton =
        onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.fab_expand_menu_button),
                isVisible(),
                isDescendantOfA(
                    allOf(
                        withId(R.id.fab),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.fragment_container),
                                isDescendantOfA(withId(R.id.drawer_layout))))))));
    android_widget_ImageButton.perform(getLongClickAction());

    ViewInteraction android_widget_LinearLayout =
        onView(
            allOf(
                withId(R.id.reminder_layout),
                isVisible(),
                hasDescendant(withId(R.id.reminder_icon)),
                hasDescendant(
                    allOf(
                        withId(R.id.datetime),
                        withTextOrHint(equalToTrimmingAndIgnoringCase("Add reminder")))),
                isDescendantOfA(
                    allOf(
                        withId(R.id.content_wrapper),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.detail_content_card),
                                isDescendantOfA(
                                    allOf(
                                        withId(R.id.detail_wrapper),
                                        isDescendantOfA(
                                            allOf(
                                                withId(R.id.detail_root),
                                                isDescendantOfA(
                                                    allOf(
                                                        withId(R.id.fragment_container),
                                                        isDescendantOfA(
                                                            withId(R.id.drawer_layout))))))))))))));
    android_widget_LinearLayout.perform(getLongClickAction());

    Espresso.pressBackUnconditionally();

    ViewInteraction root = onView(isRoot());
    root.perform(getSwipeAction(540, 897, 540, 1794));

    waitToScrollEnd();

    ViewInteraction android_widget_ImageView =
        onView(
            allOf(
                withContentDescription(equalToTrimmingAndIgnoringCase("More options")),
                isVisible(),
                isDescendantOfA(
                    allOf(withId(R.id.toolbar), isDescendantOfA(withId(R.id.drawer_layout))))));
    android_widget_ImageView.perform(getLongClickAction());

    ViewInteraction android_widget_EditText =
        onView(
            allOf(
                withId(R.id.detail_content),
                withTextOrHint(equalToTrimmingAndIgnoringCase("Content")),
                isVisible(),
                isDescendantOfA(
                    allOf(
                        withId(R.id.content_wrapper),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.detail_content_card),
                                isDescendantOfA(
                                    allOf(
                                        withId(R.id.detail_wrapper),
                                        isDescendantOfA(
                                            allOf(
                                                withId(R.id.detail_root),
                                                isDescendantOfA(
                                                    allOf(
                                                        withId(R.id.fragment_container),
                                                        isDescendantOfA(
                                                            withId(R.id.drawer_layout))))))))))))));
    android_widget_EditText.perform(replaceText("cracky"));

    ViewInteraction android_widget_LinearLayout2 =
        onView(
            allOf(
                withId(R.id.reminder_layout),
                isVisible(),
                hasDescendant(withId(R.id.reminder_icon)),
                hasDescendant(
                    allOf(
                        withId(R.id.datetime),
                        withTextOrHint(equalToTrimmingAndIgnoringCase("Add reminder")))),
                isDescendantOfA(
                    allOf(
                        withId(R.id.content_wrapper),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.detail_content_card),
                                isDescendantOfA(
                                    allOf(
                                        withId(R.id.detail_wrapper),
                                        isDescendantOfA(
                                            allOf(
                                                withId(R.id.detail_root),
                                                isDescendantOfA(
                                                    allOf(
                                                        withId(R.id.fragment_container),
                                                        isDescendantOfA(
                                                            withId(R.id.drawer_layout))))))))))))));
    android_widget_LinearLayout2.perform(getClickAction());

    ViewInteraction android_widget_Button =
        onView(
            allOf(
                withId(R.id.buttonNegative),
                withTextOrHint(equalToTrimmingAndIgnoringCase("CANCEL")),
                isVisible(),
                isDescendantOfA(
                    allOf(
                        withId(R.id.button_layout),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.llMainContentHolder),
                                isDescendantOfA(withId(R.id.sublime_picker))))))));
    android_widget_Button.perform(getClickAction());

    ViewInteraction root2 = onView(isRoot());
    root2.perform(getSwipeAction(540, 897, 540, 1794));

    waitToScrollEnd();

    onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_ENTER));

    ViewInteraction android_widget_ImageButton2 =
        onView(
            allOf(
                withContentDescription(equalToTrimmingAndIgnoringCase("drawer open")),
                isVisible(),
                isDescendantOfA(
                    allOf(withId(R.id.toolbar), isDescendantOfA(withId(R.id.drawer_layout))))));
    android_widget_ImageButton2.perform(getClickAction());

    ViewInteraction android_widget_FrameLayout =
        onView(
            allOf(
                withId(R.id.root),
                isVisible(),
                hasDescendant(
                    allOf(
                        withId(R.id.card_layout),
                        hasDescendant(withId(R.id.category_marker)),
                        hasDescendant(withId(R.id.note_title)))),
                isDescendantOfA(
                    allOf(
                        withId(R.id.list),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.list_root),
                                isDescendantOfA(
                                    allOf(
                                        withId(R.id.fragment_container),
                                        isDescendantOfA(withId(R.id.drawer_layout))))))))));
    android_widget_FrameLayout.perform(getLongClickAction());

    ViewInteraction android_widget_FrameLayout2 =
        onView(
            allOf(
                withId(R.id.root),
                isVisible(),
                hasDescendant(
                    allOf(
                        withId(R.id.card_layout),
                        hasDescendant(withId(R.id.category_marker)),
                        hasDescendant(withId(R.id.note_title)))),
                isDescendantOfA(
                    allOf(
                        withId(R.id.list),
                        isDescendantOfA(
                            allOf(
                                withId(R.id.list_root),
                                isDescendantOfA(
                                    allOf(
                                        withId(R.id.fragment_container),
                                        isDescendantOfA(withId(R.id.drawer_layout))))))))));
    android_widget_FrameLayout2.perform(getClickAction());

    ViewInteraction android_widget_ImageButton3 =
        onView(
            allOf(
                withContentDescription(equalToTrimmingAndIgnoringCase("drawer open")),
                isVisible(),
                isDescendantOfA(
                    allOf(withId(R.id.toolbar), isDescendantOfA(withId(R.id.drawer_layout))))));
    android_widget_ImageButton3.perform(getClickAction());

    ViewInteraction root3 = onView(isRoot());
    root3.perform(getSwipeAction(540, 897, 540, 0));

    waitToScrollEnd();

    ViewInteraction root4 = onView(isRoot());
    root4.perform(getSwipeAction(540, 897, 540, 0));

    waitToScrollEnd();

    ViewInteraction android_widget_LinearLayout3 =
        onView(
            allOf(
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
                                isDescendantOfA(withId(R.id.drawer_layout))))))));
    android_widget_LinearLayout3.perform(getClickAction());

    ViewInteraction root5 = onView(isRoot());
    root5.perform(getSwipeAction(540, 1002, 540, 1302));

    waitToScrollEnd();
  }

  private static Matcher<View> withTextOrHint (final Matcher<String> stringMatcher) {
    return anyOf(withText(stringMatcher), withHint(stringMatcher));
  }

  private ViewAction getSwipeAction (
      final int fromX, final int fromY, final int toX, final int toY) {
    return ViewActions.actionWithAssertions(
        new GeneralSwipeAction(
            Swipe.SLOW,
            new CoordinatesProvider() {
              @Override
              public float[] calculateCoordinates (View view) {
                float[] coordinates = {fromX, fromY};
                return coordinates;
              }
            },
            new CoordinatesProvider() {
              @Override
              public float[] calculateCoordinates (View view) {
                float[] coordinates = {toX, toY};
                return coordinates;
              }
            },
            Press.FINGER));
  }

  private void waitToScrollEnd () {
    SystemClock.sleep(500);
  }

  private ClickWithoutDisplayConstraint getClickAction () {
    return new ClickWithoutDisplayConstraint(
        Tap.SINGLE,
        GeneralLocation.VISIBLE_CENTER,
        Press.FINGER,
        InputDevice.SOURCE_UNKNOWN,
        MotionEvent.BUTTON_PRIMARY);
  }

  private ClickWithoutDisplayConstraint getLongClickAction () {
    return new ClickWithoutDisplayConstraint(
        Tap.LONG,
        GeneralLocation.CENTER,
        Press.FINGER,
        InputDevice.SOURCE_UNKNOWN,
        MotionEvent.BUTTON_PRIMARY);
  }
}

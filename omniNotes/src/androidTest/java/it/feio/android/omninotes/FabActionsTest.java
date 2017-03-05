package it.feio.android.omninotes;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import it.feio.android.omninotes.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class FabActionsTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void fabActionsTest() {
         // Added a sleep statement to match the app's execution delay.
 // The recommended way to handle such scenarios is to use Espresso idling resources:
  // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
try {
 Thread.sleep(10000);
 } catch (InterruptedException e) {
 e.printStackTrace();
 }
        
        ViewInteraction viewInteraction = onView(
allOf(withId(R.id.fab_expand_menu_button),
withParent(withId(R.id.fab)),
isDisplayed()));
        viewInteraction.perform(click());
        
         // Added a sleep statement to match the app's execution delay.
 // The recommended way to handle such scenarios is to use Espresso idling resources:
  // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
try {
 Thread.sleep(20000);
 } catch (InterruptedException e) {
 e.printStackTrace();
 }
        
        ViewInteraction imageButton = onView(
allOf(withId(R.id.fab_checklist),
childAtPosition(
allOf(withId(R.id.fab),
childAtPosition(
IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class),
4)),
3),
isDisplayed()));
        imageButton.check(matches(isDisplayed()));
        
        ViewInteraction imageButton2 = onView(
allOf(withId(R.id.fab_camera),
childAtPosition(
allOf(withId(R.id.fab),
childAtPosition(
IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class),
4)),
1),
isDisplayed()));
        imageButton2.check(matches(isDisplayed()));
        
        }

        private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup)parent).getChildAt(position));
            }
        };
    }
    }

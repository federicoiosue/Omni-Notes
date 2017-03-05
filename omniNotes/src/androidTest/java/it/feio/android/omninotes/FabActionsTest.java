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

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;


@LargeTest
@RunWith(AndroidJUnit4.class)
public class FabActionsTest {

	@Rule
	public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);


	@Test
	public void fabActionsTest() {

		ViewInteraction viewInteraction = onView(
				allOf(withId(R.id.fab_expand_menu_button),
						withParent(withId(R.id.fab)),
						isDisplayed()));
		viewInteraction.perform(click());

		ViewInteraction checklistFabAction = onView(
				allOf(withId(R.id.fab_checklist),
						childAtPosition(
								allOf(withId(R.id.fab),
										childAtPosition(
												IsInstanceOf.instanceOf(android.widget.FrameLayout.class),
												4)),
								3),
						isDisplayed()));
//		imageButton.check(matches(isDisplayed()));
		assertNotNull(checklistFabAction);

		ViewInteraction cameraFabAction = onView(
				allOf(withId(R.id.fab_camera),
						childAtPosition(
								allOf(withId(R.id.fab),
										childAtPosition(
												IsInstanceOf.instanceOf(android.widget.FrameLayout.class),
												4)),
								1),
						isDisplayed()));
//		imageButton2.check(matches(isDisplayed()));
		assertNotNull(cameraFabAction);

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
						&& view.equals(((ViewGroup) parent).getChildAt(position));
			}
		};
	}
}

package it.feio.android.omninotes.ui;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import it.feio.android.omninotes.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CalendarReminderSyncTest extends BaseEspressoTest {

	@Test
	public void syncButtonAppears() throws InterruptedException {
		Thread.sleep(1500);

		onView(Matchers.allOf(ViewMatchers.withId(R.id.fab_expand_menu_button),
				withParent(withId(R.id.fab)),
				isDisplayed())).perform(click());

		onView(allOf(withId(R.id.fab_note),
				withParent(withId(R.id.fab)),
				isDisplayed())).perform(click());

		onView(withId(R.id.reminder_layout)).perform(scrollTo(), click());
		onView(allOf(withId(R.id.buttonPositive), withText("Ok"), isDisplayed())).perform(click());
		onView(withId(R.id.sync_reminder_layout)).check(matches(isDisplayed()));

		Thread.sleep(1500);

	}

	@Test
	public void syncButtonDisappears() throws InterruptedException {
		Thread.sleep(1500);

		onView(Matchers.allOf(ViewMatchers.withId(R.id.fab_expand_menu_button),
				withParent(withId(R.id.fab)),
				isDisplayed())).perform(click());

		onView(allOf(withId(R.id.fab_note),
				withParent(withId(R.id.fab)),
				isDisplayed())).perform(click());

		onView(withId(R.id.reminder_layout)).perform(scrollTo(), click());
		onView(allOf(withId(R.id.buttonPositive), withText("Ok"), isDisplayed())).perform(click());

		onView(withId(R.id.reminder_layout)).perform(scrollTo(), longClick());
		onView(withText("Ok"))
				.inRoot(isDialog())
				.check(matches(isDisplayed()))
				.perform(click());

		onView(withId(R.id.sync_reminder_layout)).check(matches(not(isDisplayed())));

		Thread.sleep(1500);

	}

}

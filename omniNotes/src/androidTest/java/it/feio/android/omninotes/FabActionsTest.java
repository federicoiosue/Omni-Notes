package it.feio.android.omninotes;


import android.support.test.espresso.ViewInteraction;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.Matchers.allOf;


@LargeTest
@RunWith(AndroidJUnit4.class)
public class FabActionsTest extends BaseEspressoTest {

    @Test
    public void fabActionsTest() throws IOException {

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
        assertNotNull(cameraFabAction);

    }

}

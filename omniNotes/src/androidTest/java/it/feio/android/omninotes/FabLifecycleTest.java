/*
 * Copyright (C) 2018 Federico Iosue (federico.iosue@gmail.com)
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
import static org.hamcrest.Matchers.allOf;


@LargeTest
@RunWith(AndroidJUnit4.class)
public class FabLifecycleTest extends BaseEspressoTest {

    @Test
    public void fabOpenCloseTest() {

        ViewInteraction viewInteraction = onView(
                allOf(withId(R.id.fab_expand_menu_button),
                        withParent(withId(R.id.fab))));
        viewInteraction.perform(click());


        ViewInteraction viewInteraction2 = onView(
                allOf(withId(R.id.fab_expand_menu_button),
                        withParent(withId(R.id.fab))));
        viewInteraction2.perform(click());
    }
    
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

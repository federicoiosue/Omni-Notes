/*
 * Copyright (C) 2016 Federico Iosue (federico.iosue@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundatibehaon, either version 3 of the License, or
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

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by AchrafAmil on 05/04/2018.
 */

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AddAndEditNoteTest extends BaseEspressoTest {

    @Test
    public void AddNote(){
        onView(allOf(withId(R.id.fab_expand_menu_button),
                withParent(withId(R.id.fab)),
                isDisplayed()))
                .perform(click());

        onView(allOf(withId(R.id.fab_note),
                withParent(withId(R.id.fab)),
                isDisplayed()))
                .perform(click());

        onView(withId(R.id.detail_title))
                .perform(typeText("Foobar title"));

        onView(withId(R.id.detail_content))
                .perform(typeText("Lorem Ipsum"));

        onView(withId(R.id.done_fab))
                .perform(click());

        onView(withText("Foobar title"))
                .check(matches(isDisplayed()));

    }

}

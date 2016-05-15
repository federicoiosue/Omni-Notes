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

import android.test.ActivityInstrumentationTestCase2;
import com.neopixl.pixlui.components.textview.TextView;
import com.robotium.solo.Solo;
import com.robotium.solo.Timeout;
import it.feio.android.omninotes.MainActivity;


public class NoteWithReminderAndSketch extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;


    public NoteWithReminderAndSketch() {
        super(MainActivity.class);
    }


    public void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation());
        getActivity();
    }


    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }


    public void testRun() {
        //Wait for activity: 'it.feio.android.omninotes.MainActivity'
        solo.waitForActivity(it.feio.android.omninotes.MainActivity.class, 4000);
        //Set default small timeout to 30522 milliseconds
        Timeout.setSmallTimeout(30522);
        //Click on ImageView
        solo.clickLongOnView(solo.getView(it.feio.android.omninotes.R.id.fab_expand_menu_button));
        //Click on LinearLayout
        solo.clickOnView(solo.getView(it.feio.android.omninotes.R.id.reminder_layout));
        // Check that no reminder is set
        assertEquals(((TextView) solo.getView(it.feio.android.omninotes.R.id.datetime)).getText().toString().length()
                , 0);
        //Wait for dialog
        solo.waitForDialogToOpen(5000);
        //Click on Done
        solo.clickOnView(solo.getView(it.feio.android.omninotes.R.id.done));
        //Wait for dialog
        solo.waitForDialogToOpen(5000);
        //Click on Done
        solo.clickOnView(solo.getView(it.feio.android.omninotes.R.id.done_button));
        //Wait for dialog
        solo.waitForDialogToOpen(5000);
        //Click on Done in recurrent reminders fragment
        solo.clickOnView(solo.getView(it.feio.android.omninotes.R.id.done));
        // Check if a reminder is present
        assertNotNull(((TextView) solo.getView(it.feio.android.omninotes.R.id.datetime)).getText().toString());
        //Click on Empty Text View
        solo.clickOnView(solo.getView(it.feio.android.omninotes.R.id.menu_attachment));
        //Click on Sketch
        solo.clickOnView(solo.getView(it.feio.android.omninotes.R.id.sketch));
        // Draw a line
        solo.drag(222.79372F, 470.5643F, 590.6923F, 1048.4539F, 40);
        //Click on ImageView
        solo.clickOnView(solo.getView(android.widget.ImageButton.class, 0));
        //Wait for thumbnail
        solo.sleep(1000);
        //Click on RelativeLayout
        solo.clickInList(1, 0);
        //Wait for activity: 'it.feio.android.omninotes.GalleryActivity'
        assertTrue("it.feio.android.omninotes.GalleryActivity is not found!", solo.waitForActivity(it.feio.android
                .omninotes.GalleryActivity.class));
        //Click on ImageView
        solo.clickOnView(solo.getView(android.widget.ImageView.class, 0));
        //Click on ImageView
        solo.clickOnView(solo.getView(android.widget.ImageView.class, 0));
        //Click on ImageView
        solo.clickOnView(solo.getView(android.widget.ImageButton.class, 0));
        //Click on ImageView
        solo.clickOnView(solo.getView(android.widget.ImageButton.class, 0));
    }
}

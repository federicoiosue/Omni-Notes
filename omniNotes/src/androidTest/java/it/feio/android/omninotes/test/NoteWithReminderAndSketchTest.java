package it.feio.android.omninotes.test;

import com.neopixl.pixlui.components.textview.TextView;
import com.robotium.solo.Timeout;
import it.feio.android.omninotes.MainActivity;


public class NoteWithReminderAndSketchTest extends BaseRobotiumTest {


    public NoteWithReminderAndSketchTest() {
        super(MainActivity.class);
    }


    public void testNoteWithReminderAndSketch() {
        //Wait for activity: 'it.feio.android.omninotes.MainActivity'
		solo.waitForActivity(MainActivity.class, 2000);
		//Set default small timeout to 30522 milliseconds
        Timeout.setSmallTimeout(30522);
        //Click on ImageView
        solo.clickLongOnView(solo.getView(it.feio.android.omninotes.R.id.fab_expand_menu_button));
		//Click on LinearLayout
		solo.clickOnView(solo.getView(it.feio.android.omninotes.R.id.reminder_layout));
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
		//Click on Done
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

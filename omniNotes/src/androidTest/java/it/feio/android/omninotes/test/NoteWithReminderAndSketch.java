package it.feio.android.omninotes.test;

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
        solo.clickOnView(solo.getView(it.feio.android.omninotes.R.id.fab_expand_menu_button));
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

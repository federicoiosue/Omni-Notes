/*
 * Copyright (C) 2015 Federico Iosue (federico.iosue@gmail.com)
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

package it.feio.android.omninotes.test;

import android.os.AsyncTask;
import android.test.ActivityInstrumentationTestCase2;
import com.neopixl.pixlui.components.edittext.EditText;
import com.robotium.solo.Solo;
import com.robotium.solo.Timeout;
import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Note;

import java.util.Calendar;
import java.util.List;


public class NoteWithTitleAndContent extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;
    private final String NOTE_TITLE = "new note";
    private final String NOTE_CONTENT = "some random content written at timestamp " + Calendar.getInstance()
            .getTimeInMillis();


    public NoteWithTitleAndContent() {
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
        solo.waitForActivity(MainActivity.class, 4000);
        //Set default small timeout to 30522 milliseconds
        Timeout.setSmallTimeout(30522);
        //Click on ImageView
        solo.clickOnView(solo.getView(it.feio.android.omninotes.R.id.fab_expand_menu_button));
        //Click on Empty Text View
        EditText titleEditText = (EditText) solo.getView(it.feio.android.omninotes.R.id.detail_title);
        solo.clickOnView(titleEditText);
        //Enter the title text
        setTextViewContent(titleEditText, NOTE_TITLE);
        //Click on Empty Text View
        EditText contentEditText = (EditText) solo.getView(it.feio.android.omninotes.R.id.detail_content);
        solo.clickOnView(contentEditText);
        //Enter the content text
        setTextViewContent(contentEditText, NOTE_CONTENT);
        //Exit the note
        solo.clickOnView(solo.getView(android.widget.ImageButton.class, 0));
        // Wait a little
        solo.sleep(1500);
        // Retrieve note and check some assertions
        List<Note> retrievedNotes = DbHelper.getInstance(OmniNotes.getAppContext()).getNotesByPattern(NOTE_CONTENT);
        assertEquals(retrievedNotes.size(), 1);
        assertEquals(retrievedNotes.get(0).getTitle(), NOTE_TITLE);
    }


    private void setTextViewContent(final EditText editText, final String text) {
        new TextSetter(editText, text).execute();

    }


    private class TextSetter extends AsyncTask {

        private final String text;
        private final EditText editText;


        public TextSetter(EditText editText, String text) {
            this.editText = editText;
            this.text = text;
        }


        @Override
        protected Object doInBackground(Object[] params) {
            return null;
        }


        @Override
        protected void onPostExecute(Object o) {
//            super.onPostExecute(o);
            editText.setText(text);
        }
    }

}

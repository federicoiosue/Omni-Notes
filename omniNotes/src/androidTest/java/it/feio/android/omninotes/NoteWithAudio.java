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

import it.feio.android.omninotes.MainActivity;
import com.robotium.solo.*;
import android.test.ActivityInstrumentationTestCase2;


public class NoteWithAudio extends ActivityInstrumentationTestCase2<MainActivity> {
  	private Solo solo;
  	
  	public NoteWithAudio() {
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
		solo.waitForActivity(it.feio.android.omninotes.MainActivity.class, 2000);
        //Set default small timeout to 19121 milliseconds
		Timeout.setSmallTimeout(19121);
        //Click on (1028.1432, 1752.0874)
		solo.clickOnScreen(1028.1432F, 1752.0874F);
        //Click on ImageView
		solo.clickLongOnView(solo.getView(it.feio.android.omninotes.R.id.fab_expand_menu_button));
        //Click on Empty Text View
		solo.clickOnView(solo.getView(it.feio.android.omninotes.R.id.menu_attachment));
        //Click on Record
		solo.clickOnView(solo.getView(it.feio.android.omninotes.R.id.recording));
        //Click on Stop
		solo.clickOnView(solo.getView(it.feio.android.omninotes.R.id.recording));
        //Click on 0:04
		solo.clickInList(1, 0);
        //Click on 0:04
		solo.clickInList(1, 0);
        //Click on ImageView
		solo.clickOnView(solo.getView(android.widget.ImageButton.class, 0));
	}
}

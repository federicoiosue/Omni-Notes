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

package it.feio.android.omninotes.test;

import android.app.Instrumentation;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import com.robotium.solo.Solo;
import it.feio.android.omninotes.MainActivity;


public class BaseRobotiumTest extends ActivityInstrumentationTestCase2<MainActivity> {

	protected Solo solo;
	private MainActivity mActivity;
	private Instrumentation.ActivityMonitor monitor;


	public BaseRobotiumTest(Class<MainActivity> activityClass) {
		super(activityClass);
	}


	public void setUp() throws Exception {
		super.setUp();
		solo = new Solo(getInstrumentation(), getActivity());
	}


	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		solo.finishOpenedActivities();
	}


	@Override
	public MainActivity getActivity() {
		if (mActivity == null) {
			Intent intent = new Intent(getInstrumentation().getTargetContext(), MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// register activity that need to be monitored.
			monitor = getInstrumentation().addMonitor(MainActivity.class.getName(), null, false);
			getInstrumentation().getTargetContext().startActivity(intent);
			mActivity = (MainActivity) getInstrumentation().waitForMonitor(monitor);
			setActivity(mActivity);
		}
		return mActivity;
	}
}

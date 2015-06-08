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

package it.feio.android;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;

import it.feio.android.omninotes.MainActivity;

import static org.junit.Assert.assertNotNull;


public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {


    public MainActivityTest() {
        super(MainActivity.class);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @MediumTest
    public void testRemoveExistentDetailFragment() {
        MainActivity mainActivity = getActivity();
        assertNotNull(mainActivity);
        assertSame(MainActivity.class, mainActivity.getClass());
        FragmentManager fm = mainActivity.getSupportFragmentManager();
        Fragment f = new TestFragment("tf1");
        fm.beginTransaction().add(f, "tf1").commit();
    }

    public MainActivity getActivity() {
        if (super.getActivity().getClass().isAssignableFrom(MainActivity.class)) {
            return super.getActivity();
        }
        return null;
    }
}


class TestFragment extends Fragment {
    private String tag;

    public TestFragment(String tag) {
		this.tag = tag;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        assertNotNull(getActivity().getSupportFragmentManager().findFragmentByTag(this.tag));
    }
}
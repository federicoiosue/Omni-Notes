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

package it.feio.android.omninotes.utils;

import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.Suppress;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import it.feio.android.omninotes.OmniNotes;


public class GeocodeHelperTest extends InstrumentationTestCase {

    @Test
	@Suppress
    public void testGetAddressFromCoordinates() throws IOException {
        if (ConnectionManager.internetAvailable(OmniNotes.getAppContext())) {
            Double LAT = 43.799328;
            Double LON = 11.171552;
            String address = GeocodeHelper.getAddressFromCoordinates(OmniNotes.getAppContext(), LAT, LON);
            Assert.assertTrue(address.length() > 0);
        }
    }
}

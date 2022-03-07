/*
 * Copyright (C) 2013-2020 Federico Iosue (federico@iosue.it)
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

package it.feio.android.omninotes.helpers;


import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import io.nlopez.smartlocation.location.LocationProvider;
import it.feio.android.omninotes.BaseAndroidTestCase;
import org.junit.Test;

public class GeocodeProviderBaseFactoryTest extends BaseAndroidTestCase {

  @Test
  public void checkUtilityClassWellDefined() throws Exception {
    assertUtilityClassWellDefined(GeocodeProviderBaseFactory.class, true, true);
  }

  @Test
  public void getProvider() {
    LocationProvider locationProvider = GeocodeProviderBaseFactory.getProvider(testContext);
    assertNotEquals(null, locationProvider);
  }

  @Test
  public void checkHighAccuracyLocationProvider() {
    boolean highAccuracyLocationProviderEnabled = GeocodeProviderBaseFactory
        .checkHighAccuracyLocationProvider(
            testContext);
    assertTrue(highAccuracyLocationProviderEnabled);
  }

}

/*
 * Copyright (C) 2013-2026 Federico Iosue (omninotes@iosue.it)
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

package it.feio.android.omninotes.helpers.location;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import it.feio.android.omninotes.BaseUnitTest;
import org.junit.Test;


public class GeocodeHelperTest extends BaseUnitTest {

  @Test
  public void checkUtilityClassWellDefined() throws Exception {
    assertUtilityClassWellDefined(GeocodeHelper.class);
  }

  @Test
  public void formatAddressUsesFullAddressLineWhenAvailable() {
    assertEquals("Via Roma 1, Firenze",
        GeocodeHelper.formatAddress("Via Roma 1, Firenze", "Via Roma", "Firenze"));
  }

  @Test
  public void formatAddressFallsBackToStreetAndCity() {
    assertEquals("Via Roma, Firenze", GeocodeHelper.formatAddress(null, "Via Roma", "Firenze"));
  }

  @Test
  public void formatAddressHandlesMissingParts() {
    assertEquals("Firenze", GeocodeHelper.formatAddress("", null, "Firenze"));
    assertEquals("Via Roma", GeocodeHelper.formatAddress(" ", "Via Roma", null));
  }

  @Test
  public void formatAddressReturnsEmptyStringWhenNothingIsKnown() {
    assertEquals("", GeocodeHelper.formatAddress(null, null, null));
    assertEquals("", GeocodeHelper.formatAddress("", "", ""));
  }

  @Test
  public void areCoordinatesRecognizesCoordinates() {
    assertTrue(GeocodeHelper.areCoordinates("43.799328, 11.171552"));
    assertTrue(GeocodeHelper.areCoordinates("-43.799328, -11.171552"));
    assertFalse(GeocodeHelper.areCoordinates("Via Roma 1, Firenze"));
  }

}

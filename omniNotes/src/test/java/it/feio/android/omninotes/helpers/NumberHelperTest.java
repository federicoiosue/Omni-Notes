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

package it.feio.android.omninotes.helpers;

import static org.junit.Assert.assertEquals;

import it.feio.android.omninotes.BaseUnitTest;
import org.junit.Test;


public class NumberHelperTest extends BaseUnitTest {

  @Test
  public void checkUtilityClassWellDefined() throws Exception {
    assertUtilityClassWellDefined(NumberHelper.class);
  }

  @Test
  public void megabytesToBytesConvertsValidValues() {
    assertEquals(0L, NumberHelper.megabytesToBytes(null));
    assertEquals(0L, NumberHelper.megabytesToBytes(""));
    assertEquals(0L, NumberHelper.megabytesToBytes("HelloWorld"));
    assertEquals(0L, NumberHelper.megabytesToBytes("-10"));
    assertEquals(10L * 1024L * 1024L, NumberHelper.megabytesToBytes("10"));
  }

  @Test
  public void megabytesToBytesDoesNotOverflow() {
    assertEquals(Long.MAX_VALUE, NumberHelper.megabytesToBytes(String.valueOf(Long.MAX_VALUE)));
  }

}

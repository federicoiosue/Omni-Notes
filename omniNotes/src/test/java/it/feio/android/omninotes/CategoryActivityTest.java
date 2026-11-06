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

package it.feio.android.omninotes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CategoryActivityTest extends BaseUnitTest {

  @Test
  public void shouldNotApplyColorWhenOnlyBrowsing() {
    assertFalse(CategoryActivity.shouldApplyColor(false, -1));
  }

  @Test
  public void shouldApplyColorWhenPresetIsSelected() {
    assertTrue(CategoryActivity.shouldApplyColor(false, 18));
  }

  @Test
  public void shouldApplyColorWhenCustomTabIsVisible() {
    assertTrue(CategoryActivity.shouldApplyColor(true, -1));
  }
}

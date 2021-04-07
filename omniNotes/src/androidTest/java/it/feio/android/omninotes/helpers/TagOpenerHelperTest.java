/*
 * Copyright (C) 2013-2021 Federico Iosue (federico@iosue.it)
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

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import it.feio.android.omninotes.BaseAndroidTestCase;
import it.feio.android.omninotes.exceptions.checked.UnhandledIntentException;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TagOpenerHelperTest extends BaseAndroidTestCase {

  @Test
  public void openOrGetIntent_mailto() throws UnhandledIntentException {
    assertNull(TagOpenerHelper.openOrGetIntent(testContext, "mailto:random@address.com"));
  }

  @Test
  public void openOrGetIntent_tel() throws UnhandledIntentException {
    assertNull(TagOpenerHelper.openOrGetIntent(testContext, "tel:123456"));
  }

  @Test
  public void openOrGetIntent_other() throws UnhandledIntentException {
    assertNotNull(TagOpenerHelper.openOrGetIntent(testContext, "thisCouldBeATag"));
  }

}
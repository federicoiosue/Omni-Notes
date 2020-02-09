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

package it.feio.android.omninotes.utils.date;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import it.feio.android.omninotes.helpers.date.RecurrenceHelper;
import java.util.Calendar;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RecurrenceHelperTest {

  @Test
  public void testGetNoteRecurrentReminderText () {
    long reminder = 1577369824425L;
    String rrule = "FREQ=WEEKLY;WKST=MO;BYDAY=MO,TU,TH";
    String alarmText = RecurrenceHelper.getNoteRecurrentReminderText(reminder, rrule);

    Assert.assertEquals("Weekly on Mon, Tue, Thu starting from Thu, Dec 26, 2019 3:17 PM", alarmText);
  }

}

/*
 * Copyright (C) 2013-2024 Federico Iosue (federico@iosue.it)
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

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import it.feio.android.omninotes.testutils.BaseAndroidTestCase;
import it.feio.android.omninotes.helpers.date.RecurrenceHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

@RunWith(AndroidJUnit4.class)
public class RecurrenceHelperTest extends BaseAndroidTestCase {

  @Test
  public void getNoteRecurrentReminderText() {
    long reminder = 1577369824425L;
    String rrule = "FREQ=WEEKLY;WKST=MO;BYDAY=MO,TU,TH";

    String alarmText = RecurrenceHelper.getNoteRecurrentReminderText(reminder, rrule);
    alarmText = alarmText.replace("\u202F", " ");
    String expectedText = getExpectedText(reminder);
    String errorMsg = String.format("%s not matching %s", alarmText, expectedText);

    assertTrue(errorMsg, alarmText.matches(expectedText));
  }

  private String getExpectedText(long reminder) {
    TimeZone localTimeZone = TimeZone.getDefault();
    Calendar calendar = Calendar.getInstance(localTimeZone);
    calendar.setTimeInMillis(reminder);

    SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd, yyyy hh:mm a", Locale.getDefault());
    sdf.setTimeZone(localTimeZone);
    String formattedDate = sdf.format(calendar.getTime());

    return "Weekly on Mon, Tue, Thu starting from " + formattedDate;
  }

}

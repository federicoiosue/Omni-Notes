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

package it.feio.android.omninotes.utils.date;

import android.test.InstrumentationTestCase;
import it.feio.android.omninotes.helpers.date.DateHelper;
import org.junit.Assert;

import java.util.Calendar;


public class DateHelperTest extends InstrumentationTestCase {

	long TEN_MINUTES = 10 * 60 * 1000;


	public void testNextReminderFromRecurrenceRule() {
		long currentTime = Calendar.getInstance().getTimeInMillis();
		long reminder = Calendar.getInstance().getTimeInMillis() + TEN_MINUTES;

		// Daily test
		String rruleDaily = "FREQ=DAILY;COUNT=30;WKST=MO";
		long nextReminder = DateHelper.nextReminderFromRecurrenceRule(reminder, currentTime, rruleDaily);
		Assert.assertNotEquals(nextReminder, 0);
		Assert.assertEquals((nextReminder - reminder) / 60 / 60 / 1000, 24-1);

		// 3-Daily test
		String rruleDaily2 = "FREQ=DAILY;COUNT=30;INTERVAL=3";
		long nextReminder2 = DateHelper.nextReminderFromRecurrenceRule(reminder, currentTime, rruleDaily2);
		Assert.assertNotEquals(nextReminder2, 0);
		Assert.assertEquals((nextReminder2 - reminder) / 60 / 60 / 1000, 3*24 - 1);


	}

}

/*
 * Copyright (C) 2016 Federico Iosue (federico.iosue@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundatibehaon, either version 3 of the License, or
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

import android.app.PendingIntent;
import android.content.Intent;
import android.test.InstrumentationTestCase;
import android.util.Log;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.ReminderHelper;
import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;


public class ReminderHelperTest extends InstrumentationTestCase {

	@Test
	public void testGetRequestCode() {
		Long now = Calendar.getInstance().getTimeInMillis();
		Note note = new Note();
		note.setAlarm(now);
		int requestCode = ReminderHelper.getRequestCode(note);
		int requestCode2 = ReminderHelper.getRequestCode(note);
		assertEquals(requestCode, requestCode2);
		assertTrue(String.valueOf(now).startsWith(String.valueOf(requestCode)));
	}


	public void testAddReminder() {
		Note note = buildNote();
		ReminderHelper.addReminder(getInstrumentation().getTargetContext(), note);
		boolean reminderActive = ReminderHelper.checkReminder(getInstrumentation().getTargetContext(), note);
		assertTrue(reminderActive);
	}


	public void testRemoveReminder() {
		Note note = buildNote();
		ReminderHelper.addReminder(getInstrumentation().getTargetContext(), note);
		boolean reminderActive = ReminderHelper.checkReminder(getInstrumentation().getTargetContext(), note);
		ReminderHelper.removeReminder(getInstrumentation().getTargetContext(), note);
		boolean reminderRemoved = ReminderHelper.checkReminder(getInstrumentation().getTargetContext(), note);
		assertTrue(reminderActive);
		assertFalse(reminderRemoved);
	}


	private Note buildNote() {
		Long now = Calendar.getInstance().getTimeInMillis();
		Note note = new Note();
		note.setCreation(now);
		note.setAlarm(now);
		return note;
	}
}

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

package it.feio.android.omninotes.utils;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import it.feio.android.omninotes.models.Note;
import java.util.Calendar;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ReminderHelperTest {

  @Test
  public void shouldGetRequestCode () {
    Long now = Calendar.getInstance().getTimeInMillis();
    Note note = new Note();
    note.setAlarm(now);
    int requestCode = ReminderHelper.getRequestCode(note);
    int requestCode2 = ReminderHelper.getRequestCode(note);
    assertEquals(requestCode, requestCode2);
    assertTrue(String.valueOf(now).startsWith(String.valueOf(requestCode)));
  }

  @Test
  public void shouldAddReminder () {
    Note note = buildNote();
    ReminderHelper.addReminder(InstrumentationRegistry.getInstrumentation().getTargetContext(), note);
    boolean reminderActive = ReminderHelper.checkReminder(InstrumentationRegistry.getInstrumentation().getTargetContext(), note);
    assertTrue(reminderActive);
  }

  @Test
  public void shouldNotAddReminderWithPassedTime () {
    Note note = buildNote();
    note.setAlarm(Calendar.getInstance().getTimeInMillis());
    ReminderHelper.addReminder(InstrumentationRegistry.getInstrumentation().getTargetContext(), note);
    boolean reminderActive = ReminderHelper.checkReminder(InstrumentationRegistry.getInstrumentation().getTargetContext(), note);
    assertFalse(reminderActive);
  }

  @Test
  public void shouldRemoveReminder () {
    Note note = buildNote();
    ReminderHelper.addReminder(InstrumentationRegistry.getInstrumentation().getTargetContext(), note);
    boolean reminderActive = ReminderHelper.checkReminder(InstrumentationRegistry.getInstrumentation().getTargetContext(), note);
    ReminderHelper.removeReminder(InstrumentationRegistry.getInstrumentation().getTargetContext(), note);
    boolean reminderRemoved = ReminderHelper.checkReminder(InstrumentationRegistry.getInstrumentation().getTargetContext(), note);
    assertTrue(reminderActive);
    assertFalse(reminderRemoved);
  }

  private Note buildNote () {
    Long now = Calendar.getInstance().getTimeInMillis();
    Note note = new Note();
    note.setCreation(now);
    note.setAlarm(now + 1000);
    return note;
  }
}

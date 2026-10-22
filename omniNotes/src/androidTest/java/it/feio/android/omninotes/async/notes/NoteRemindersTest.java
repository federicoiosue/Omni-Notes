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

package it.feio.android.omninotes.async.notes;

import static it.feio.android.omninotes.utils.ConstantsBase.INTENT_NOTE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Intent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.receiver.AlarmReceiver;
import it.feio.android.omninotes.testutils.BaseAndroidTestCase;
import it.feio.android.omninotes.utils.ParcelableUtil;
import it.feio.android.omninotes.utils.ReminderHelper;
import java.util.Calendar;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Notes with reminders that are trashed or deleted must not leave a pending alarm behind, otherwise
 * a fired reminder would resurrect them (see issues #1031 and #1032)
 */
@RunWith(AndroidJUnit4.class)
public class NoteRemindersTest extends BaseAndroidTestCase {

  @Test
  public void trashingNoteCancelsItsReminder() {
    var note = createRemindedNote("Trash rearm");
    var context = OmniNotes.getAppContext();
    assertTrue(ReminderHelper.checkReminder(context, note));

    note.setTrashed(true);
    new SaveNoteTask(false).doInBackground(note);

    assertFalse(ReminderHelper.checkReminder(context, note));
  }

  @Test
  public void untrashingNoteRestoresItsReminder() {
    var note = createRemindedNote("Restore rearm");
    var context = OmniNotes.getAppContext();
    note.setTrashed(true);
    new SaveNoteTask(false).doInBackground(note);
    assertFalse(ReminderHelper.checkReminder(context, note));

    note.setTrashed(false);
    new SaveNoteTask(false).doInBackground(note);

    assertTrue(ReminderHelper.checkReminder(context, note));
  }

  @Test
  public void deletingNoteCancelsItsReminder() {
    var note = createRemindedNote("Delete rearm");
    var context = OmniNotes.getAppContext();
    assertTrue(ReminderHelper.checkReminder(context, note));

    new NoteProcessorDelete(List.of(note)).processNote(note);

    assertFalse(ReminderHelper.checkReminder(context, note));
    assertNull(dbHelper.getNote(note.get_id()));
  }

  @Test
  public void firedAlarmOfDeletedNoteIsNotRestored() {
    var note = createRemindedNote("Reset note");
    dbHelper.deleteNote(note);

    var intent = new Intent(OmniNotes.getAppContext(), AlarmReceiver.class);
    intent.putExtra(INTENT_NOTE, ParcelableUtil.marshall(note));
    new AlarmReceiver().onReceive(OmniNotes.getAppContext(), intent);

    assertNull(dbHelper.getNote(note.get_id()));
  }

  private Note createRemindedNote(String title) {
    var note = new Note();
    note.setTitle(title);
    note.setContent("content");
    note.setCreation(Calendar.getInstance().getTimeInMillis());
    note.setAlarm(Calendar.getInstance().getTimeInMillis() + 60_000);
    dbHelper.updateNote(note, false);
    ReminderHelper.addReminder(OmniNotes.getAppContext(), note);
    return note;
  }
}

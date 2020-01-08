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

package it.feio.android.omninotes;

import static it.feio.android.omninotes.utils.Constants.PREFS_NAME;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_DISMISS;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_NOTIFICATION_CLICK;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_POSTPONE;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_SNOOZE;
import static it.feio.android.omninotes.utils.ConstantsBase.INTENT_KEY;
import static it.feio.android.omninotes.utils.ConstantsBase.INTENT_NOTE;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_SNOOZE_DEFAULT;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;
import it.feio.android.omninotes.helpers.date.RecurrenceHelper;
import java.util.Arrays;
import java.util.Calendar;

import it.feio.android.omninotes.async.notes.SaveNoteTask;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.listeners.OnReminderPickedListener;
import it.feio.android.omninotes.utils.ReminderHelper;
import it.feio.android.omninotes.utils.date.DateUtils;
import it.feio.android.omninotes.utils.date.ReminderPickers;


public class SnoozeActivity extends AppCompatActivity implements OnReminderPickedListener {

  private Note note;
  private Note[] notes;

  public static void setNextRecurrentReminder(Note note) {
    if (!TextUtils.isEmpty(note.getRecurrenceRule())) {
      long nextReminder = RecurrenceHelper.nextReminderFromRecurrenceRule(Long.parseLong(note.getAlarm()), note
          .getRecurrenceRule());
      if (nextReminder > 0) {
        updateNoteReminder(nextReminder, note, true);
      }
    } else {
      new SaveNoteTask(false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, note);
    }
  }

  private static void updateNoteReminder(long reminder, Note note) {
    updateNoteReminder(reminder, note, false);
  }

  private static void updateNoteReminder(long reminder, Note noteToUpdate, boolean updateNote) {
    if (updateNote) {
      noteToUpdate.setAlarm(reminder);
      new SaveNoteTask(false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, noteToUpdate);
    } else {
      ReminderHelper.addReminder(OmniNotes.getAppContext(), noteToUpdate, reminder);
      ReminderHelper.showReminderMessage(noteToUpdate.getAlarm());
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getIntent().getParcelableExtra(INTENT_NOTE) != null) {
      note = getIntent().getParcelableExtra(INTENT_NOTE);
      manageNotification(getSharedPreferences(PREFS_NAME, MODE_MULTI_PROCESS));
    } else {
      Object[] notesObjs = (Object[]) getIntent().getExtras().get(INTENT_NOTE);
      notes = Arrays.copyOf(notesObjs, notesObjs.length, Note[].class);
      postpone(getSharedPreferences(PREFS_NAME, MODE_MULTI_PROCESS), DateUtils.getNextMinute(), null);
    }
  }

  private void manageNotification(SharedPreferences prefs) {
    if (ACTION_DISMISS.equals(getIntent().getAction())) {
      setNextRecurrentReminder(note);
      finish();
    } else if (ACTION_SNOOZE.equals(getIntent().getAction())) {
      String snoozeDelay = prefs.getString("settings_notification_snooze_delay", PREF_SNOOZE_DEFAULT);
      long newReminder = Calendar.getInstance().getTimeInMillis() + Integer.parseInt(snoozeDelay) * 60 * 1000;
      updateNoteReminder(newReminder, note);
      finish();
    } else if (ACTION_POSTPONE.equals(getIntent().getAction())) {
      postpone(prefs, Long.parseLong(note.getAlarm()), note.getRecurrenceRule());
    } else {
      Intent intent = new Intent(this, MainActivity.class);
      intent.putExtra(INTENT_KEY, note.get_id());
      intent.setAction(ACTION_NOTIFICATION_CLICK);
      startActivity(intent);
      finish();
    }
    removeNotification(note);
  }

  private void postpone(SharedPreferences prefs, Long alarm, String recurrenceRule) {
    ReminderPickers reminderPicker = new ReminderPickers(this, this);
    reminderPicker.pick(alarm, recurrenceRule);
  }

  private void removeNotification(Note note) {
    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    manager.cancel(String.valueOf(note.get_id()), 0);
  }

  @Override
  public void onReminderPicked(long reminder) {
    if (note != null) {
      note.setAlarm(reminder);
    } else {
      for (Note currentNote : notes) {
        currentNote.setAlarm(reminder);
      }
    }
  }

  @Override
  public void onRecurrenceReminderPicked(String recurrenceRule) {
    if (note != null) {
      note.setRecurrenceRule(recurrenceRule);
      setNextRecurrentReminder(note);
    } else {
      for (Note processedNotes : notes) {
        processedNotes.setRecurrenceRule(recurrenceRule);
        setNextRecurrentReminder(processedNotes);
      }
      setResult(RESULT_OK, getIntent());
    }
    finish();
  }

}

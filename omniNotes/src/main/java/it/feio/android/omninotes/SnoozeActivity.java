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

package it.feio.android.omninotes;

import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import it.feio.android.omninotes.async.notes.SaveNoteTask;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.listeners.OnReminderPickedListener;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.ReminderHelper;
import it.feio.android.omninotes.utils.date.DateHelper;
import it.feio.android.omninotes.utils.date.ReminderPickers;


public class SnoozeActivity extends ActionBarActivity implements OnReminderPickedListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private Note note;
    private ReminderPickers onDateSetListener;
    private ReminderPickers onTimeSetListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        note = getIntent().getParcelableExtra(Constants.INTENT_NOTE);

        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);

        // If an alarm has been fired a notification must be generated
        if (Constants.ACTION_DISMISS.equals(getIntent().getAction())) {
            setNextRecurrentReminder(note);
            finish();
        } else if (Constants.ACTION_SNOOZE.equals(getIntent().getAction())) {
            String snoozeDelay = prefs.getString("settings_notification_snooze_delay", Constants.PREF_SNOOZE_DEFAULT);
            long newReminder = Calendar.getInstance().getTimeInMillis() + Integer.parseInt(snoozeDelay) * 60 * 1000;
            updateNoteReminder(newReminder, note);
            finish();
        } else if (Constants.ACTION_POSTPONE.equals(getIntent().getAction())) {
            int pickerType = prefs.getBoolean("settings_simple_calendar", false) ? ReminderPickers.TYPE_AOSP :
                    ReminderPickers.TYPE_GOOGLE;
            ReminderPickers reminderPicker = new ReminderPickers(this, this, pickerType);
            reminderPicker.pick(Long.parseLong(note.getAlarm()), note.getRecurrenceRule());
            onDateSetListener = reminderPicker;
            onTimeSetListener = reminderPicker;
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(Constants.INTENT_KEY, note.get_id());
            intent.setAction(Constants.ACTION_NOTIFICATION_CLICK);
            startActivity(intent);
        }
        removeNotification(note);
    }


    private void removeNotification(Note note) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(note.get_id().intValue());
    }


    @Override
    public void onReminderPicked(long reminder) {
        this.note.setAlarm(reminder);
    }

    @Override
    public void onRecurrenceReminderPicked(String recurrenceRule) {
        this.note.setRecurrenceRule(recurrenceRule);
        setNextRecurrentReminder(note);
    }


    public static void setNextRecurrentReminder(Note note) {
        if (!TextUtils.isEmpty(note.getRecurrenceRule())) {
            long nextReminder = DateHelper.nextReminderFromRecurrenceRule(Long.parseLong(note.getAlarm()), note
                    .getRecurrenceRule());
            if (nextReminder > 0) {
                updateNoteReminder(nextReminder, note, true);
            }
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
        }
        Toast.makeText(OmniNotes.getAppContext(), OmniNotes.getAppContext().getString(R.string.alarm_set_on) + " " +
                DateHelper.getDateTimeShort(OmniNotes.getAppContext(), reminder), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        onDateSetListener.onDateSet(view, year, monthOfYear, dayOfMonth);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        onTimeSetListener.onTimeSet(view, hourOfDay, minute);
    }
}

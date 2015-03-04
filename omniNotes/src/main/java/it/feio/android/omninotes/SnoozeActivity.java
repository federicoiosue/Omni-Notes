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

import android.app.AlarmManager;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;
import it.feio.android.omninotes.async.notes.SaveNoteTask;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.listeners.OnReminderPickedListener;
import it.feio.android.omninotes.receiver.AlarmReceiver;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.date.ReminderPickers;

import java.util.Calendar;


public class SnoozeActivity extends FragmentActivity implements OnReminderPickedListener, OnDateSetListener, 
        OnTimeSetListener {

    private Note note;
    private OnDateSetListener mOnDateSetListener;
    private OnTimeSetListener mOnTimeSetListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        note = getIntent().getParcelableExtra(Constants.INTENT_NOTE);

        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);

        // If an alarm has been fired a notification must be generated
        if (Constants.ACTION_DISMISS.equals(getIntent().getAction())) {
            finish();
        } else if (Constants.ACTION_SNOOZE.equals(getIntent().getAction())) {
            String snoozeDelay = prefs.getString("settings_notification_snooze_delay", "10");
            long newReminder = Calendar.getInstance().getTimeInMillis() + Integer.parseInt(snoozeDelay) * 60 * 1000;
            updateNoteReminder(newReminder);
        } else if (Constants.ACTION_POSTPONE.equals(getIntent().getAction())) {
            int pickerType = prefs.getBoolean("settings_simple_calendar", false) ? ReminderPickers.TYPE_AOSP :
                    ReminderPickers.TYPE_GOOGLE;
            ReminderPickers reminderPicker = new ReminderPickers(this, this, pickerType);
            reminderPicker.pick(Long.parseLong(note.getAlarm()));
            mOnDateSetListener = reminderPicker;
            mOnTimeSetListener = reminderPicker;
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
        manager.cancel(note.get_id());
    }


    @Override
    public void onReminderPicked(long reminder) {
        updateNoteReminder(reminder);
    }


    private void updateNoteReminder(long reminder) {
        note.setAlarm(reminder);
        new SaveNoteTask(this, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, note);
        Toast.makeText(this, R.string.note_updated, Toast.LENGTH_SHORT).show();
        finish();
    }


    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear,
                          int dayOfMonth) {
        mOnDateSetListener.onDateSet(view, year, monthOfYear, dayOfMonth);
    }


    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mOnTimeSetListener.onTimeSet(view, hourOfDay, minute);
    }


}

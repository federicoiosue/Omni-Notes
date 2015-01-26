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

import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.widget.DatePicker;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import it.feio.android.omninotes.models.listeners.OnReminderPickedListener;
import it.feio.android.omninotes.utils.Constants;

import java.util.Calendar;


public class ReminderPickers implements OnDateSetListener, OnTimeSetListener {

    public static final int TYPE_GOOGLE = 0;
    public static final int TYPE_AOSP = 1;

    private FragmentActivity mActivity;
    private OnReminderPickedListener mOnReminderPickedListener;
    private int pickerType;

    private int reminderYear;
    private int reminderMonth;
    private int reminderDay;

    private boolean timePickerCalledAlready = false;
    private long presetDateTime;


    public ReminderPickers(FragmentActivity mActivity,
                           OnReminderPickedListener mOnReminderPickedListener, int pickerType) {
        this.mActivity = mActivity;
        this.mOnReminderPickedListener = mOnReminderPickedListener;
        this.pickerType = pickerType;
    }


    public void pick() {
        pick(null);
    }


    public void pick(Long presetDateTime) {
        this.presetDateTime = DateHelper.getCalendar(presetDateTime).getTimeInMillis();
        if (pickerType == TYPE_AOSP) {
            timePickerCalledAlready = false;
            // Timepicker will be automatically called after date is inserted by user
            showDatePickerDialog(this.presetDateTime);
        } else {
            showDateTimeSelectors(this.presetDateTime);
        }
    }


//    /**
//     * Show date and time pickers
//     */
//    protected void showDateTimeSelectors(long reminder) {
//
//        // Sets actual time or previously saved in note
//        final Calendar now = DateHelper.getCalendar(reminder);
//        CalendarDatePickerDialog mCalendarDatePickerDialog = CalendarDatePickerDialog.newInstance(
//                new CalendarDatePickerDialog.OnDateSetListener() {
//
//                    @Override
//                    public void onDateSet(CalendarDatePickerDialog dialog, int year, int monthOfYear, 
// int dayOfMonth) {
//                        reminderYear = year;
//                        reminderMonth = monthOfYear;
//                        reminderDay = dayOfMonth;
//                        RadialTimePickerDialog mRadialTimePickerDialog = RadialTimePickerDialog.newInstance(
//                                new RadialTimePickerDialog.OnTimeSetListener() {
//                                    @Override
//                                    public void onTimeSet(
//                                            RadialPickerLayout radialPickerLayout,
//                                            int hourOfDay, int minute) {
//                                        // Setting alarm time in milliseconds
//                                        Calendar c = Calendar.getInstance();
//                                        c.set(reminderYear, reminderMonth, reminderDay, hourOfDay, minute);
//                                        if (mOnReminderPickedListener != null) {
//                                            mOnReminderPickedListener.onReminderPicked(c.getTimeInMillis());
//                                        }
//                                    }
//                                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), 
// DateHelper.is24HourMode(mActivity));
//                        mRadialTimePickerDialog.show(mActivity.getSupportFragmentManager(), Constants.TAG);
//                    }
//
//                }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
//        mCalendarDatePickerDialog.show(mActivity.getSupportFragmentManager(), Constants.TAG);
//    }


    /**
     * Show date and time pickers
     */
    protected void showDateTimeSelectors(long reminder) {

        // Sets actual time or previously saved in note
        final Calendar now = DateHelper.getCalendar(reminder);
        DatePickerDialog mCalendarDatePickerDialog = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
                        reminderYear = year;
                        reminderMonth = monthOfYear;
                        reminderDay = dayOfMonth;
                        TimePickerDialog mRadialTimePickerDialog = TimePickerDialog.newInstance(
                                new TimePickerDialog.OnTimeSetListener() {

                                    @Override
                                    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfDay, 
                                                          int minute) {
                                        // Setting alarm time in milliseconds
                                        Calendar c = Calendar.getInstance();
                                        c.set(reminderYear, reminderMonth, reminderDay, hourOfDay, minute);
                                        if (mOnReminderPickedListener != null) {
                                            mOnReminderPickedListener.onReminderPicked(c.getTimeInMillis());
                                        }
                                    }
                                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), 
                                DateHelper.is24HourMode(mActivity));
                        mRadialTimePickerDialog.show(mActivity.getSupportFragmentManager(), Constants.TAG);
                    }

                }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        mCalendarDatePickerDialog.show(mActivity.getSupportFragmentManager(), Constants.TAG);
    }


    /**
     * Shows fallback date and time pickers for smaller screens
     */

    public void showDatePickerDialog(long presetDateTime) {
        Bundle b = new Bundle();
        b.putLong(DatePickerDialogFragment.DEFAULT_DATE, presetDateTime);
        DialogFragment picker = new DatePickerDialogFragment();
        picker.setArguments(b);
        picker.show(mActivity.getSupportFragmentManager(), Constants.TAG);
    }


    private void showTimePickerDialog(long presetDateTime) {
        TimePickerFragment newFragment = new TimePickerFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(TimePickerFragment.DEFAULT_TIME, presetDateTime);
        newFragment.setArguments(bundle);
        newFragment.show(mActivity.getSupportFragmentManager(), Constants.TAG);
    }


    @Override
    public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
        // Setting alarm time in milliseconds
        Calendar c = Calendar.getInstance();
        c.set(reminderYear, reminderMonth, reminderDay, hourOfDay, minute);
        if (mOnReminderPickedListener != null) {
            mOnReminderPickedListener.onReminderPicked(c.getTimeInMillis());
        }
    }


    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        reminderYear = year;
        reminderMonth = monthOfYear;
        reminderDay = dayOfMonth;
        if (!timePickerCalledAlready) {    // Used to avoid native bug that calls onPositiveButtonPressed in the onClose()
            timePickerCalledAlready = true;
            showTimePickerDialog(presetDateTime);
        }
    }
}

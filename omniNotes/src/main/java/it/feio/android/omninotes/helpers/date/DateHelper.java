/*
 * Copyright (C) 2018 Federico Iosue (federico.iosue@gmail.com)
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
package it.feio.android.omninotes.helpers.date;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import be.billington.calendar.recurrencepicker.EventRecurrence;
import be.billington.calendar.recurrencepicker.EventRecurrenceFormatter;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.utils.Constants;
import net.fortuna.ical4j.model.property.RRule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * Helper per la generazione di date nel formato specificato nelle costanti
 *
 * @author 17000026
 */
public class DateHelper {

    public static String getSortableDate() {
        String result;
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_SORTABLE);
        Date now = Calendar.getInstance().getTime();
        result = sdf.format(now);
        return result;
    }


    /**
     * Build a formatted date string starting from values obtained by a DatePicker
     *
     * @param year
     * @param month
     * @param day
     * @param format
     * @return
     */
    public static String onDateSet(int year, int month, int day, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        return sdf.format(cal.getTime());
    }


    /**
     * Build a formatted time string starting from values obtained by a TimePicker
     *
     * @param hour
     * @param minute
     * @param format
     * @return
     */
    public static String onTimeSet(int hour, int minute, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        return sdf.format(cal.getTime());
    }

    /**
     * @param mContext
     * @param date
     * @return
     */
    public static String getDateTimeShort(Context mContext, Long date) {
        int flags = DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_SHOW_WEEKDAY
                | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_DATE;
        return (date == null) ? "" : DateUtils.formatDateTime(mContext, date, flags)
                + " " + DateUtils.formatDateTime(mContext, date, DateUtils.FORMAT_SHOW_TIME);
    }


    /**
     * @param mContext
     * @param date
     * @return
     */
    public static String getTimeShort(Context mContext, Long time) {
        if (time == null)
            return "";
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        return DateUtils.formatDateTime(mContext, time, DateUtils.FORMAT_SHOW_TIME);
    }


    /**
     * @param mContext
     * @param hourOfDay
     * @param minute
     * @return
     */
    public static String getTimeShort(Context mContext, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        return DateUtils.formatDateTime(mContext, c.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME);
    }


    /**
     * Formats a short time period (minutes)
     *
     * @param time
     * @return
     */
    public static String formatShortTime(Context mContext, long time) {
        String m = String.valueOf(time / 1000 / 60);
        String s = String.format("%02d", (time / 1000) % 60);
        return m + ":" + s;
    }


    public static String formatRecurrence(Context mContext, String recurrenceRule) {
        if (!TextUtils.isEmpty(recurrenceRule)) {
            EventRecurrence recurrenceEvent = new EventRecurrence();
            recurrenceEvent.setStartDate(new Time("" + new Date().getTime()));
            recurrenceEvent.parse(recurrenceRule);
            return EventRecurrenceFormatter.getRepeatString(mContext.getApplicationContext(),
                    mContext.getResources(), recurrenceEvent, true);
        } else {
            return "";
        }
    }


    public static Long nextReminderFromRecurrenceRule(long reminder, String recurrenceRule) {
        return nextReminderFromRecurrenceRule(reminder, Calendar.getInstance().getTimeInMillis(), recurrenceRule);
    }


    public static Long nextReminderFromRecurrenceRule(long reminder, long currentTime, String recurrenceRule) {
        RRule rule = new RRule();
        try {
            rule.setValue(recurrenceRule);
            net.fortuna.ical4j.model.DateTime seed = new net.fortuna.ical4j.model.DateTime(reminder);
            long startTimestamp = reminder + 60 * 1000;
            if (startTimestamp < currentTime) {
                startTimestamp = currentTime;
            }
            net.fortuna.ical4j.model.DateTime start = new net.fortuna.ical4j.model.DateTime(startTimestamp);
            Date nextDate = rule.getRecur().getNextDate(seed, start);
            return nextDate == null ? 0L : nextDate.getTime();
        } catch (ParseException e) {
            Log.e(Constants.TAG, "Error parsing rrule");
        }
        return 0L;
    }


    public static String getNoteReminderText(long reminder) {
        return OmniNotes.getAppContext().getString(R.string.alarm_set_on) + " " + getDateTimeShort(OmniNotes
				.getAppContext(), reminder);
    }


    public static String getNoteRecurrentReminderText(long reminder, String rrule) {
        return DateHelper.formatRecurrence(OmniNotes.getAppContext(), rrule) + " " + OmniNotes.getAppContext().getString
                (R.string.starting_from) + " " + DateHelper.getDateTimeShort(OmniNotes.getAppContext(), reminder);
    }


	public static String getFormattedDate(Long timestamp, boolean prettified) {
		if(prettified) {
			return it.feio.android.omninotes.utils.date.DateUtils.prettyTime(timestamp);
		} else {
			return DateHelper.getDateTimeShort(OmniNotes.getAppContext(), timestamp);
		}
	}

}

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
package it.feio.android.omninotes.helpers.date;

import static it.feio.android.omninotes.utils.ConstantsBase.DATE_FORMAT_SORTABLE;

import android.content.Context;
import android.text.format.DateUtils;
import it.feio.android.omninotes.OmniNotes;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Helper per la generazione di date nel formato specificato nelle costanti
 */
public class DateHelper {

  private DateHelper () {
    // hides public constructor
  }

  public static String getSortableDate () {
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_SORTABLE);
    return sdf.format(Calendar.getInstance().getTime());
  }


  /**
   * Build a formatted date string starting from values obtained by a DatePicker
   */
  public static String onDateSet (int year, int month, int day, String format) {
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.MONTH, month);
    cal.set(Calendar.DAY_OF_MONTH, day);
    return sdf.format(cal.getTime());
  }


  /**
   * Build a formatted time string starting from values obtained by a TimePicker
   */
  public static String onTimeSet (int hour, int minute, String format) {
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, hour);
    cal.set(Calendar.MINUTE, minute);
    return sdf.format(cal.getTime());
  }

  /**
   *
   */
  public static String getDateTimeShort (Context mContext, Long date) {
    int flags = DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_SHOW_WEEKDAY
        | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_DATE;
    return (date == null) ? "" : DateUtils.formatDateTime(mContext, date, flags)
        + " " + DateUtils.formatDateTime(mContext, date, DateUtils.FORMAT_SHOW_TIME);
  }


  /**
   *
   */
  public static String getTimeShort (Context mContext, Long time) {
    if (time == null) {
      return "";
    }
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(time);
    return DateUtils.formatDateTime(mContext, time, DateUtils.FORMAT_SHOW_TIME);
  }


  /**
   *
   */
  public static String getTimeShort (Context mContext, int hourOfDay, int minute) {
    Calendar c = Calendar.getInstance();
    c.set(Calendar.HOUR_OF_DAY, hourOfDay);
    c.set(Calendar.MINUTE, minute);
    return DateUtils.formatDateTime(mContext, c.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME);
  }


  /**
   * Formats a short time period (minutes)
   */
  public static String formatShortTime (Context mContext, long time) {
    String m = String.valueOf(time / 1000 / 60);
    String s = String.format("%02d", (time / 1000) % 60);
    return m + ":" + s;
  }


  public static String getFormattedDate (Long timestamp, boolean prettified) {
    if (prettified) {
      return it.feio.android.omninotes.utils.date.DateUtils.prettyTime(timestamp);
    } else {
      return DateHelper.getDateTimeShort(OmniNotes.getAppContext(), timestamp);
    }
  }

}

/*******************************************************************************
 * Copyright 2014 Federico Iosue (federico.iosue@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package it.feio.android.omninotes.utils.date;

import it.feio.android.omninotes.utils.Constants;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.util.Log;


/**
 * Helper per la generazione di date nel formato specificato nelle costanti
 * @author 17000026
 *
 */
public class DateHelper {

	public static String getString(long date, String format) {
		Date d = new Date(date);
		return getString(d, format);
	}
	
	public static String getString(Date d, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(d);
	}
	
	
	public static Calendar getDateFromString(String str, String format) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			cal.setTime(sdf.parse(str));
		} catch (ParseException e) {
			Log.e(Constants.TAG, "Malformed datetime string" + e.getMessage());

		} catch (NullPointerException e) {
			Log.d(Constants.TAG, "Date or time not set");
		}
		return cal;
	}
	
	/**
	 * Build a formatted date string starting from values obtained by a DatePicker
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
		String dateString = sdf.format(cal.getTime());
		return dateString;		
	}
	
	/**
	 * Build a formatted time string starting from values obtained by a TimePicker
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
		String dateString = sdf.format(cal.getTime());
		return dateString;		
	}

	
	
	public static Calendar getLongFromDateTime(String date, String dateFormat, String time, String timeFormat) {
		Calendar cal = Calendar.getInstance();
		Calendar cDate = Calendar.getInstance();
		Calendar cTime = Calendar.getInstance();
		SimpleDateFormat sdfDate = new SimpleDateFormat(dateFormat);
		SimpleDateFormat sdfTime = new SimpleDateFormat(timeFormat);
		try {
			cDate.setTime(sdfDate.parse(date));
			cTime.setTime(sdfTime.parse(time));
		} catch (ParseException e) {
			Log.e(Constants.TAG, "Date or time parsing error: " + e.getMessage());
		}
		cal.set(Calendar.YEAR, cDate.get(Calendar.YEAR));
		cal.set(Calendar.MONTH, cDate.get(Calendar.MONTH));
		cal.set(Calendar.DAY_OF_MONTH, cDate.get(Calendar.DAY_OF_MONTH));
		cal.set(Calendar.HOUR_OF_DAY, cTime.get(Calendar.HOUR_OF_DAY));
		cal.set(Calendar.MINUTE, cTime.get(Calendar.MINUTE));
		cal.set(Calendar.SECOND, 0);
		return cal;		
	}
	
	
	
//	public static String getLocalizedDateTime(Context mContext, String dateString, String format) {
//		SimpleDateFormat sdf = new SimpleDateFormat(format);
//		Date date = null;
//		try {
//			date = sdf.parse(dateString);
//		} catch (ParseException e) {
//			// handle exception here !
//		}
//		String dateFormatted = android.text.format.DateFormat.getDateFormat(mContext).format(date);
//		String timeFormatted = android.text.format.DateFormat.getTimeFormat(mContext).format(date);
//		return dateFormatted + " " + timeFormatted;
//	}
	
}

package it.feio.android.omninotes.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.util.Log;


/**
 * Helper per la generazione di date nel formato specificato nelle costanti
 * @author 17000026
 *
 */
public class DateHelper {

	public static String getDateString(long date) {
		Date d = new Date(date);
		return getDateString(d);
	}
	
	public static String getDateString(Date d) {
		String dateString = "";
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_EU);
		dateString = sdf.format(d);
		return dateString;
	}
	
	public static Calendar getDateFromString(String str, String format) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			cal.setTime(sdf.parse(str));
		} catch (ParseException e) {
			Log.e(Constants.TAG, "Malformed datetime string" + e.getMessage());
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
		return cal;		
	}
}

package it.feio.android.omninotes.utils;

import java.text.SimpleDateFormat;
import java.util.Date;


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
}

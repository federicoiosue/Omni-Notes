package it.feio.android.omninotes.utils;

public interface Constants {

	final String TAG = "Omni Notes";
	final String PREFS_FILE_NAME = "omni-notes.prefs";
	
	// Splash screen timer
	static int SPLASH_TIME_OUT = 2000;

	final String DEV_EMAIL = "federico.iosue@gmail.com";

	final String DATE_SEPARATOR = "/";
	final String TIME_SEPARATOR = ":";
	
	final String DATE_FORMAT_EU = "dd/MM/yyyy HH:mm";
	final String DATE_FORMAT_ISO8601  = "YYYY-MM-DD HH:mm:SS.SSS";
	final String DATE_FORMAT_SHORT  = "d MMM HH:mm";
	final String DATE_FORMAT_SHORT_DATE  = "d MMM";
	final String DATE_FORMAT_SHORT_TIME  = "HH" + TIME_SEPARATOR + "mm";
	
    final String INTENT_KEY = "note_id";
    final String INTENT_NOTE = "note";
	final int INTENT_ALARM_CODE = 12345;
	
	final String MESSAGE = "message";

    final String PREF_NAVIGATION = "navigation";
    final String PREF_SORTING_COLUMN = "sorting_column";
    final String PREF_SORTING_ORDER = "sorting_direction";
    
    final String EXPORT_FILE_NAME = TAG;
}

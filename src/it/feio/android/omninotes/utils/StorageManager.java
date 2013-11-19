package it.feio.android.omninotes.utils;

import it.feio.android.omninotes.BaseActivity;
import java.io.File;

import android.os.Environment;

public class StorageManager {
	
	public static boolean checkStorage() {
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		return mExternalStorageAvailable && mExternalStorageWriteable;
	}
	
	public static String getStorageDir() {
//		return Environment.getExternalStorageDirectory() + File.separator + Constants.TAG + File.separator;
		return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
	}
	
	public static String getDataStorageDir() {		
		return Environment.getDataDirectory() + File.separator + "data" + File.separator + BaseActivity.class.getPackage().getName() + File.separator;
	}
	

}

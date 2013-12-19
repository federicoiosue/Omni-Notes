/*******************************************************************************
 * Copyright 2013 Federico Iosue (federico.iosue@gmail.com)
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
package it.feio.android.omninotes.utils;

import it.feio.android.omninotes.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

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
		// return Environment.getExternalStorageDirectory() + File.separator +
		// Constants.TAG + File.separator;
		return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
	}

	public static File getAttachmentDir(Context mContext) {
		return mContext.getExternalFilesDir(null);
	}


	
	/**
	 * Create a path where we will place our private file on external
	 * 
	 * @param mContext
	 * @param uri
	 * @return
	 */
	public static File createExternalStoragePrivateFile(Context mContext, Uri uri) {

		// Checks for external storage availability
		if (!checkStorage()) {
			Toast.makeText(mContext, mContext.getString(R.string.storage_not_available), Toast.LENGTH_SHORT).show();
			return null;
		}
		File file = createNewAttachmentFile(mContext);

		try {
			InputStream is = mContext.getContentResolver().openInputStream(uri);
			OutputStream os = new FileOutputStream(file);
			copyFile(is, os);
		} catch (IOException e) {
			Log.e(Constants.TAG, "Error writing " + file, e);
			return null;
		}
		return file;
	}

	
	
	
	public static boolean copyFile(File source, File destination) {
		try {
			return copyFile(new FileInputStream(source), new FileOutputStream(destination));
		} catch (FileNotFoundException e) {
			Log.e(Constants.TAG, "Error copying file", e);
			return false;
		}
	}

	
	
	/**
	 * Generic file copy method
	 * @param is Input
	 * @param os Output
	 * @return True if copy is done, false otherwise
	 */
	public static boolean copyFile(InputStream is, OutputStream os) {
		boolean res = false;
		byte[] data;
		try {
			data = new byte[is.available()];
			is.read(data);
			os.write(data);
			is.close();
			os.close();
			res = true;
		} catch (IOException e) {
			Log.e(Constants.TAG, "Error copying file", e);
		}
		return res;
	}

	
	public static boolean deleteExternalStoragePrivateFile(Context mContext, String name) {
		boolean res = false;

		// Checks for external storage availability
		if (!checkStorage()) {
			Toast.makeText(mContext, mContext.getString(R.string.storage_not_available), Toast.LENGTH_SHORT).show();
			return res;
		}

		File file = new File(mContext.getExternalFilesDir(null), name);
		if (file != null) {
			file.delete();
			res = true;
		}
		
		return res;
	}

	
	
	
	public static String getRealPathFromURI(Context mContext, Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
	    Cursor cursor = mContext.getContentResolver().query(contentUri, proj, null, null, null);
	    if (cursor == null) {
	    	return null;
	    }
	    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	    cursor.moveToFirst();
	    return cursor.getString(column_index);
	}
	
	
	public static File createNewAttachmentFile(Context mContext){
		DateTime now = DateTime.now();
		String name = now.toString(DateTimeFormat.forPattern(Constants.DATE_FORMAT_SORTABLE));
		File f = new File(mContext.getExternalFilesDir(null), name);
		return f;
	}

	
	
	/**
	 * Create a path where we will place our private file on external
	 * 
	 * @param mContext
	 * @param uri
	 * @return
	 */
	public static File copyToBackupDir(File backupDir, File file) {

		// Checks for external storage availability
		if (!checkStorage()) {
			return null;
		}
		
		if (!backupDir.exists()) {
			backupDir.mkdirs();
		}
		
		File destination = new File(backupDir, file.getName());

		try {
			copyFile(new FileInputStream(file), new FileOutputStream(destination));
		} catch (FileNotFoundException e) {
			Log.e(Constants.TAG, "Error copying file to backup", e);
			destination = null;
		}
		
		return destination;
	}
	
	

	public static File getExternalStoragePublicDir() {
		File dir = new File(Environment.getExternalStorageDirectory() + File.separator + Constants.TAG + File.separator);		
		if (!dir.exists())
			dir.mkdirs();
		return dir;
	}
	
	
	public static File getBackupDir(String backupName){
		File backupDir = new File(getExternalStoragePublicDir(), backupName);		
		if (!backupDir.exists())
			backupDir.mkdirs();
		return backupDir;
	}
	
	
	
	public static boolean copyDirectory(File sourceLocation, File targetLocation) {
		boolean res = true;
		
		// If target is a directory the method will be iterated
		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdirs();
			}

			String[] children = sourceLocation.list();
			for (int i = 0; i < sourceLocation.listFiles().length; i++) {
				res = res && copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
			}

			// Otherwise a file copy will be performed
		} else {
			try {
				res = res && copyFile(new FileInputStream(sourceLocation), new FileOutputStream(targetLocation));
			} catch (FileNotFoundException e) {
				Log.e(Constants.TAG, "Error copying directory", e);
				res = false;
			}
		}		
		return res;
	}
	
}

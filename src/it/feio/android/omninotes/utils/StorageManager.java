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
package it.feio.android.omninotes.utils;

import it.feio.android.omninotes.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
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
	public static File createExternalStoragePrivateFile(Context mContext, Uri uri, String extension) {

		// Checks for external storage availability
		if (!checkStorage()) {
			Toast.makeText(mContext, mContext.getString(R.string.storage_not_available), Toast.LENGTH_SHORT).show();
			return null;
		}
		File file = createNewAttachmentFile(mContext, extension);

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

	
	public static boolean delete(Context mContext, String name) {
		boolean res = false;

		// Checks for external storage availability
		if (!checkStorage()) {
			Toast.makeText(mContext, mContext.getString(R.string.storage_not_available), Toast.LENGTH_SHORT).show();
			return res;
		}

		File file = new File(name);
		if (file != null) {
			if (file.isFile()) {
				res = file.delete();
			} else if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (File file2 : files) {
					res = delete(mContext, file2.getAbsolutePath());					
				}
				res = file.delete();
			}
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
	
	
	public static File createNewAttachmentFile(Context mContext, String extension){
		Calendar now = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_SORTABLE);
		String name = sdf.format(now.getTime());
		name += extension != null ? extension : "";
		File f = new File(mContext.getExternalFilesDir(null), name);
		return f;
	}
	
	
	public static File createNewAttachmentFile(Context mContext){
		return createNewAttachmentFile(mContext, null);
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
	
	
	
	public static File getCacheDir(Context mContext) {
		File dir = mContext.getExternalCacheDir();		
		if (!dir.exists())
			dir.mkdirs();
		return dir;
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
	
	
	public static File getDefaultSharedPreferences(Context mContext){
		File appData = mContext.getFilesDir().getParentFile();
		String packageName = mContext.getApplicationContext().getPackageName();
		File prefsPath = new File(appData 
									+ System.getProperty("file.separator") 
									+ "shared_prefs" 
									+ System.getProperty("file.separator") 
									+ packageName 
									+ "_preferences.xml");
		return prefsPath;
	}
	
	
	/**
	 * Returns a directory size in bytes
	 * @param directory
	 * @return
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static long getSize(File directory) {
	    StatFs statFs = new StatFs(directory.getAbsolutePath());
	    long blockSize = 0;
	    try {
		    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
		        blockSize = statFs.getBlockSizeLong();
		    } else {
		        blockSize = statFs.getBlockSize();
		    }
		// Can't understand why on some devices this fails
	    } catch (NoSuchMethodError e) {}

	    return getSize(directory, blockSize);
	}
	
		
	private static long getSize(File directory, long blockSize) {
	    File[] files = directory.listFiles();
	    if (files != null) {

	        // space used by directory itself 
	        long size = directory.length();

	        for (File file : files) {
	            if (file.isDirectory()) {
	                // space used by subdirectory
	                size += getSize(file, blockSize);
	            } else {
	                // file size need to rounded up to full block sizes
	                // (not a perfect function, it adds additional block to 0 sized files
	                // and file who perfectly fill their blocks) 
	                size += (file.length() / blockSize + 1) * blockSize;
	            }
	        }
	        return size;
	    } else {
	        return 0;
	    }
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
	
	

	/**
	 * Retrieves uri mime-type using ContentResolver
	 * @param mContext
	 * @param uri
	 * @return
	 */
	public static String getMimeType(Context mContext, Uri uri) {
		ContentResolver cR = mContext.getContentResolver();
		String mimeType = cR.getType(uri);
		return mimeType;
	}
	
		
	/**
	 * Tries to retrieve mime types from file extension
	 * @param url
	 * @return
	 */
	public static String getMimeType(String url) {
		String type = null;
		String extension = MimeTypeMap.getFileExtensionFromUrl(url);
		if (extension != null) {
			MimeTypeMap mime = MimeTypeMap.getSingleton();
			type = mime.getMimeTypeFromExtension(extension);
		}
		return type;
	}
	
	
	/**
	 * Retrieves uri mime-type between the ones managed by application
	 * @param mContext
	 * @param uri
	 * @return
	 */
	public static String getMimeTypeInternal(Context mContext, Uri uri) {		
		String mimeType = getMimeType(mContext, uri);
		mimeType = getMimeTypeInternal(mContext, mimeType);		
		return mimeType;		
	}	
	
	/**
	 * Retrieves mime-type between the ones managed by application from given string
	 * @param mContext
	 * @param mimeType
	 * @return
	 */
	public static String getMimeTypeInternal(Context mContext, String mimeType) {	
		if (mimeType != null) {
			if (mimeType.contains("image/")) {
				mimeType = Constants.MIME_TYPE_IMAGE;
			} else if (mimeType.contains("audio/")) {
				mimeType = Constants.MIME_TYPE_AUDIO;
			} else if (mimeType.contains("video/")) {
				mimeType = Constants.MIME_TYPE_VIDEO;
			}
		}
		return mimeType;
	}
	
	
	
	
}

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

import it.feio.android.omninotes.BaseActivity;
import it.feio.android.omninotes.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;

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

	public static String getExternalStorageDir() {
		// return Environment.getExternalStorageDirectory() + File.separator +
		// Constants.TAG + File.separator;
		return Environment.getExternalStorageDirectory().toString();
	}

	public static String getStorageDir() {
		// return Environment.getExternalStorageDirectory() + File.separator +
		// Constants.TAG + File.separator;
		return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
	}

	public static String getPictureDir() {
		// return Environment.getExternalStorageDirectory() + File.separator +
		// Constants.TAG + File.separator;
		return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
	}

	public static String getDataStorageDir() {
		return Environment.getDataDirectory() + File.separator + "data" + File.separator
				+ BaseActivity.class.getPackage().getName() + File.separator;
	}

	public static String getApplicationDir() {
		File dir = new File(getExternalStorageDir() + File.separator + Constants.APP_STORAGE_DIRECTORY);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir.getAbsolutePath();
	}

	public static File getAttachmentDir(Context mContext) {
		return mContext.getExternalFilesDir(null);
	}

	/**
	 * Used to copy files from a storage type to another
	 * 
	 * @return Result
	 */
	public static boolean copyFile(File src, File dst) {
		boolean returnValue = true;

		FileChannel inChannel = null, outChannel = null;

		try {

			inChannel = new FileInputStream(src).getChannel();
			outChannel = new FileOutputStream(dst).getChannel();

		} catch (FileNotFoundException fnfe) {

			Log.d(Constants.TAG, "inChannel/outChannel FileNotFoundException");
			fnfe.printStackTrace();
			return false;
		}

		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);

		} catch (IllegalArgumentException iae) {

			Log.d(Constants.TAG, "TransferTo IllegalArgumentException");
			iae.printStackTrace();
			returnValue = false;

		} catch (NonReadableChannelException nrce) {

			Log.d(Constants.TAG, "TransferTo NonReadableChannelException");
			nrce.printStackTrace();
			returnValue = false;

		} catch (NonWritableChannelException nwce) {

			Log.d(Constants.TAG, "TransferTo NonWritableChannelException");
			nwce.printStackTrace();
			returnValue = false;

		} catch (ClosedByInterruptException cie) {

			Log.d(Constants.TAG, "TransferTo ClosedByInterruptException");
			cie.printStackTrace();
			returnValue = false;

		} catch (AsynchronousCloseException ace) {

			Log.d(Constants.TAG, "TransferTo AsynchronousCloseException");
			ace.printStackTrace();
			returnValue = false;

		} catch (ClosedChannelException cce) {

			Log.d(Constants.TAG, "TransferTo ClosedChannelException");
			cce.printStackTrace();
			returnValue = false;

		} catch (IOException ioe) {

			Log.d(Constants.TAG, "TransferTo IOException");
			ioe.printStackTrace();
			returnValue = false;

		} finally {

			if (inChannel != null)

				try {

					inChannel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			if (outChannel != null)
				try {
					outChannel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

		}

		return returnValue;
	}

	/**
	 * Used to copy files from a storage type to another
	 * 
	 * @return Result
	 */
	public static boolean copyFile(InputStream src, OutputStream dst) {
		boolean returnValue = false;

		int read = 0;
		byte[] bytes = new byte[1024];

		try {
			while ((read = src.read(bytes)) != -1) {
				dst.write(bytes, 0, read);
			}
			returnValue = true;
		} catch (IOException e) {
			Log.e(Constants.TAG, "Error copying file");
		}

		return returnValue;
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
		File file = new File(mContext.getExternalFilesDir(null), uri.getLastPathSegment());

		try {
			InputStream is = mContext.getContentResolver().openInputStream(uri);
			OutputStream os = new FileOutputStream(file);
			byte[] data = new byte[is.available()];
			is.read(data);
			os.write(data);
			is.close();
			os.close();
		} catch (IOException e) {
			Log.e(Constants.TAG, "Error writing " + file, e);
			return null;
		}
		return file;
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

	
	public static boolean hasExternalStoragePrivateFile(Context mContext, String name) {
		// Get path for the file on external storage. If external
		// storage is not currently mounted this will fail.
		File file = new File(mContext.getExternalFilesDir(null), name);
		if (file != null) {
			return file.exists();
		}
		return false;
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
		String name = now.toString(DateTimeFormat.forPattern("yyyyMMddHHmmss"));
		File f = new File(mContext.getExternalFilesDir(null), name);
		return f;
	}

}

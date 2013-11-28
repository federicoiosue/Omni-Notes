package it.feio.android.omninotes.utils;

import it.feio.android.omninotes.BaseActivity;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;

import android.os.Environment;
import android.util.Log;

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
		return Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DOWNLOADS).toString();
	}

	
	
	public static String getDataStorageDir() {
		return Environment.getDataDirectory() + File.separator + "data"
				+ File.separator + BaseActivity.class.getPackage().getName()
				+ File.separator;
	}


	/**
	 * Used to copy files from a storage type to another
	 * @return Result
	 */
	public boolean copyFile(File src, File dst) {
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

}

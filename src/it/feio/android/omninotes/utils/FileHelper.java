package it.feio.android.omninotes.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

public class FileHelper {
	/**
	 * Get a file path from a Uri. This will get the the path for Storage Access
	 * Framework Documents, as well as the _data field for the MediaStore and
	 * other file-based ContentProviders.
	 * 
	 * @param context
	 *            The context.
	 * @param uri
	 *            The Uri to query.
	 * @author paulburke
	 */
	@SuppressLint("NewApi")
	public static String getPath(final Context context, final Uri uri) {

		if (uri == null)
			return null;

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/"
							+ split[1];
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"),
						Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] { split[1] };

				return getDataColumn(context, contentUri, selection,
						selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 * 
	 * @param context
	 *            The context.
	 * @param uri
	 *            The Uri to query.
	 * @param selection
	 *            (Optional) Filter used in the query.
	 * @param selectionArgs
	 *            (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri,
			String selection, String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver().query(uri, projection,
					selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, "Error retrieving uri path", e);
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri
				.getAuthority());
	}

	public static InputStream getInputStream(Context mContext, Uri mUri) {
		InputStream inputStream;
		try {
			inputStream = mContext.getContentResolver().openInputStream(mUri);
			inputStream.close();
			return inputStream;
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}
	
	
	
	
//	public static File getFileFromUri(Context mContext, Uri uri) {
//		File f = null;
//		try {
//			InputStream is = mContext.getContentResolver().openInputStream(uri);			
//			f = getFileFromInputStream(mContext, is, getNameFromUri(mContext, uri));
//		} catch (FileNotFoundException e) {
//			Log.e(Constants.TAG, "Error creating InputStream", e);
//		}
//		return f;
//	}

	
	
	public static String getNameFromUri(Context mContext, Uri uri) {
		String fileName = "";
		// Trying to retrieve file name from content resolver
		Cursor c = mContext.getContentResolver().query(uri, new String[]{"_display_name"}, null, null, null);
		if (c != null) {
			try {
				if (c.moveToFirst()) {
					fileName = c.getString(0);
				}
				
			} catch (Exception e) {}
		} else {
			fileName = uri.getLastPathSegment();
		}
		return fileName;
	}
	
	
	
//	public static File getFileFromInputStream(Context mContext, InputStream inputStream, String fileName) {
//		File file = null;
//		File f = null;
//		
//		try {
////			String name = !TextUtils.isEmpty(getFilePrefix(fileName)) ? getFilePrefix(fileName) : String
////					.valueOf(Calendar.getInstance().getTimeInMillis());
////			String extension = !TextUtils.isEmpty(getFileExtension(fileName)) ? getFileExtension(fileName) : "";
////			f = File.createTempFile(name, extension);
//			f = new File(StorageManager.getCacheDir(mContext), fileName);
//			f = StorageManager.createExternalStoragePrivateFile(mContext, uri, extension)
//			f.deleteOnExit();
//		} catch (IOException e1) {
//			Log.e(Constants.TAG, "Error creating file from InputStream", e1);
//			return file;
//		}
//		OutputStream outputStream = null;
//
//		try {
//
//			outputStream = new FileOutputStream(f);
//
//			int read = 0;
//			byte[] bytes = new byte[1024];
//			while ((read = inputStream.read(bytes)) != -1) {
//				outputStream.write(bytes, 0, read);
//			}
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			if (inputStream != null) {
//				try {
//					inputStream.close();
//				} catch (IOException e) {
//					Log.e(Constants.TAG,
//							"Error closing InputStream", e);
//				}
//			}
//			if (outputStream != null) {
//				try {
//					// outputStream.flush();
//					file = f;
//					outputStream.close();
//				} catch (IOException e) {
//					Log.e(Constants.TAG,
//							"Error closing OutputStream", e);
//				}
//
//			}
//		}
//		return file;
//	}

	
	
	public static String getFilePrefix(File file) {
		return getFilePrefix(file.getName());
	}

	public static String getFilePrefix(String fileName) {
		String prefix = fileName;
		int index = fileName.indexOf(".");
		if (index != -1) {
			prefix = fileName.substring(0, index);			
		}
		return prefix;
	}

	
	
	public static String getFileExtension(File file) {
		return getFileExtension(file.getName());
	}

	public static String getFileExtension(String fileName) {
		String extension = "";
		int index = fileName.indexOf(".");
		if (index != -1) {
			extension = fileName.substring(index, fileName.length());			
		} 
		return extension;
	}
}

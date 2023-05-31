/*
 * Copyright (C) 2013-2023 Federico Iosue (federico@iosue.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.feio.android.omninotes.utils;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static it.feio.android.omninotes.utils.ConstantsBase.MIME_TYPE_AUDIO;
import static it.feio.android.omninotes.utils.ConstantsBase.MIME_TYPE_FILES;
import static it.feio.android.omninotes.utils.ConstantsBase.MIME_TYPE_IMAGE;
import static it.feio.android.omninotes.utils.ConstantsBase.MIME_TYPE_VIDEO;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import androidx.annotation.Nullable;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.exceptions.unchecked.ExternalDirectoryCreationException;
import it.feio.android.omninotes.helpers.LogDelegate;
import it.feio.android.omninotes.models.Attachment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;


@UtilityClass
public class StorageHelper {

  public static boolean checkStorage() {
    boolean mExternalStorageAvailable;
    boolean mExternalStorageWriteable;
    String state = Environment.getExternalStorageState();

    switch (state) {
      case Environment.MEDIA_MOUNTED:
        // We can read and write the media
        mExternalStorageAvailable = mExternalStorageWriteable = true;
        break;
      case Environment.MEDIA_MOUNTED_READ_ONLY:
        // We can only read the media
        mExternalStorageAvailable = true;
        mExternalStorageWriteable = false;
        break;
      default:
        // Something else is wrong. It may be one of many other states, but
        // all we need
        // to know is we can neither read nor write
        mExternalStorageAvailable = mExternalStorageWriteable = false;
        break;
    }
    return mExternalStorageAvailable && mExternalStorageWriteable;
  }


  public static String getStorageDir() {
    return Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).toString();
  }


  public static File getAttachmentDir() {
    return OmniNotes.getAppContext().getExternalFilesDir(null);
  }


  /**
   * Retrieves the folderwhere to store data to sync notes
   */
  public static File getDbSyncDir(Context mContext) {
    File extFilesDir = mContext.getExternalFilesDir(null);
    File dbSyncDir = new File(extFilesDir, Constants.APP_STORAGE_DIRECTORY_SB_SYNC);
    dbSyncDir.mkdirs();
    if (dbSyncDir.exists() && dbSyncDir.isDirectory()) {
      return dbSyncDir;
    } else {
      return null;
    }
  }


  /**
   * Create a path where we will place our private file on external
   */
  public static File createExternalStoragePrivateFile(Context mContext, Uri uri, String extension) {

    if (!checkStorage()) {
      Toast.makeText(mContext, mContext.getString(R.string.storage_not_available),
          Toast.LENGTH_SHORT).show();
      return null;
    }
    File file = createNewAttachmentFile(mContext, extension);

    InputStream contentResolverInputStream = null;
    OutputStream contentResolverOutputStream = null;
    try {
      contentResolverInputStream = mContext.getContentResolver().openInputStream(uri);
      contentResolverOutputStream = new FileOutputStream(file);
      copyFile(contentResolverInputStream, contentResolverOutputStream);
    } catch (IOException e) {
      try {
        FileUtils.copyFile(new File(FileHelper.getPath(mContext, uri)), file);
        // It's a path!!
      } catch (NullPointerException e1) {
        try {
          FileUtils.copyFile(new File(uri.getPath()), file);
        } catch (IOException e2) {
          LogDelegate.e("Error writing " + file, e2);
          file = null;
        }
      } catch (IOException e2) {
        LogDelegate.e("Error writing " + file, e2);
        file = null;
      }
    } finally {
      try {
        if (contentResolverInputStream != null) {
          contentResolverInputStream.close();
        }
        if (contentResolverOutputStream != null) {
          contentResolverOutputStream.close();
        }
      } catch (IOException e) {
        LogDelegate.e("Error closing streams", e);
      }

    }
    return file;
  }

  public static boolean copyFile(Context context, Uri fileUri, Uri targetUri) {
    ContentResolver content = context.getContentResolver();
    try (var is = content.openInputStream(fileUri);
        var os = content.openOutputStream(targetUri)) {
      IOUtils.copy(is, os);
      return true;
    } catch (IOException e) {
      LogDelegate.e("Error copying file", e);
      return false;
    }
  }

  public static void copyFile(File source, File destination, boolean failOnError) throws IOException {
    try {
      FileUtils.copyFile(source, destination);
    } catch (IOException e) {
      LogDelegate.e("Error copying file: " + e.getMessage(), e);
      if (failOnError) throw e;
    }
  }

  /**
   * Generic file copy method
   *
   * @param is Input
   * @param os Output
   * @return True if copy is done, false otherwise
   */
  public static boolean copyFile(InputStream is, OutputStream os) {
    try {
      IOUtils.copy(is, os);
      return true;
    } catch (IOException e) {
      LogDelegate.e("Error copying file", e);
      return false;
    }
  }


  public static boolean deleteExternalStoragePrivateFile(Context mContext, String name) {
    // Checks for external storage availability
    if (!checkStorage()) {
      Toast.makeText(mContext, mContext.getString(R.string.storage_not_available),
          Toast.LENGTH_SHORT).show();
      return false;
    }
    File file = new File(mContext.getExternalFilesDir(null), name);
    return file.delete();
  }


  public static boolean delete(Context mContext, String path) {
    if (!checkStorage()) {
      Toast.makeText(mContext, mContext.getString(R.string.storage_not_available),
          Toast.LENGTH_SHORT).show();
      return false;
    }
    try {
      FileUtils.forceDelete(new File(path));
    } catch (IOException e) {
      LogDelegate.e("Can't delete '" + path + "': " + e.getMessage());
      return false;
    }
    return true;
  }


  public static String getRealPathFromURI(Context mContext, Uri contentUri) {
    String[] proj = {MediaStore.Images.Media.DATA};
    Cursor cursor = mContext.getContentResolver().query(contentUri, proj, null, null, null);
    if (cursor == null) {
      return null;
    }
    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
    cursor.moveToFirst();
    String path = cursor.getString(column_index);
    cursor.close();
    return path;
  }


  public static File createNewAttachmentFile(Context mContext, String extension) {
    File f = null;
    if (checkStorage()) {
      f = new File(mContext.getExternalFilesDir(null), createNewAttachmentName(extension));
    }
    return f;
  }


  private static synchronized String createNewAttachmentName(String extension) {
    Calendar now = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_SORTABLE);
    String name = sdf.format(now.getTime());
    name += extension != null ? extension : "";
    return name;
  }


  public static File createNewAttachmentFile(Context mContext) {
    return createNewAttachmentFile(mContext, null);
  }

  /**
   * Create a path where we will place our private file on external
   */
  public static File copyToBackupDir(File backupDir, String fileName, InputStream fileInputStream) {
    if (!checkStorage()) {
      return null;
    }
    if (!backupDir.exists()) {
      backupDir.mkdirs();
    }
    File destination = new File(backupDir, fileName);
    try {
      copyFile(fileInputStream, new FileOutputStream(destination));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return destination;
  }


  public static File getCacheDir(Context mContext) {
    File dir = mContext.getExternalCacheDir();
    if (!dir.exists()) {
      dir.mkdirs();
    }
    return dir;
  }

  public static File getOrCreateExternalStoragePublicDir() {
    File dir = getExternalStoragePublicDir();
    if (!dir.exists() && !dir.mkdirs()) {
        throw new ExternalDirectoryCreationException("Can't create folder " + dir.getAbsolutePath());
    }
    return dir;
  }

  public static File getExternalStoragePublicDir() {
    return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        + File.separator + Constants.EXTERNAL_STORAGE_FOLDER + File.separator);
  }

  public static File getOrCreateBackupDir(String backupName) {
    File backupDir = new File(getOrCreateExternalStoragePublicDir(), backupName);
    if (!backupDir.exists() && backupDir.mkdirs()) {
      createNoMediaFile(backupDir);
    }
    return backupDir;
  }


  private static void createNoMediaFile(File folder) {
    try {
      boolean created = new File(folder, ".nomedia").createNewFile();
      if (!created) {
        LogDelegate.w("File .nomedia already existing into " + folder.getAbsolutePath());
      }
    } catch (IOException e) {
      LogDelegate.e("Error creating .nomedia file into backup folder");
    }
  }


  public static File getSharedPreferencesFile(Context mContext) {
    File appData = mContext.getFilesDir().getParentFile();
    String packageName = mContext.getApplicationContext().getPackageName();
    return new File(appData
        + System.getProperty("file.separator")
        + "shared_prefs"
        + System.getProperty("file.separator")
        + packageName
        + "_preferences.xml");
  }


  /**
   * Returns a directory size in bytes
   */
  public static long getSize(File directory) {
    StatFs statFs = new StatFs(directory.getAbsolutePath());
    long blockSize = 0;
    try {
      blockSize = statFs.getBlockSizeLong();
    } catch (NoSuchMethodError e) {
      LogDelegate.e("Mysterious error", e);
    }
    return getSize(directory, blockSize);
  }


  private static long getSize(File directory, long blockSize) {
    if (blockSize == 0) {
      throw new InvalidParameterException("Blocksize can't be 0");
    }
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

  /**
   * Retrieves uri mime-type using ContentResolver
   */
  public static String getMimeType(Context mContext, Uri uri) {
    ContentResolver cR = mContext.getContentResolver();
    String mimeType = cR.getType(uri);
    if (mimeType == null) {
      mimeType = getMimeType(uri.toString());
    }
    return mimeType;
  }


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
   */
  public static String getMimeTypeInternal(Context mContext, Uri uri) {
    String mimeType = getMimeType(mContext, uri);
    return getMimeTypeInternal(mimeType);
  }


  /**
   * Retrieves mime-type between the ones managed by application from given string
   */
  public static String getMimeTypeInternal(String mimeType) {
    if (mimeType != null) {
      if (mimeType.contains("image/")) {
        mimeType = MIME_TYPE_IMAGE;
      } else if (mimeType.contains("audio/")) {
        mimeType = MIME_TYPE_AUDIO;
      } else if (mimeType.contains("video/")) {
        mimeType = MIME_TYPE_VIDEO;
      } else {
        mimeType = MIME_TYPE_FILES;
      }
    }
    return mimeType;
  }


  /**
   * Creates a new attachment file copying data from source file
   */
  public static @Nullable Attachment createAttachmentFromUri(Context mContext, Uri uri) {
    return createAttachmentFromUri(mContext, uri, false);
  }


  /**
   * Creates a file to be used as attachment.
   */
  public static @Nullable Attachment createAttachmentFromUri(Context mContext, Uri uri, boolean moveSource) {
    String name = FileHelper.getNameFromUri(mContext, uri);
    String extension = FileHelper.getFileExtension(FileHelper.getNameFromUri(mContext, uri))
        .toLowerCase(Locale.getDefault());
    File f;
    if (moveSource) {
      f = createNewAttachmentFile(mContext, extension);
      try {
        FileUtils.moveFile(new File(uri.getPath()), f);
      } catch (IOException e) {
        LogDelegate.e("Can't move file " + uri.getPath());
      }
    } else {
      f = StorageHelper.createExternalStoragePrivateFile(mContext, uri, extension);
    }
    Attachment mAttachment = null;
    if (f != null) {
      mAttachment = new Attachment(Uri.fromFile(f),
          StorageHelper.getMimeTypeInternal(mContext, uri));
      mAttachment.setName(name);
      mAttachment.setSize(f.length());
    }
    return mAttachment;
  }


  /**
   * Creates new attachment from web content
   */
  public static File createNewAttachmentFileFromHttp(Context mContext, String url)
      throws IOException {
    if (TextUtils.isEmpty(url)) {
      return null;
    }
    return getFromHttp(url, createNewAttachmentFile(mContext, FileHelper.getFileExtension(url)));
  }


  /**
   * Retrieves a file from its web url
   */
  public static File getFromHttp(String url, File file) throws IOException {
    URL imageUrl = new URL(url);
    FileUtils.copyURLToFile(imageUrl, file);
    return file;
  }

}
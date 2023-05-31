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

package it.feio.android.omninotes.helpers;


import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
import static it.feio.android.omninotes.OmniNotes.getAppContext;
import static it.feio.android.omninotes.utils.ConstantsBase.DATABASE_NAME;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_BACKUP_FOLDER_URI;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_PASSWORD;

import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.lazygeniouz.dfc.file.DocumentFileCompat;
import com.pixplicity.easyprefs.library.Prefs;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.async.DataBackupIntentService;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.exceptions.checked.BackupAttachmentException;
import it.feio.android.omninotes.helpers.notifications.NotificationsHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.Security;
import it.feio.android.omninotes.utils.StorageHelper;
import it.feio.android.omninotes.utils.TextHelper;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import rx.Observable;

@UtilityClass
public final class BackupHelper {

  public static void exportNotes(DocumentFileCompat backupDir) {
    for (Note note : DbHelper.getInstance(true).getAllNotes(false)) {
      exportNote(backupDir, note);
    }
  }

  public static void exportNote(DocumentFileCompat backupDir, Note note) {
    if (Boolean.TRUE.equals(note.isLocked())) {
      note.setContent(Security.encrypt(note.getContent(), Prefs.getString(PREF_PASSWORD, "")));
    }
    var noteFile = getBackupNoteFile(backupDir, note);
    try {
      DocumentFileHelper.write(getAppContext(), noteFile, note.toJSON());
    } catch (IOException e) {
      LogDelegate.e(String.format("Error on note %s backup: %s",  note.get_id(), e.getMessage()));
    }
  }

  @NonNull
  public static DocumentFileCompat getBackupNoteFile(DocumentFileCompat backupDir, Note note) {
    String backupFileMimetype = "application/json";
    String backupFileExtension = MimeTypeMap.getSingleton().hasMimeType(backupFileMimetype) ? "" : ".json";
    return backupDir.createFile(backupFileMimetype, note.get_id() + backupFileExtension);
  }

  /**
   * Export attachments to backup folder notifying for each attachment copied
   */
  public static void exportAttachments(DocumentFileCompat backupDir, NotificationsHelper notificationsHelper) {
    DocumentFileCompat attachmentsDestinationDir = backupDir.createDirectory(StorageHelper.getAttachmentDir().getName());
    List<Attachment> list = DbHelper.getInstance().getAllAttachments();
    exportAttachments(notificationsHelper, attachmentsDestinationDir, list, null);
  }

  public static boolean exportAttachments(NotificationsHelper notificationsHelper,
      DocumentFileCompat destinationattachmentsDir, List<Attachment> list, List<Attachment> listOld) {
    boolean result = true;
    listOld = listOld == null ? Collections.emptyList() : listOld;
    int exported = 0;
    int failed = 0;
    String failedString = "";

    for (Attachment attachment : list) {
      try {
        exportAttachment(destinationattachmentsDir, attachment);
        ++exported;
      } catch (BackupAttachmentException e) {
        ++failed;
        result = false;
        failedString = " (" + failed + " " + getAppContext().getString(R.string.failed) + ")";
      }

      notifyAttachmentBackup(notificationsHelper, list, exported, failedString);
    }

    Observable.from(listOld)
        .filter(attachment -> !list.contains(attachment))
        .forEach(attachment -> destinationattachmentsDir.findFile(
            attachment.getUri().getLastPathSegment()).delete());

    return result;
  }

  private static void notifyAttachmentBackup(NotificationsHelper notificationsHelper,
      List<Attachment> list, int exported, String failedString) {
    if (notificationsHelper != null) {
      String notificationMessage =
          TextHelper.capitalize(getAppContext().getString(R.string.attachment)) + " "
              + exported + "/" + list.size() + failedString;
      notificationsHelper.updateMessage(notificationMessage);
    }
  }

  private static void exportAttachment(DocumentFileCompat attachmentsDestination, Attachment attachment)
      throws BackupAttachmentException {
    try {
      var destinationAttachment = attachmentsDestination.createFile("",
          attachment.getUri().getLastPathSegment());
      DocumentFileHelper.copyFileTo(getAppContext(), new File(attachment.getUri().getPath()),
          destinationAttachment);
    } catch (Exception e) {
      LogDelegate.e("Error during attachment backup: " + attachment.getUriPath(), e);
      throw new BackupAttachmentException(e);
    }
  }

  public static List<Note> importNotes(DocumentFileCompat backupDir) {
    return Observable.from(backupDir.listFiles())
        .filter(f -> f.getName().matches("\\d{13}.json"))
        .map(BackupHelper::importNote)
        .filter(n -> n != null)
        .toList().toBlocking().single();
  }

  @Nullable
  public static Note importNote(DocumentFileCompat file) {
    Note note = getImportNote(file);

    if (Boolean.TRUE.equals(note.isLocked())) {
      if (StringUtils.isEmpty(Prefs.getString(PREF_PASSWORD, ""))) {
        return null;
      }
      note.setContent(Security.decrypt(note.getContent(), Prefs.getString(PREF_PASSWORD, "")));
    }

    if (note.getCategory() != null) {
      DbHelper.getInstance().updateCategory(note.getCategory());
    }
    DbHelper.getInstance().updateNote(note, false);
    return note;
  }

  public static Note getImportNote(DocumentFileCompat file) {
    try {
      Note note = new Note();
      String jsonString = DocumentFileHelper.readContent(getAppContext(), file);
      if (!TextUtils.isEmpty(jsonString)) {
        note.buildFromJson(jsonString);
      }
      return note;
    } catch (IOException e) {
      LogDelegate.e("Error parsing note json");
      return new Note();
    }
  }

  /**
   * Import attachments from backup folder notifying for each imported item
   */
  public static boolean importAttachments(DocumentFileCompat backupDir, NotificationsHelper notificationsHelper) {
    AtomicBoolean result = new AtomicBoolean(true);
    File attachmentsDir = StorageHelper.getAttachmentDir();
    var backupAttachmentsDir = backupDir.findFile(attachmentsDir.getName());
    if (!backupAttachmentsDir.exists()) {
      return false;
    }

    AtomicInteger imported = new AtomicInteger();
    ArrayList<Attachment> attachments = DbHelper.getInstance().getAllAttachments();
    var BackupedAttachments = backupAttachmentsDir.listFiles();
    rx.Observable.from(attachments)
        .forEach(attachment -> {
          try {
            importAttachment(BackupedAttachments, attachmentsDir, attachment);
            if (notificationsHelper != null) {
              notificationsHelper.updateMessage(TextHelper.capitalize(getAppContext().getString(R.string.attachment)) + " "
                      + imported.incrementAndGet() + "/" + attachments.size());
            }
          } catch (BackupAttachmentException e) {
            result.set(false);
          }
        });
    return result.get();
  }

  static void importAttachment(List<DocumentFileCompat> backupedAttachments, File attachmentsDir,
      Attachment attachment) throws BackupAttachmentException {
    String attachmentName = attachment.getUri().getLastPathSegment();
    try {
      File destinationAttachment = new File(attachmentsDir, attachmentName);
      var backupedAttachment = Observable.from(backupedAttachments)
          .filter(ba -> attachmentName.equals(ba.getName())).toBlocking().single();
      DocumentFileHelper.copyFileTo(getAppContext(), backupedAttachment, destinationAttachment);
    } catch (Exception e) {
      LogDelegate.e("Error importing the attachment " + attachment.getUri().getPath(), e);
      throw new BackupAttachmentException(e);
    }
  }

  /**
   * Starts backup service
   *
   * @param backupFolderName subfolder of the app's external sd folder where notes will be stored
   */
  public static void startBackupService(String backupFolderName) {
    Intent service = new Intent(getAppContext(), DataBackupIntentService.class);
    service.setAction(DataBackupIntentService.ACTION_DATA_EXPORT);
    service.putExtra(DataBackupIntentService.INTENT_BACKUP_NAME, backupFolderName);
    getAppContext().startService(service);
  }

  public static void deleteNote(File file) {
    try {
      Note note = new Note();
      note.buildFromJson(FileUtils.readFileToString(file));
      DbHelper.getInstance().deleteNote(note);
    } catch (IOException e) {
      LogDelegate.e("Error parsing note json");
    }
  }

  /**
   * Import database from backup folder. Used ONLY to restore legacy backup
   *
   * @deprecated {@link BackupHelper#importNotes(DocumentFileCompat)}
   */
  @Deprecated(forRemoval = true)
  public static void importDB(Context context, File backupDir) throws IOException {
    File database = context.getDatabasePath(DATABASE_NAME);
    if (database.exists() && database.delete()) {
      StorageHelper.copyFile(new File(backupDir, DATABASE_NAME), database, true);
    }
  }

  public static DocumentFileCompat saveScopedStorageUriInPreferences(Intent intent) {
    var context = getAppContext();
    final int takeFlags = FLAG_GRANT_READ_URI_PERMISSION & FLAG_GRANT_WRITE_URI_PERMISSION;
    context.getContentResolver().takePersistableUriPermission(intent.getData(), takeFlags);

    var currentlySelected = DocumentFileCompat.Companion.fromTreeUri(getAppContext(),
        intent.getData());

    // Selected a folder already name "Omni Notes" (ex. from previous backups)
    if(Constants.EXTERNAL_STORAGE_FOLDER.equals(currentlySelected.getName())) {
      Prefs.putString(PREF_BACKUP_FOLDER_URI, currentlySelected.getUri().toString());
      return currentlySelected;
    } else {
      var childFolder = currentlySelected.findFile(Constants.EXTERNAL_STORAGE_FOLDER);
      if (childFolder == null || !childFolder.isDirectory()) {
        childFolder = DocumentFileCompat.Companion.fromTreeUri(context, intent.getData())
            .createDirectory(Constants.EXTERNAL_STORAGE_FOLDER);
      }
      Prefs.putString(PREF_BACKUP_FOLDER_URI, childFolder.getUri().toString());
      return childFolder;
    }
  }

//  public static List<LinkedList<DiffMatchPatch.Diff>> integrityCheck(File backupDir) {
//    List<LinkedList<DiffMatchPatch.Diff>> errors = new ArrayList<>();
//    for (Note note : DbHelper.getInstance(true).getAllNotes(false)) {
//      File noteFile = getBackupNoteFile(backupDir, note);
//      try {
//        String noteString = note.toJSON();
//        String noteFileString = FileUtils.readFileToString(noteFile);
//        if (noteString.equals(noteFileString)) {
//          File backupAttachmentsDir = new File(backupDir,
//              StorageHelper.getAttachmentDir().getName());
//          for (Attachment attachment : note.getAttachmentsList()) {
//            if (!new File(backupAttachmentsDir, FilenameUtils.getName(attachment.getUriPath()))
//                .exists()) {
//              addIntegrityCheckError(errors, new FileNotFoundException("Attachment " + attachment
//                  .getUriPath() + " missing"));
//            }
//          }
//        } else {
//          errors.add(new DiffMatchPatch().diffMain(noteString, noteFileString));
//        }
//      } catch (IOException e) {
//        LogDelegate.e(e.getMessage(), e);
//        addIntegrityCheckError(errors, e);
//      }
//    }
//    return errors;
//  }

  public static String getBackupFolderPath() {
    if (VERSION.SDK_INT >= VERSION_CODES.O) {
      var backupFolder = Prefs.getString(PREF_BACKUP_FOLDER_URI, "");
      var paths =URI.create(backupFolder).getPath().split(":");
      return paths[paths.length - 1];
    } else {
      return StorageHelper.getExternalStoragePublicDir().getAbsolutePath();
    }
  }

  private static void addIntegrityCheckError(List<LinkedList<DiffMatchPatch.Diff>> errors,
      IOException e) {
    LinkedList<DiffMatchPatch.Diff> l = new LinkedList<>();
    l.add(new DiffMatchPatch.Diff(DiffMatchPatch.Operation.DELETE, e.getMessage()));
    errors.add(l);
  }

}

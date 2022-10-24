/*
 * Copyright (C) 2013-2020 Federico Iosue (federico@iosue.it)
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


import static it.feio.android.omninotes.utils.ConstantsBase.DATABASE_NAME;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_PASSWORD;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import com.pixplicity.easyprefs.library.Prefs;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.async.DataBackupIntentService;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.exceptions.BackupException;
import it.feio.android.omninotes.exceptions.checked.BackupAttachmentException;
import it.feio.android.omninotes.helpers.notifications.NotificationsHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Security;
import it.feio.android.omninotes.utils.StorageHelper;
import it.feio.android.omninotes.utils.TextHelper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import rx.Observable;

@UtilityClass
public final class BackupHelper {

  public static void exportNotes(DocumentFile backupDir) {
    for (Note note : DbHelper.getInstance(true).getAllNotes(false)) {
      exportNote(backupDir, note);
    }
  }

  public static void exportNote(DocumentFile backupDir, Note note) {
    if (Boolean.TRUE.equals(note.isLocked())) {
      note.setContent(Security.encrypt(note.getContent(), Prefs.getString(PREF_PASSWORD, "")));
    }
    DocumentFile noteFile = getBackupNoteFile(backupDir, note);
    try {
      DocumentFileHelper.write(OmniNotes.getAppContext(), noteFile, note.toJSON());
    } catch (IOException e) {
      LogDelegate.e(String.format("Error on note %s backup: %s",  note.get_id(), e.getMessage()));
    }
  }

  @NonNull
  public static DocumentFile getBackupNoteFile(DocumentFile backupDir, Note note) {
    return backupDir.createFile("application/json", String.valueOf(note.get_id()));
  }

  /**
   * Export attachments to backup folder notifying for each attachment copied
   */
  public static void exportAttachments(DocumentFile backupDir, NotificationsHelper notificationsHelper) {
    DocumentFile attachmentsDestinationDir = backupDir.createDirectory(StorageHelper.getAttachmentDir().getName());
    List<Attachment> list = DbHelper.getInstance().getAllAttachments();
    exportAttachments(notificationsHelper, attachmentsDestinationDir, list, null);
  }

  public static boolean exportAttachments(NotificationsHelper notificationsHelper,
      DocumentFile destinationattachmentsDir, List<Attachment> list, List<Attachment> listOld) {
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
        failedString = " (" + failed + " " + OmniNotes.getAppContext().getString(R.string.failed) + ")";
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
          TextHelper.capitalize(OmniNotes.getAppContext().getString(R.string.attachment)) + " "
              + exported + "/" + list.size() + failedString;
      notificationsHelper.updateMessage(notificationMessage);
    }
  }

  private static void exportAttachment(DocumentFile attachmentsDestination, Attachment attachment)
      throws BackupAttachmentException {
    try {
      DocumentFile destinationAttachment = attachmentsDestination.createFile(
          "", attachment.getUri().getLastPathSegment());
      DocumentFileHelper.copyFileTo(OmniNotes.getAppContext(), new File(attachment.getUri().getPath()),
          destinationAttachment);
    } catch (Exception e) {
      LogDelegate.e("Error during attachment backup: " + attachment.getUriPath(), e);
      throw new BackupAttachmentException(e);
    }
  }

  public static List<Note> importNotes(DocumentFile backupDir) {
    List<Note> notes = new ArrayList<>();
    for (DocumentFile file : Observable.from(backupDir.listFiles())
        .filter(f -> f.getName().matches("\\d{13}.json")).toList().toBlocking().single()) {
      notes.add(importNote(file));
    }
    return notes;
  }

  public static Note importNote(DocumentFile file) {
    Note note = getImportNote(file);
    if (note.getCategory() != null) {
      DbHelper.getInstance().updateCategory(note.getCategory());
    }
    if (Boolean.TRUE.equals(note.isLocked())) {
      note.setContent(Security.decrypt(note.getContent(), Prefs.getString(PREF_PASSWORD, "")));
    }
    DbHelper.getInstance().updateNote(note, false);
    return note;
  }

  public static Note getImportNote(DocumentFile file) {
    try {
      Note note = new Note();
      String jsonString = DocumentFileHelper.readContent(OmniNotes.getAppContext(), file);
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
  public static boolean importAttachments(DocumentFile backupDir, NotificationsHelper notificationsHelper) {
    AtomicBoolean result = new AtomicBoolean(true);
    File attachmentsDir = StorageHelper.getAttachmentDir();
    DocumentFile backupAttachmentsDir = backupDir.findFile(attachmentsDir.getName());
    if (!backupAttachmentsDir.exists()) {
      return false;
    }

    AtomicInteger imported = new AtomicInteger();
    ArrayList<Attachment> attachments = DbHelper.getInstance().getAllAttachments();
    rx.Observable.from(attachments)
        .forEach(attachment -> {
          try {
            importAttachment(backupAttachmentsDir, attachmentsDir, attachment);
            if (notificationsHelper != null) {
              notificationsHelper.updateMessage(TextHelper.capitalize(OmniNotes.getAppContext().getString(R.string.attachment)) + " "
                      + imported.incrementAndGet() + "/" + attachments.size());
            }
          } catch (BackupAttachmentException e) {
            result.set(false);
          }
        });
    return result.get();
  }

  static void importAttachment(DocumentFile backupAttachmentsDir, File attachmentsDir,
      Attachment attachment) throws BackupAttachmentException {
    String attachmentName = attachment.getUri().getLastPathSegment();
    try {
      File destinationAttachment = new File(attachmentsDir, attachmentName);
      DocumentFileHelper.copyFileTo(OmniNotes.getAppContext(),
          backupAttachmentsDir.findFile(attachmentName), destinationAttachment);
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
    Intent service = new Intent(OmniNotes.getAppContext(), DataBackupIntentService.class);
    service.setAction(DataBackupIntentService.ACTION_DATA_EXPORT);
    service.putExtra(DataBackupIntentService.INTENT_BACKUP_NAME, backupFolderName);
    OmniNotes.getAppContext().startService(service);
  }

  public static void exportSettings(DocumentFile backupDir) throws IOException {
    File preferences = StorageHelper.getSharedPreferencesFile(OmniNotes.getAppContext());
    var destinationSetting = backupDir.createFile("", preferences.getName());
    DocumentFileHelper.copyFileTo(OmniNotes.getAppContext(), preferences, destinationSetting);
  }

  public static void importSettings(DocumentFile backupDir) throws IOException {
    File preferences = StorageHelper.getSharedPreferencesFile(OmniNotes.getAppContext());
    DocumentFile preferenceBackup = backupDir.findFile(preferences.getName());
    try {
      StorageHelper.copyFile(OmniNotes.getAppContext(), preferenceBackup.getUri(),
          Uri.fromFile(preferences));
    } catch (Exception e) {
      throw new BackupException("Impossible to find settings file on " + backupDir.getName(), e);
    }
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
   * @deprecated {@link BackupHelper#importNotes(DocumentFile)}
   */
  @Deprecated
  public static void importDB(Context context, File backupDir) throws IOException {
    File database = context.getDatabasePath(DATABASE_NAME);
    if (database.exists() && database.delete()) {
      StorageHelper.copyFile(new File(backupDir, DATABASE_NAME), database, true);
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

  private static void addIntegrityCheckError(List<LinkedList<DiffMatchPatch.Diff>> errors,
      IOException e) {
    LinkedList<DiffMatchPatch.Diff> l = new LinkedList<>();
    l.add(new DiffMatchPatch.Diff(DiffMatchPatch.Operation.DELETE, e.getMessage()));
    errors.add(l);
  }

}

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

package it.feio.android.omninotes.async;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static it.feio.android.omninotes.helpers.IntentHelper.immutablePendingIntentFlag;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_RESTART_APP;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_BACKUP_FOLDER_URI;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import com.lazygeniouz.dfc.file.DocumentFileCompat;
import com.pixplicity.easyprefs.library.Prefs;
import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.helpers.BackupHelper;
import it.feio.android.omninotes.helpers.LogDelegate;
import it.feio.android.omninotes.helpers.SpringImportHelper;
import it.feio.android.omninotes.helpers.notifications.NotificationChannels.NotificationChannelNames;
import it.feio.android.omninotes.helpers.notifications.NotificationsHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.listeners.OnAttachingFileListener;
import it.feio.android.omninotes.utils.ReminderHelper;
import it.feio.android.omninotes.utils.StorageHelper;
import java.io.File;
import rx.Observable;

public class DataBackupIntentService extends IntentService implements OnAttachingFileListener {

  public static final String INTENT_BACKUP_NAME = "backup_name";
  public static final String ACTION_DATA_EXPORT = "action_data_export";
  public static final String ACTION_DATA_IMPORT = "action_data_import";
  public static final String ACTION_DATA_DELETE = "action_data_delete";

  private NotificationsHelper mNotificationsHelper;

//    {
//        File autoBackupDir = StorageHelper.getBackupDir(Constants.AUTO_BACKUP_DIR);
//        BackupHelper.exportNotes(autoBackupDir);
//        BackupHelper.exportAttachments(autoBackupDir);
//    }


  public DataBackupIntentService() {
    super("DataBackupIntentService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    mNotificationsHelper = new NotificationsHelper(this).start(NotificationChannelNames.BACKUPS,
        R.drawable.ic_content_save_white_24dp, getString(R.string.working));

    // If an alarm has been fired a notification must be generated
    if (ACTION_DATA_EXPORT.equals(intent.getAction())) {
      exportData(intent);
    } else if (ACTION_DATA_IMPORT.equals(intent.getAction())) {
      importData(intent);
    } else if (SpringImportHelper.ACTION_DATA_IMPORT_SPRINGPAD.equals(intent.getAction())) {
      importDataFromSpringpad(intent, mNotificationsHelper);
    } else if (ACTION_DATA_DELETE.equals(intent.getAction())) {
      deleteData(intent);
    }
  }

  private void importDataFromSpringpad(Intent intent, NotificationsHelper mNotificationsHelper) {
    new SpringImportHelper(OmniNotes.getAppContext())
        .importDataFromSpringpad(intent, mNotificationsHelper);
    String title = getString(R.string.data_import_completed);
    String text = getString(R.string.click_to_refresh_application);
    createNotification(intent, this, title, text);
  }

  private void exportData(Intent intent) {
    String backupName = intent.getStringExtra(INTENT_BACKUP_NAME);
    var backupDir = DocumentFileCompat.Companion.fromTreeUri(getBaseContext(),
        Uri.parse(Prefs.getString(PREF_BACKUP_FOLDER_URI, null))).createDirectory(backupName);

    BackupHelper.exportNotes(backupDir);
    BackupHelper.exportAttachments(backupDir, mNotificationsHelper);

    var readableBackupFolder = BackupHelper.getBackupFolderPath() + "/" + backupName;
    mNotificationsHelper.finish(getString(R.string.data_export_completed), readableBackupFolder);
  }

  @TargetApi(VERSION_CODES.O)
  private synchronized void importData(Intent intent) {
    var backupDir = Observable.from(DocumentFileCompat.Companion.fromTreeUri(getBaseContext(),
            Uri.parse(Prefs.getString(PREF_BACKUP_FOLDER_URI, null))).listFiles())
        .filter(f -> f.getName().equals(intent.getStringExtra(INTENT_BACKUP_NAME))).toBlocking()
        .single();

    BackupHelper.importNotes(backupDir);
    BackupHelper.importAttachments(backupDir, mNotificationsHelper);

    resetReminders();
    mNotificationsHelper.cancel();

    createNotification(intent, this, getString(R.string.data_import_completed),
        getString(R.string.click_to_refresh_application));

    // Performs auto-backup filling after backup restore
//        if (Prefs.getBoolean(Constants.PREF_ENABLE_AUTOBACKUP, false)) {
//            File autoBackupDir = StorageHelper.getBackupDir(Constants.AUTO_BACKUP_DIR);
//            BackupHelper.exportNotes(autoBackupDir);
//            BackupHelper.exportAttachments(autoBackupDir);
//        }
  }

  private synchronized void deleteData(Intent intent) {
    String backupName = intent.getStringExtra(INTENT_BACKUP_NAME);
    File backupDir = StorageHelper.getOrCreateBackupDir(backupName);

    StorageHelper.delete(this, backupDir.getAbsolutePath());

    mNotificationsHelper.finish(getString(R.string.data_deletion_completed), backupName + " " + getString(R.string.deleted));
  }

  private void createNotification(Intent intent, Context context, String title, String message) {
    Intent intentLaunch;
    if (DataBackupIntentService.ACTION_DATA_IMPORT.equals(intent.getAction())
        || SpringImportHelper.ACTION_DATA_IMPORT_SPRINGPAD.equals(intent.getAction())) {
      intentLaunch = new Intent(context, MainActivity.class);
      intentLaunch.setAction(ACTION_RESTART_APP);
    } else {
      intentLaunch = new Intent();
    }
    // Add this bundle to the intent
    intentLaunch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intentLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    PendingIntent notifyIntent = PendingIntent.getActivity(context, 0, intentLaunch,
        immutablePendingIntentFlag(FLAG_UPDATE_CURRENT));

    NotificationsHelper notificationsHelper = new NotificationsHelper(context);
    notificationsHelper.createStandardNotification(NotificationChannelNames.BACKUPS,
        R.drawable.ic_content_save_white_24dp, title, notifyIntent)
        .setMessage(message).setRingtone(Prefs.getString("settings_notification_ringtone", null))
        .setLedActive();
    if (Prefs.getBoolean("settings_notification_vibration", true)) {
      notificationsHelper.setVibration();
    }
    notificationsHelper.show();
  }


  /**
   * Schedules reminders
   */
  private void resetReminders() {
    LogDelegate.d("Resetting reminders");
    for (Note note : DbHelper.getInstance().getNotesWithReminderNotFired()) {
      ReminderHelper.addReminder(OmniNotes.getAppContext(), note);
    }
  }


  @Override
  public void onAttachingFileErrorOccurred(Attachment mAttachment) {
    // TODO Auto-generated method stub
  }


  @Override
  public void onAttachingFileFinished(Attachment mAttachment) {
    // TODO Auto-generated method stub
  }

}

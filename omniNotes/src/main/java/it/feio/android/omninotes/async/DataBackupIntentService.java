/*
 * Copyright (C) 2015 Federico Iosue (federico.iosue@gmail.com)
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

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import exceptions.ImportException;
import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.helpers.BackupHelper;
import it.feio.android.omninotes.helpers.SpringImportHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.listeners.OnAttachingFileListener;
import it.feio.android.omninotes.utils.*;
import it.feio.android.springpadimporter.Importer;
import it.feio.android.springpadimporter.models.SpringpadAttachment;
import it.feio.android.springpadimporter.models.SpringpadComment;
import it.feio.android.springpadimporter.models.SpringpadElement;
import it.feio.android.springpadimporter.models.SpringpadItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;


public class DataBackupIntentService extends IntentService implements OnAttachingFileListener {

    public final static String INTENT_BACKUP_NAME = "backup_name";
    public final static String INTENT_BACKUP_INCLUDE_SETTINGS = "backup_include_settings";
    public final static String ACTION_DATA_EXPORT = "action_data_export";
    public final static String ACTION_DATA_IMPORT = "action_data_import";
    public final static String ACTION_DATA_DELETE = "action_data_delete";

    private SharedPreferences prefs;
    private NotificationsHelper mNotificationsHelper;


    public DataBackupIntentService() {
        super("DataBackupIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);

        // Creates an indeterminate processing notification until the work is complete
        mNotificationsHelper = new NotificationsHelper(this)
                .createNotification(R.drawable.ic_content_save_white_24dp, getString(R.string.working), null)
                .setIndeterminate().setOngoing().show();

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
		new SpringImportHelper(OmniNotes.getAppContext()).importDataFromSpringpad(intent, mNotificationsHelper);
		String title = getString(R.string.data_import_completed);
		String text = getString(R.string.click_to_refresh_application);
		createNotification(intent, this, title, text, null);
	}


	synchronized private void exportData(Intent intent) {

        // Gets backup folder
        String backupName = intent.getStringExtra(INTENT_BACKUP_NAME);
        File backupDir = StorageHelper.getBackupDir(backupName);

        // Directory clean in case of previously used backup name
        StorageHelper.delete(this, backupDir.getAbsolutePath());

        // Directory is re-created in case of previously used backup name (removed above)
        backupDir = StorageHelper.getBackupDir(backupName);

        // Database backup
//        exportDB(backupDir);
		BackupHelper.exportNotes(backupDir);

        // Attachments backup
		BackupHelper.exportAttachments(backupDir, mNotificationsHelper);

        // Settings
        if (intent.getBooleanExtra(INTENT_BACKUP_INCLUDE_SETTINGS, true)) {
			BackupHelper.exportSettings(backupDir);
        }

        // Notification of operation ended
        String title = getString(R.string.data_export_completed);
        String text = backupDir.getPath();
        createNotification(intent, this, title, text, backupDir);
    }


    synchronized private void importData(Intent intent) {

        // Gets backup folder
        String backupName = intent.getStringExtra(INTENT_BACKUP_NAME);
        File backupDir = StorageHelper.getBackupDir(backupName);

        // Database backup
//        importDB(backupDir);
		BackupHelper.importNotes(backupDir);

        // Attachments backup
		BackupHelper.importAttachments(backupDir, mNotificationsHelper);

		// Settings restore
		BackupHelper.importSettings(backupDir);

		// Reminders restore
		resetReminders();

        String title = getString(R.string.data_import_completed);
        String text = getString(R.string.click_to_refresh_application);
        createNotification(intent, this, title, text, backupDir);

		// Performs auto-backup filling after backup restore
		File autoBackupDir = StorageHelper.getBackupDir(Constants.AUTO_BACKUP_DIR);
		BackupHelper.exportNotes(autoBackupDir);
		BackupHelper.exportAttachments(autoBackupDir);
    }


    synchronized private void deleteData(Intent intent) {

        // Gets backup folder
        String backupName = intent.getStringExtra(INTENT_BACKUP_NAME);
        File backupDir = StorageHelper.getBackupDir(backupName);

        // Backup directory removal
        StorageHelper.delete(this, backupDir.getAbsolutePath());

        String title = getString(R.string.data_deletion_completed);
        String text = backupName + " " + getString(R.string.deleted);
        createNotification(intent, this, title, text, backupDir);
    }


    /**
     * Creation of notification on operations completed
     */
    private void createNotification(Intent intent, Context mContext, String title, String message, File backupDir) {

        // The behavior differs depending on intent action
        Intent intentLaunch;
        if (DataBackupIntentService.ACTION_DATA_IMPORT.equals(intent.getAction())
                || SpringImportHelper.ACTION_DATA_IMPORT_SPRINGPAD.equals(intent.getAction())) {
			intentLaunch = new Intent(mContext, MainActivity.class);
			intentLaunch.setAction(Constants.ACTION_RESTART_APP);
        } else {
            intentLaunch = new Intent();
        }
        // Add this bundle to the intent
        intentLaunch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Creates the PendingIntent
        PendingIntent notifyIntent = PendingIntent.getActivity(mContext, 0, intentLaunch,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationsHelper mNotificationsHelper = new NotificationsHelper(mContext);
        mNotificationsHelper.createNotification(R.drawable.ic_content_save_white_24dp, title, notifyIntent)
                .setMessage(message).setRingtone(prefs.getString("settings_notification_ringtone", null))
                .setLedActive();
        if (prefs.getBoolean("settings_notification_vibration", true)) mNotificationsHelper.setVibration();
        mNotificationsHelper.show();
    }


    /**
     * Export database to backup folder
     *
     * @return True if success, false otherwise
	 * @deprecated
	 * {@link BackupHelper#exportNotes(File)}
	 */
     @Deprecated
    private boolean exportDB(File backupDir) {
        File database = getDatabasePath(Constants.DATABASE_NAME);
        return (StorageHelper.copyFile(database, new File(backupDir, Constants.DATABASE_NAME)));
    }


	/**
	 * Schedules reminders
	 */
	private void resetReminders() {
		Log.d(Constants.TAG, "Resettings reminders");
		for (Note note : DbHelper.getInstance().getNotesWithReminderNotFired()) {
			ReminderHelper.addReminder(OmniNotes.getAppContext(), note);
		}
	}


    /**
     * Import database from backup folder
	 * @deprecated
	 * {@link BackupHelper#importNotes(File)}
     */
	@Deprecated
    private boolean importDB(File backupDir) {
        File database = getDatabasePath(Constants.DATABASE_NAME);
        if (database.exists()) {
            database.delete();
        }
        return (StorageHelper.copyFile(new File(backupDir, Constants.DATABASE_NAME), database));
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

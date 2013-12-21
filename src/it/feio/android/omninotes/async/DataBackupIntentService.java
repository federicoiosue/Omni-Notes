package it.feio.android.omninotes.async;

import java.io.File;
import java.io.FileInputStream;

import it.feio.android.omninotes.ListActivity;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.StorageManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

public class DataBackupIntentService extends IntentService {

	public DataBackupIntentService() {
		super("DataBackupIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// PowerManager pm = (PowerManager)
		// getSystemService(Context.POWER_SERVICE);
		// PowerManager.WakeLock wl =
		// pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Constants.TAG);
		// // Acquire the lock
		// wl.acquire();

		// If an alarm has been fired a notification must be generated
		if (Constants.ACTION_DATA_EXPORT.equals(intent.getAction())) {
			exportData(intent);
		} else if (Constants.ACTION_DATA_IMPORT.equals(intent.getAction())) {
			importData(intent);
		}

		// Release the lock
		// Log.d(Constants.TAG, "Releasing power lock, all done");
		// wl.release();

	}

	
	synchronized private void exportData(Intent intent) {
		boolean res = true;
		
		// Gets backup folder
		String backupName = intent.getStringExtra(Constants.INTENT_BACKUP_NAME);
		File backupDir = StorageManager.getBackupDir(backupName);
		
		// Database backup
		res = res && exportDB(backupDir);
		
		// Attachments backup
		res = res && exportAttachments(backupDir);		
		
		// Notification of operation ended
		String title = getString(R.string.data_export_completed);
		String text = backupDir.getPath();
		createNotification(this, title, text);
	}

	
	synchronized private void importData(Intent intent) {
		boolean res = true;
		
		// Gets backup folder
		String backupName = intent.getStringExtra(Constants.INTENT_BACKUP_NAME);
		File backupDir = StorageManager.getBackupDir(backupName);
		
		// Database backup
		res = res && importDB(backupDir);
		
		// Attachments backup
		res = res && importAttachments(backupDir);	
		
		String title = getString(R.string.data_import_completed);
		String text = getString(R.string.click_to_refresh_application);
		createNotification(this, title, text);
	}

	
	
	/**
	 * Creation of notification on operations completed
	 * @param ctx
	 * @param message
	 */
	private void createNotification(Context ctx, String title, String message) {

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx)
				.setSmallIcon(R.drawable.ic_stat_notification_icon)
				.setContentTitle(title)
				.setContentText(message)
				.setAutoCancel(true);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		// Impostazione suoneria
		if (prefs.getBoolean("settings_notification_sound", true))
			mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

		// Impostazione vibrazione
		long[] pattern = { 500, 500 };
		if (prefs.getBoolean("settings_notification_vibration", true))
			mBuilder.setVibrate(pattern);

		Intent intent = new Intent(ctx, ListActivity.class);
		// Add this bundle to the intent
		// Sets the Activity to start in a new, empty task
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

		// Creates the PendingIntent
		PendingIntent notifyIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Puts the PendingIntent into the notification builder
		mBuilder.setContentIntent(notifyIntent);
		// Notifications are issued by sending them to the
		// NotificationManager system service.
		NotificationManager mNotificationManager = (NotificationManager) ctx
				.getSystemService(Context.NOTIFICATION_SERVICE);
		// Builds an anonymous Notification object from the builder, and
		// passes it to the NotificationManager
		mNotificationManager.notify(0, mBuilder.build());
	}

	
	/**
	 * Export database to backup folder
	 * 
	 * @param backupDir
	 * @return True if success, false otherwise
	 */
	private boolean exportDB(File backupDir) {
		File database = getDatabasePath(Constants.DATABASE_NAME);
		return (StorageManager.copyFile(database, new File(backupDir, Constants.DATABASE_NAME)) );
	}

	
	/**
	 * Export attachments to backup folder
	 * 
	 * @param backupDir
	 * @return True if success, false otherwise
	 */
	private boolean exportAttachments(File backupDir) {
		File attachmentsDir = StorageManager.getAttachmentDir(this);
		File destinationattachmentsDir = new File(backupDir, attachmentsDir.getName());
		return (StorageManager.copyDirectory(attachmentsDir, destinationattachmentsDir));
	}

	
	/**
	 * Import database from backup folder
	 * 
	 * @param backupDir
	 * @return True if success, false otherwise
	 */
	private boolean importDB(File backupDir) {
		File database = getDatabasePath(Constants.DATABASE_NAME);
		if (database.exists()) {
			database.delete();
		}
		return (StorageManager.copyFile(new File(backupDir, Constants.DATABASE_NAME), database));
	}

	
	/**
	 * Import attachments from backup folder
	 * 
	 * @param backupDir
	 * @return True if success, false otherwise
	 */
	private boolean importAttachments(File backupDir) {
		File attachmentsDir = StorageManager.getAttachmentDir(this);
		File backupAttachmentsDir = new File(backupDir, attachmentsDir.getName());
		return (StorageManager.copyDirectory(backupAttachmentsDir, attachmentsDir));
	}

}

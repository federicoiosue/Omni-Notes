package it.feio.android.omninotes.async;

import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.listeners.OnAttachingFileListener;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.GeocodeHelper;
import it.feio.android.omninotes.utils.StorageManager;
import it.feio.android.omninotes.utils.date.DateHelper;
import it.feio.android.springpadimporter.Importer;
import it.feio.android.springpadimporter.models.SpringpadAttachment;
import it.feio.android.springpadimporter.models.SpringpadItem;
import it.feio.android.springpadimporter.models.SpringpadElement;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

public class DataBackupIntentService extends IntentService implements OnAttachingFileListener {

	public final static String INTENT_BACKUP_NAME = "backup_name";
	public final static String INTENT_BACKUP_INCLUDE_SETTINGS = "backup_include_settings";
	public final static String ACTION_DATA_EXPORT = "action_data_export";
	public final static String ACTION_DATA_IMPORT = "action_data_import";
	public final static String ACTION_DATA_IMPORT_SPRINGPAD = "action_data_import_springpad";
	public final static String ACTION_DATA_DELETE = "action_data_delete";
	public final static String EXTRA_SPRINGPAD_BACKUP = "extra_springpad_backup";

	private SharedPreferences prefs;


	public DataBackupIntentService() {
		super("DataBackupIntentService");
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		// PowerManager pm = (PowerManager)
		// getSystemService(Context.POWER_SERVICE);
		// PowerManager.WakeLock wl =
		// pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		// // Acquire the lock
		// wl.acquire();

		prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);

		// If an alarm has been fired a notification must be generated
		if (ACTION_DATA_EXPORT.equals(intent.getAction())) {
			exportData(intent);
		} else if (ACTION_DATA_IMPORT.equals(intent.getAction())) {
			importData(intent);
		} else if (ACTION_DATA_IMPORT_SPRINGPAD.equals(intent.getAction())) {
			importDataFromSpringpad(intent);
		} else if (ACTION_DATA_DELETE.equals(intent.getAction())) {
			deleteData(intent);
		}

		// Release the lock
		// Log.d(TAG, "Releasing power lock, all done");
		// wl.release();

	}


	synchronized private void exportData(Intent intent) {

		// Gets backup folder
		String backupName = intent.getStringExtra(INTENT_BACKUP_NAME);
		File backupDir = StorageManager.getBackupDir(backupName);

		// Directory clean in case of previously used backup name
		StorageManager.delete(this, backupDir.getAbsolutePath());

		// Directory is re-created in case of previously used backup name (removed above)
		backupDir = StorageManager.getBackupDir(backupName);

		// Database backup
		exportDB(backupDir);

		// Attachments backup
		exportAttachments(backupDir);

		// Settings
		if (intent.getBooleanExtra(INTENT_BACKUP_INCLUDE_SETTINGS, true)) ;
		exportSettings(backupDir);

		// Notification of operation ended
		String title = getString(R.string.data_export_completed);
		String text = backupDir.getPath();
		createNotification(intent, this, title, text);
	}


	synchronized private void importData(Intent intent) {

		// Gets backup folder
		String backupName = intent.getStringExtra(INTENT_BACKUP_NAME);
		File backupDir = StorageManager.getBackupDir(backupName);

		// Database backup
		importDB(backupDir);

		// Attachments backup
		importAttachments(backupDir);

		// Settings restore
		importSettings(backupDir);

		String title = getString(R.string.data_import_completed);
		String text = getString(R.string.click_to_refresh_application);
		createNotification(intent, this, title, text);
	}


	synchronized private void importDataFromSpringpad(Intent intent) {

		// Backupped notes retrieval
		String backupPath = intent.getStringExtra(EXTRA_SPRINGPAD_BACKUP);
		Importer importer = new Importer();
		importer.doImport(backupPath);
		List<SpringpadElement> elements = importer.getSpringpadNotes();

		// If nothing is retrieved it will exit
		if (elements == null || elements.size() == 0) { return; }

//		 Otherwise a new category will be create to host notes
//		Category categoryDefault = new Category();
//		categoryDefault.setName("Springpad_" + DateHelper.getSortableDate());
//		categoryDefault.setColor(String.valueOf(Color.parseColor("#F9EA1B")));
//		DbHelper.getInstance(this).updateCategory(categoryDefault);
		
		// These maps are used to associate with post processing notes to categories (notebooks)
		HashMap<String, Category> categoriesWithUuid = new HashMap<String, Category>();
		HashMap<Note, String> notesWithcategory = new HashMap<Note, String>();

		// And then notes are created
		Note note;
		Attachment mAttachment = null;
		Uri uri = null;
		for (SpringpadElement springpadElement : elements) {
			
			// Checks if is a notebook (category)
			if (springpadElement.getType().equals(SpringpadElement.TYPE_NOTEBOOK)) {
				Category cat = new Category();
				cat.setName(springpadElement.getName());
				cat.setColor(String.valueOf(Color.parseColor("#F9EA1B")));
				DbHelper.getInstance(this).updateCategory(cat);
				categoriesWithUuid.put(springpadElement.getUuid(), cat);
				continue;
			}
			
			// Otherwise is a note or comparable content
			note = new Note();
			// Title and content
			note.setTitle(springpadElement.getName());
			CharSequence content = TextUtils.isEmpty(springpadElement.getText()) ? "" : Html.fromHtml(springpadElement
					.getText());
			note.setContent(content.toString());
			// Checklists
			if (springpadElement.getType().equals(SpringpadElement.TYPE_CHECKLIST)) {
				StringBuilder sb = new StringBuilder();
				String checkmark;
				for (SpringpadItem mSpringpadItem : springpadElement.getItems()) {
					checkmark = mSpringpadItem.getComplete() ? it.feio.android.checklistview.interfaces.Constants.CHECKED_SYM
							: it.feio.android.checklistview.interfaces.Constants.UNCHECKED_SYM;
					sb.append(checkmark).append(mSpringpadItem.getName()).append(System.getProperty("line.separator"));
				}
				note.setContent(sb.toString());
				note.setChecklist(true);
			}
			// Tags
			String tags = springpadElement.getTags().size() > 0 ? "#" + TextUtils.join(" #", springpadElement.getTags()) : "";
			if (note.isChecklist()) {
				note.setTitle(note.getTitle() + tags);
			} else {
				note.setContent(note.getContent() + System.getProperty("line.separator") + tags);
			}
			// Address
			String address = springpadElement.getAddresses() != null ? springpadElement.getAddresses().getAddress() : "";
			if (!TextUtils.isEmpty(address)) {
				try {
					double[] coords = GeocodeHelper.getCoordinatesFromAddress(this, address);
					note.setLatitude(coords[0]);
					note.setLongitude(coords[1]);
				} catch (IOException e) {
					Log.e(Constants.TAG,
							"An error occurred trying to resolve address to coords during Springpad import");
				}
				note.setAddress(address);
			}
			// Creation, modification, category
			note.setCreation(springpadElement.getCreated().getTime());
			note.setLastModification(springpadElement.getModified().getTime());

			// Image
			String image = springpadElement.getImage();
			if (image != null) {
				try {
					File file = StorageManager.createNewAttachmentFileFromHttp(this, image);
					uri = Uri.fromFile(file);
					String mimeType = StorageManager.getMimeType(uri.getPath());
					mAttachment = new Attachment(uri, mimeType);
				} catch (MalformedURLException e) {
					uri = Uri.parse(importer.getWorkingPath() + image);
					mAttachment = StorageManager.createAttachmentFromUri(this, uri);
				} catch (IOException e) {
					Log.e(Constants.TAG, "Error retrieving Springpad online image");
				}
				if (mAttachment != null) {
					note.addAttachment(mAttachment);
				}
				mAttachment = null;
			}

			// Other attachments
			for (SpringpadAttachment springpadAttachment : springpadElement.getAttachments()) {
				// The attachment could be the image itself so it's jumped
				if (image != null && image.equals(springpadAttachment.getUrl())) continue;

				// Tryies first with online images
				try {
					File file = StorageManager.createNewAttachmentFileFromHttp(this, springpadAttachment.getUrl());
					uri = Uri.fromFile(file);
					String mimeType = StorageManager.getMimeType(uri.getPath());
					mAttachment = new Attachment(uri, mimeType);
				} catch (MalformedURLException e) {
					uri = Uri.parse(importer.getWorkingPath() + springpadAttachment.getUrl());
					mAttachment = StorageManager.createAttachmentFromUri(this, uri);
				} catch (IOException e) {
					Log.e(Constants.TAG, "Error retrieving Springpad online image");
				}
				if (mAttachment != null) {
					note.addAttachment(mAttachment);
				}
				uri = null;
				mAttachment = null;
			}
			
			// If the note has a category is added to the map to be post-processed
			if (springpadElement.getNotebooks().size() > 0) {
				notesWithcategory.put(note, springpadElement.getNotebooks().get(0));
			}
			
			// The note is saved
			DbHelper.getInstance(this).updateNote(note, false);

		};
		
		// Categories association post-process
		Iterator iterator = notesWithcategory.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry mapEntry = (Map.Entry) iterator.next();
			System.out.println("The key is: " + mapEntry.getKey()
				+ ",value is :" + mapEntry.getValue());
			Note noteWithcategory = (Note)mapEntry.getKey();
			String uuid = (String)mapEntry.getValue();
			noteWithcategory.setCategory(categoriesWithUuid.get(uuid));
			DbHelper.getInstance(this).updateNote(noteWithcategory, false);
		}

		String title = getString(R.string.data_import_completed);
		String text = getString(R.string.click_to_refresh_application);
		createNotification(intent, this, title, text);
	}


	synchronized private void deleteData(Intent intent) {

		// Gets backup folder
		String backupName = intent.getStringExtra(INTENT_BACKUP_NAME);
		File backupDir = StorageManager.getBackupDir(backupName);

		// Backup directory removal
		StorageManager.delete(this, backupDir.getAbsolutePath());

		String title = getString(R.string.data_deletion_completed);
		String text = backupName + " " + getString(R.string.deleted);
		createNotification(intent, this, title, text);
	}


	/**
	 * Creation of notification on operations completed
	 * 
	 * @param intent2
	 * @param ctx
	 * @param message
	 */
	private void createNotification(Intent intent, Context ctx, String title, String message) {

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx)
				.setSmallIcon(R.drawable.ic_stat_notification_icon).setContentTitle(title).setContentText(message)
				.setAutoCancel(true);

		// Ringtone options
		String ringtone = prefs.getString("settings_notification_ringtone", null);
		if (ringtone != null) {
			mBuilder.setSound(Uri.parse(ringtone));
		}

		// Vibration options
		long[] pattern = { 500, 500 };
		if (prefs.getBoolean("settings_notification_vibration", true)) mBuilder.setVibrate(pattern);

		// The behavior differs depending on intent action
		Intent intentLaunch;
		if (DataBackupIntentService.ACTION_DATA_IMPORT.equals(intent.getAction())
				|| DataBackupIntentService.ACTION_DATA_IMPORT_SPRINGPAD.equals(intent.getAction())) {
			intentLaunch = new Intent(ctx, MainActivity.class);
			intentLaunch.setAction(Constants.ACTION_RESTART_APP);
		} else {
			intentLaunch = new Intent();
		}

		// Add this bundle to the intent
		intentLaunch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intentLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		// Creates the PendingIntent
		PendingIntent notifyIntent = PendingIntent.getActivity(ctx, 0, intentLaunch, PendingIntent.FLAG_UPDATE_CURRENT);

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
		return (StorageManager.copyFile(database, new File(backupDir, Constants.DATABASE_NAME)));
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

		DbHelper db = DbHelper.getInstance(this);
		ArrayList<Attachment> list = db.getAllAttachments();

		for (Attachment attachment : list) {
			StorageManager.copyToBackupDir(destinationattachmentsDir, new File(attachment.getUri().getPath()));
		}
		return true;
	}


	/**
	 * Exports settings if required
	 * 
	 * @param backupDir
	 * @return
	 */
	private boolean exportSettings(File backupDir) {
		File preferences = StorageManager.getSharedPreferencesFile(this);
		return (StorageManager.copyFile(preferences, new File(backupDir, preferences.getName())));
	}


	/**
	 * Imports settings
	 * 
	 * @param backupDir
	 * @return
	 */
	private boolean importSettings(File backupDir) {
		File preferences = StorageManager.getSharedPreferencesFile(this);
		File preferenceBackup = new File(backupDir, preferences.getName());
		return (StorageManager.copyFile(preferenceBackup, preferences));
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
		// Clearing
		StorageManager.delete(this, attachmentsDir.getAbsolutePath());
		// Moving back
		File backupAttachmentsDir = new File(backupDir, attachmentsDir.getName());
		return (StorageManager.copyDirectory(backupAttachmentsDir, attachmentsDir));
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

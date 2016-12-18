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

package it.feio.android.omninotes.helpers;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.NotificationsHelper;
import it.feio.android.omninotes.utils.StorageHelper;
import it.feio.android.omninotes.utils.TextHelper;
import rx.Observable;


public class BackupHelper {

	private static String TAG = BackupHelper.class.getSimpleName();


	public static void exportNotes(File backupDir) {
		for (Note note : DbHelper.getInstance(true).getAllNotes(false)) {
			exportNote(backupDir, note);
		}
	}


	public static void exportNote(File backupDir, Note note) {
		File noteFile = new File(backupDir, String.valueOf(note.get_id()) + ".json");
		try {
			FileUtils.write(noteFile, note.toJSON());
		} catch (IOException e) {
			Log.e(Constants.TAG, "Error backupping note: " + note.get_id());
		}
	}


	/**
	 * Export attachments to backup folder
	 *
	 * @return True if success, false otherwise
	 */
	public static boolean exportAttachments(File backupDir) {
		return exportAttachments(backupDir, null);
	}


	/**
	 * Export attachments to backup folder notifying for each attachment copied
	 *
	 * @return True if success, false otherwise
	 */
	public static boolean exportAttachments(File backupDir, NotificationsHelper notificationsHelper) {
		File destinationattachmentsDir = new File(backupDir, StorageHelper.getAttachmentDir().getName());
		ArrayList<Attachment> list = DbHelper.getInstance().getAllAttachments();
		exportAttachments(notificationsHelper, destinationattachmentsDir, list, null);
		return true;
	}


	public static void exportAttachments(NotificationsHelper notificationsHelper, File destinationattachmentsDir,
										 List<Attachment> list, List<Attachment> listOld) {
		listOld = listOld == null ? Collections.EMPTY_LIST : listOld;
		int exported = 0;
		for (Attachment attachment : list) {
			StorageHelper.copyToBackupDir(destinationattachmentsDir, new File(attachment.getUri().getPath()));
			if (notificationsHelper != null) {
				notificationsHelper.setMessage(TextHelper.capitalize(OmniNotes.getAppContext().getString(R.string
						.attachment)) + " " + exported++ + "/" + list.size()).show();
			}
		}
		Observable.from(listOld)
				.filter(attachment -> !list.contains(attachment))
				.forEach(attachment -> StorageHelper.delete(OmniNotes.getAppContext(), new File
						(destinationattachmentsDir.getAbsolutePath(),
								attachment.getUri().getLastPathSegment()).getAbsolutePath()));
	}


	/**
	 * Imports backuped notes
	 *
	 * @param backupDir
	 */
	public static void importNotes(File backupDir) {
		for (File file : FileUtils.listFiles(backupDir, new RegexFileFilter("\\d{13}"), TrueFileFilter.INSTANCE)) {
			importNote(file);
		}
	}


	/**
	 * Imports single note from its file
	 */
	public static Note importNote(File file) {
		try {
			Note note = new Note();
			String jsonString = FileUtils.readFileToString(file);
			if (!TextUtils.isEmpty(jsonString)) {
				note.buildFromJson(jsonString);
				if (note.getCategory() != null) {
					DbHelper.getInstance().updateCategory(note.getCategory());
				}
				note.setAttachmentsListOld(DbHelper.getInstance().getNoteAttachments(note));
				DbHelper.getInstance().updateNote(note, false);
			}
			return note;
		} catch (IOException e) {
			Log.e(Constants.TAG, "Error parsing note json");
			return new Note();
		}
	}


	/**
	 * Import attachments from backup folder
	 */
	public static boolean importAttachments(File backupDir) {
		return importAttachments(backupDir, null);
	}


	/**
	 * Import attachments from backup folder notifying for each imported item
	 */
	public static boolean importAttachments(File backupDir, NotificationsHelper notificationsHelper) {
		File attachmentsDir = StorageHelper.getAttachmentDir();
		File backupAttachmentsDir = new File(backupDir, attachmentsDir.getName());
		if (!backupAttachmentsDir.exists()) return true;
		boolean result = true;
		Collection list = FileUtils.listFiles(backupAttachmentsDir, FileFilterUtils.trueFileFilter(),
				TrueFileFilter.INSTANCE);
		Iterator i = list.iterator();
		int imported = 0;
		File file = null;
		while (i.hasNext()) {
			try {
				file = (File) i.next();
				FileUtils.copyFileToDirectory(file, attachmentsDir, true);
				if (notificationsHelper != null) {
					notificationsHelper.setMessage(TextHelper.capitalize(OmniNotes.getAppContext().getString(R.string
							.attachment)) + " " + imported++ + "/" + list.size()).show();
				}
			} catch (IOException e) {
				result = false;
				Log.e(Constants.TAG, "Error importing the attachment " + file.getName());
			}
		}
		return result;
	}


	/**
	 * Import attachments of a specific note from backup folder
	 */
	public static void importAttachments(Note note, File backupDir) throws IOException {

		File backupAttachmentsDir = new File(backupDir, StorageHelper.getAttachmentDir().getName());

		for (Attachment attachment : note.getAttachmentsList()) {
			String attachmentFileName = FilenameUtils.getName(attachment.getUriPath());
			File attachmentFile = new File(backupAttachmentsDir, attachmentFileName);
			if (attachmentFile.exists()) {
				FileUtils.copyFileToDirectory(attachmentFile, StorageHelper.getAttachmentDir(), true);
			} else {
				Log.e(Constants.TAG, "Attachment file not found: " + attachmentFileName);
			}
		}
	}


	/**
	 * Exports settings if required
	 */
	public static boolean exportSettings(File backupDir) {
		File preferences = StorageHelper.getSharedPreferencesFile(OmniNotes.getAppContext());
		return (StorageHelper.copyFile(preferences, new File(backupDir, preferences.getName())));
	}


	/**
	 * Imports settings
	 */
	public static boolean importSettings(File backupDir) {
		File preferences = StorageHelper.getSharedPreferencesFile(OmniNotes.getAppContext());
		File preferenceBackup = new File(backupDir, preferences.getName());
		return (StorageHelper.copyFile(preferenceBackup, preferences));
	}


	public static boolean deleteNoteBackup(File backupDir, Note note) {
		File noteFile = new File(backupDir, String.valueOf(note.get_id()));
		boolean result = noteFile.delete();
		File attachmentBackup = new File(backupDir, StorageHelper.getAttachmentDir().getName());
		for (Attachment attachment : note.getAttachmentsList()) {
			result = result && new File(attachmentBackup, FilenameUtils.getName(attachment.getUri().getPath()))
					.delete();
		}
		return result;
	}


	public static void deleteNote(File file) {
		try {
			Note note = new Note();
			note.buildFromJson(FileUtils.readFileToString(file));
			DbHelper.getInstance().deleteNote(note);
		} catch (IOException e) {
			Log.e(Constants.TAG, "Error parsing note json");
		}
	}


	/**
	 * Import database from backup folder. Used ONLY to restore legacy backup
	 *
	 * @deprecated {@link BackupHelper#importNotes(File)}
	 */
	@Deprecated
	public static boolean importDB(Context context, File backupDir) {
		File database = context.getDatabasePath(Constants.DATABASE_NAME);
		if (database.exists()) {
			database.delete();
		}
		return (StorageHelper.copyFile(new File(backupDir, Constants.DATABASE_NAME), database));
	}


	public static List<LinkedList<DiffMatchPatch.Diff>> integrityCheck(Context context, File backupDir) {
		List<LinkedList<DiffMatchPatch.Diff>> errors = new ArrayList<>();
		for (Note note : DbHelper.getInstance(true).getAllNotes(false)) {
			File noteFile = new File(backupDir, String.valueOf(note.get_id()));
			try {
				String noteString = note.toJSON();
				String noteFileString = FileUtils.readFileToString(noteFile);
				if (noteString.equals(noteFileString)) {
					File backupAttachmentsDir = new File(backupDir, StorageHelper.getAttachmentDir().getName());
					for (Attachment attachment : note.getAttachmentsList()) {
						if (!new File(backupAttachmentsDir, FilenameUtils.getName(attachment.getUriPath())).exists()) {
							addIntegrityCheckError(errors, new FileNotFoundException("Attachment " + attachment
									.getUriPath() + " missing"));
						}
					}
				} else {
					errors.add(new DiffMatchPatch().diffMain(noteString, noteFileString));
				}
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
				addIntegrityCheckError(errors, e);
			}
		}
		return errors;
	}


	private static void addIntegrityCheckError(List<LinkedList<DiffMatchPatch.Diff>> errors, IOException e) {
		LinkedList l = new LinkedList();
		l.add(new DiffMatchPatch.Diff(DiffMatchPatch.Operation.DELETE, e.getMessage()));
		errors.add(l);
	}


}

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

import android.os.AsyncTask;
import android.util.Log;
import de.greenrobot.event.EventBus;
import it.feio.android.omninotes.async.bus.NotesUpdatedEvent;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.helpers.BackupHelper;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.StorageHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SyncAutoBackupTask extends AsyncTask<Void, Void, Boolean> {


	@Override
	protected Boolean doInBackground(Void... params) {

		List<Note> allNotes = DbHelper.getInstance().getAllNotes(false);
		File autoBackupDir = StorageHelper.getBackupDir(Constants.AUTO_BACKUP_DIR);
		boolean refreshNotes = false;

		if (!autoBackupDir.exists()) return refreshNotes;

		List<Note> autoBackupNotes = new ArrayList<>();
		for (File file : FileUtils.listFiles(autoBackupDir, new RegexFileFilter("\\d{13}"), TrueFileFilter.INSTANCE)) {
			try {
				Note note = new Note();
				note.buildFromJson(FileUtils.readFileToString(file));
				autoBackupNotes.add(note);
				if (!allNotes.contains(note)) {
					Log.d(getClass().getSimpleName(), "Matching note found: " + note.get_id());
					BackupHelper.importNote(file);
					BackupHelper.importAttachments(note, autoBackupDir);
					refreshNotes = true;
				}
			} catch (IOException e) {
				Log.e(getClass().getSimpleName(), "Error parsing note JSON", e);
			}
		}

		// Managing remote note deletion
		for (Note note : allNotes) {
			if (!autoBackupNotes.contains(note)) {
				DbHelper.getInstance().deleteNote(note);
			}
		}

		return refreshNotes;
	}


	@Override
	protected void onPostExecute(Boolean refreshNotes) {

		if (refreshNotes) {
			EventBus.getDefault().post(new NotesUpdatedEvent(Collections.emptyList()));
		}
	}
}
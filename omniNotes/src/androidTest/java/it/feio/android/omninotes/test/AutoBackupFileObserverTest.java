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

package it.feio.android.omninotes.test;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import de.greenrobot.event.EventBus;
import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.async.bus.NotesDeletedEvent;
import it.feio.android.omninotes.async.bus.NotesUpdatedEvent;
import it.feio.android.omninotes.async.notes.NoteProcessorDelete;
import it.feio.android.omninotes.async.notes.SaveNoteTask;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.listeners.OnNoteSaved;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.StorageHelper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;


public class AutoBackupFileObserverTest extends BaseRobotiumTest {

	public AutoBackupFileObserverTest() {
		super(MainActivity.class);
	}


	public void testAutoBackupFileCreationAndDeletion() {
		solo.waitForActivity(MainActivity.class);
		createDummyNote(noteSaved -> {
			EventBus.getDefault().post(new NotesUpdatedEvent(Collections.singletonList(noteSaved)));
			new Handler().postDelayed(() -> {
				checkNoteFile(noteSaved);
				deleteDummyNote(noteSaved);
				checkNoteFileExists(noteSaved);
			}
					, 400);
		});
	}


	private void checkNoteFileExists(Note dummyNote) {
		File dummyNodeFile = new File(StorageHelper.getBackupDir(Constants.AUTO_BACKUP_DIR), dummyNote.get_id().toString());
		Log.d(getClass().getSimpleName(), "Checking if exists " + dummyNodeFile.getAbsolutePath());
		assertTrue(dummyNodeFile.exists());
	}


	public void onEvent(NotesDeletedEvent notesDeletedEvent) {
		for (Note dummyNote : notesDeletedEvent.notes) {
			File dummyNodeFile = new File(StorageHelper.getBackupDir(Constants.AUTO_BACKUP_DIR), dummyNote.get_id().toString());
			Log.d(getClass().getSimpleName(), "Checking if is deleted " + dummyNodeFile.getAbsolutePath());
			assertFalse(dummyNodeFile.exists());
		}
	}


	private void createDummyNote(OnNoteSaved onNoteSaved) {
		Note dummyNote = new Note();
		dummyNote.setTitle("Dummy title");
		dummyNote.setContent("Dummy content");
		new SaveNoteTask(onNoteSaved, true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dummyNote);
	}


	private void deleteDummyNote(Note dummyNote) {
		new NoteProcessorDelete(Collections.singletonList(dummyNote)).process();
	}


	private void checkNoteFile(Note noteSaved) {
		File autoBackupDir = StorageHelper.getBackupDir(Constants.AUTO_BACKUP_DIR);
		Note note = new Note();
		try {
			note.buildFromJson(FileUtils.readFileToString(new File(autoBackupDir, noteSaved.get_id().toString())));
			assertNotNull(note);
			Log.d(getClass().getSimpleName(), "Recovered note with title: " + note.getTitle());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

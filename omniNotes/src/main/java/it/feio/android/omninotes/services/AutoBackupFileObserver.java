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

package it.feio.android.omninotes.services;

import android.os.FileObserver;
import android.util.Log;
import de.greenrobot.event.EventBus;
import it.feio.android.omninotes.async.bus.NotesSavedEvent;
import it.feio.android.omninotes.helpers.BackupHelper;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.StorageHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class AutoBackupFileObserver extends FileObserver {

	private static AutoBackupFileObserver instance;
	private static String monitoredPath;

	private List<Note> recentlyModifiedNotes = new ArrayList<>();

	private List<String> receivedOnce = new ArrayList<>(); // Workaround for MODIFY event called twice


	private AutoBackupFileObserver() {
		super(monitoredPath, FileObserver.CREATE | FileObserver.MODIFY);
		EventBus.getDefault().register(this);
	}


	public static AutoBackupFileObserver getInstance() {
		if (instance == null) {
			monitoredPath = StorageHelper.getBackupDir(Constants.AUTO_BACKUP_DIR).getAbsolutePath();
			instance = new AutoBackupFileObserver();
		}
		return instance;
	}


	@Override
	public void stopWatching() {
		if (instance != null) {
			super.stopWatching();
		}
	}


	@Override
	public void onEvent(int event, String path) {

		if (path == null) {
			return;
		}

		if (!receivedOnce.contains(path)) {
			receivedOnce.add(path);
			return;
		}

		receivedOnce.remove(path);

		StringBuilder logMsg = new StringBuilder(path);
		if (isEvent(event, FileObserver.MODIFY)) {
			logMsg.append(" has been modified");
		} else if (isEvent(event, FileObserver.CREATE)) {
			logMsg.append(" has been created");
		}

		if (!isRecentlyModifiedNote(path)) {
			logMsg.append(" externally");
			BackupHelper.importNote(new File(monitoredPath + "/" + path));
		}
		Log.d(getClass().getSimpleName(), logMsg.toString());
	}


	private boolean isRecentlyModifiedNote(String path) {
		for (Note recentlyModifiedNote : recentlyModifiedNotes) {
			if (recentlyModifiedNote.get_id().toString().equals(path)) {
				recentlyModifiedNotes.remove(recentlyModifiedNote);
				return true;
			}
		}
		return false;
	}


	public void onEventAsync(NotesSavedEvent notesSavedEvent) {
		recentlyModifiedNotes = new ArrayList<>(notesSavedEvent.notes);
	}


	private boolean isEvent(int event, int[] eventTypes) {
		for (int eventType : eventTypes) {
			if (isEvent(event, eventType)) {
				return true;
			}
		}
		return false;
	}


	private boolean isEvent(int event, int eventType) {
		return (eventType & event) != 0;
	}

}

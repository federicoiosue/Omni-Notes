/*
 * Copyright (C) 2018 Federico Iosue (federico.iosue@gmail.com)
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
package it.feio.android.omninotes.async.notes;

import android.os.AsyncTask;
import android.util.Log;
import de.greenrobot.event.EventBus;
import it.feio.android.omninotes.async.bus.NotesLoadedEvent;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.exceptions.NotesLoadingException;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;


public class NoteLoaderTask extends AsyncTask<Object, Void, ArrayList<Note>> {

	private static NoteLoaderTask instance;

	private NoteLoaderTask() {}


	public static NoteLoaderTask getInstance() {

		if (instance != null) {
			if (instance.getStatus() == Status.RUNNING && !instance.isCancelled()) {
				instance.cancel(true);
			} else if (instance.getStatus() == Status.PENDING) {
				return instance;
			}
		}

		instance = new NoteLoaderTask();
		return instance;
	}


	@Override
	protected ArrayList<Note> doInBackground(Object... params) {

		ArrayList<Note> notes = new ArrayList<>();
		String methodName = params[0].toString();
		DbHelper db = DbHelper.getInstance();

		if (params.length < 2 || params[1] == null) {
			try {
				Method method = db.getClass().getDeclaredMethod(methodName);
				notes = (ArrayList<Note>)method.invoke(db);
			} catch (NoSuchMethodException e) {
				return notes;
			} catch (IllegalAccessException e) {
				throw new NotesLoadingException("Error retrieving notes", e);
			} catch (InvocationTargetException e) {
				throw new NotesLoadingException("Error retrieving notes", e);
			}
		} else {
			Object methodArgs = params[1];
			Class[] paramClass = new Class[]{methodArgs.getClass()};
			try {
				Method method = db.getClass().getDeclaredMethod(methodName, paramClass);
				notes = (ArrayList<Note>) method.invoke(db, paramClass[0].cast(methodArgs));
			} catch (Exception e) {
				throw new NotesLoadingException("Error retrieving notes", e);
			}
		}

		return notes;
	}


	@Override
	protected void onPostExecute(ArrayList<Note> notes) {

		super.onPostExecute(notes);
		EventBus.getDefault().post(new NotesLoadedEvent(notes));
	}
}

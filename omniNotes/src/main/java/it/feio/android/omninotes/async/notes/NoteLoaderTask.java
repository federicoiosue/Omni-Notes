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
package it.feio.android.omninotes.async.notes;

import android.os.AsyncTask;
import de.greenrobot.event.EventBus;
import it.feio.android.omninotes.async.bus.NotesLoadedEvent;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.exceptions.NotesLoadingException;
import it.feio.android.omninotes.models.Note;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public class NoteLoaderTask extends AsyncTask<Object, Void, List<Note>> {

  private static final String ERROR_RETRIEVING_NOTES = "Error retrieving notes";

  private static NoteLoaderTask instance;

  private NoteLoaderTask () {}


  public static NoteLoaderTask getInstance () {

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
  protected List<Note> doInBackground (Object... params) {

    String methodName = params[0].toString();
    DbHelper db = DbHelper.getInstance();

    if (params.length < 2 || params[1] == null) {
      try {
        Method method = db.getClass().getDeclaredMethod(methodName);
        return (List<Note>) method.invoke(db);
      } catch (NoSuchMethodException e) {
        return new ArrayList<>();
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new NotesLoadingException(ERROR_RETRIEVING_NOTES, e);
      }
    } else {
      Object methodArgs = params[1];
      Class[] paramClass = new Class[]{methodArgs.getClass()};
      try {
        Method method = db.getClass().getDeclaredMethod(methodName, paramClass);
        return (List<Note>) method.invoke(db, paramClass[0].cast(methodArgs));
      } catch (Exception e) {
        throw new NotesLoadingException(ERROR_RETRIEVING_NOTES, e);
      }
    }
  }


  @Override
  protected void onPostExecute (List<Note> notes) {

    super.onPostExecute(notes);
    EventBus.getDefault().post(new NotesLoadedEvent(notes));
  }
}

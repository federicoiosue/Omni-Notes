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
import it.feio.android.omninotes.async.bus.NotesUpdatedEvent;
import it.feio.android.omninotes.models.Note;
import java.util.ArrayList;
import java.util.List;


public abstract class NoteProcessor {

  List<Note> notes;


  protected NoteProcessor (List<Note> notes) {
    this.notes = new ArrayList<>(notes);
  }


  public void process () {
    NotesProcessorTask task = new NotesProcessorTask();
    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, notes);
  }


  protected abstract void processNote (Note note);


  class NotesProcessorTask extends AsyncTask<List<Note>, Void, List<Note>> {

    @Override
    protected List<Note> doInBackground (List<Note>... params) {
      List<Note> processableNote = params[0];
      for (Note note : processableNote) {
        processNote(note);
      }
      return processableNote;
    }


    @Override
    protected void onPostExecute (List<Note> notes) {
      afterProcess(notes);
    }
  }


  protected void afterProcess (List<Note> notes) {
    EventBus.getDefault().post(new NotesUpdatedEvent(notes));
  }
}

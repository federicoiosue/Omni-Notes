package it.feio.android.omninotes.async.bus;

import android.util.Log;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;

import java.util.List;


public class NotesUpdatedEvent {

	public List<Note> notes;


	public NotesUpdatedEvent(List<Note> notes) {
		Log.d(Constants.TAG, this.getClass().getName());
		this.notes = notes;
	}
}

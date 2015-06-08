package it.feio.android.omninotes.async.bus;

import android.util.Log;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;

import java.util.ArrayList;


public class NotesLoadedEvent {

	public ArrayList<Note> notes;


	public NotesLoadedEvent(ArrayList<Note> notes) {
		Log.d(Constants.TAG, this.getClass().getName());
		this.notes = notes;
	}
}

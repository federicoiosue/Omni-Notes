package it.feio.android.omninotes.models.listeners;

import java.util.ArrayList;

import it.feio.android.omninotes.models.Note;

public interface OnNotesLoadedListener {
	public void onNotesLoaded(ArrayList<Note> notes);
}

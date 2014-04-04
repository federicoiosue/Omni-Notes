package it.feio.android.omninotes.models.listeners;

import it.feio.android.omninotes.models.Note;

import java.util.ArrayList;

public interface OnNotesLoadedListener {
	public void onNotesLoaded(ArrayList<Note> notes);
}

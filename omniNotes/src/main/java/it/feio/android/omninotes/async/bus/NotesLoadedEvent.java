package it.feio.android.omninotes.async.bus;

import android.util.Log;
import it.feio.android.omninotes.utils.Constants;


public class NotesLoadedEvent {

	public NotesLoadedEvent() {
		Log.d(Constants.TAG, this.getClass().getName());
	}
}

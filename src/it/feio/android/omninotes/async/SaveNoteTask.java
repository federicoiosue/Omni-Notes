package it.feio.android.omninotes.async;

import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Note;
import android.content.Context;
import android.os.AsyncTask;

public class SaveNoteTask extends AsyncTask<Note, Void, Void> {
	private final Context mContext;

	public SaveNoteTask(Context context) {
		super();
		this.mContext = context;
	}

	@Override
	protected Void doInBackground(Note... params) {
		Note note = params[0];
		DbHelper db = new DbHelper(mContext);
		note = db.updateNote(note);
		return null;
	}
}

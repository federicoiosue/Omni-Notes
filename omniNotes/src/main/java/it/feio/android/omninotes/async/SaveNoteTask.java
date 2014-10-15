package it.feio.android.omninotes.async;

import it.feio.android.omninotes.DetailFragment;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.listeners.OnNoteSaved;
import it.feio.android.omninotes.receiver.AlarmReceiver;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.StorageManager;
import java.util.Calendar;
import java.util.List;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

public class SaveNoteTask extends AsyncTask<Note, Void, Note> {
	
	private final Activity mActivity;
	private boolean error = false;
	private boolean updateLastModification = true;
	private OnNoteSaved mOnNoteSaved;

	
	public SaveNoteTask(DetailFragment activity, boolean updateLastModification) {
		this(activity, null, updateLastModification);
	}

	
	public SaveNoteTask(DetailFragment activity, OnNoteSaved mOnNoteSaved, boolean updateLastModification) {
		super();
		mActivity = activity.getActivity();
		this.mOnNoteSaved = mOnNoteSaved;
		this.updateLastModification = updateLastModification;
	}

	
	@Override
	protected Note doInBackground(Note... params) {
		Note note = params[0];
		purgeRemovedAttachments(note);
		
		if (!error) {
			DbHelper db = DbHelper.getInstance(mActivity);		
			// Note updating on database
			note = db.updateNote(note, updateLastModification);
		} else {
			Toast.makeText(mActivity, mActivity.getString(R.string.error_saving_attachments), Toast.LENGTH_SHORT).show();
		}
			
		return note;
	}
	
	private void purgeRemovedAttachments(Note note) {
		List<Attachment> deletedAttachments = note.getAttachmentsListOld();
		for (Attachment attachment : note.getAttachmentsList()) {
			if (attachment.getId() != 0) {
				deletedAttachments.remove(attachment);
			}
		}
		// Remove from database deleted attachments
		for (Attachment deletedAttachment : deletedAttachments) {
			StorageManager.delete(mActivity, deletedAttachment.getUri().getPath());
		}
	}

	
	@Override
	protected void onPostExecute(Note note) {
		super.onPostExecute(note);
		
		// Set reminder if is not passed yet
		long now = Calendar.getInstance().getTimeInMillis();
		if (note.getAlarm() != null && Long.parseLong(note.getAlarm()) >= now) {
			setAlarm(note);
		}

		if (this.mOnNoteSaved != null) {
			mOnNoteSaved.onNoteSaved(note);
		}
	}
	
	

	private void setAlarm(Note note) {
		Intent intent = new Intent(mActivity, AlarmReceiver.class);
		intent.putExtra(Constants.INTENT_NOTE, note);
		PendingIntent sender = PendingIntent.getBroadcast(mActivity, note.getCreation().intValue(), intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager) mActivity.getSystemService(Activity.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, Long.parseLong(note.getAlarm()), sender);
	}
	
	
}

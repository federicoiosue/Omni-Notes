package it.feio.android.omninotes.async;

import it.feio.android.omninotes.DetailActivity;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.receiver.AlarmReceiver;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.StorageManager;

import java.io.File;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class SaveNoteTask extends AsyncTask<Note, Void, Note> {
	private final Activity mActivity;

	public SaveNoteTask(Activity activity) {
		super();
		this.mActivity = activity;
	}

	@Override
	protected Note doInBackground(Note... params) {
		Note note = params[0];
		createAttachmentCopy(note);
		DbHelper db = new DbHelper(mActivity);		
		// Note updating on database
		note = db.updateNote(note);		
		return note;
	}
	
	@Override
	protected void onPostExecute(Note note) {
		super.onPostExecute(note);
		
		// Set reminder if is not passed yet
		long now = Calendar.getInstance().getTimeInMillis();
		if (note.getAlarm() != null && Long.parseLong(note.getAlarm()) >= now) {
			setAlarm(note);
		}

		// Return back to parent activity now that the heavy work is done to speed up interface
		((DetailActivity)mActivity).goHome();
	}

	
	/**
	 * Makes copies of attachments files and replace uris 
	 * @param note
	 */
	private void createAttachmentCopy(Note note) {
		File destination;
		
		for (Attachment attachment : note.getAttachmentsList()) {
			
			// The copy will be made only if it's a new attachment or if attachment directory is not yet the destination one
			if (attachment.getId() != 0 || 
					attachment.getUri().getPath().contains(mActivity.getExternalFilesDir(null).getAbsolutePath()))
				return;
			
			String extension = "";
			if (attachment.getMime_type().equals(Constants.MIME_TYPE_AUDIO))
				extension = Constants.MIME_TYPE_AUDIO_EXT;
			else if (attachment.getMime_type().equals(Constants.MIME_TYPE_IMAGE))
				extension = Constants.MIME_TYPE_IMAGE_EXT;
			else if (attachment.getMime_type().equals(Constants.MIME_TYPE_VIDEO))
				extension = Constants.MIME_TYPE_VIDEO_EXT;
								
			destination = StorageManager.createExternalStoragePrivateFile(mActivity, attachment.getUri(), extension);
			Log.v(Constants.TAG, "Moving attachment " + attachment.getUri() + " to " + destination);
			
			if (destination == null) {				
				Log.e(Constants.TAG, "Can't move file");
				break;
			}
			
			// Replace uri
			attachment.setUri(Uri.fromFile(destination));
		}
	}
	


	private void setAlarm(Note note) {
		Intent intent = new Intent(mActivity, AlarmReceiver.class);
		intent.putExtra(Constants.INTENT_NOTE, note);
		PendingIntent sender = PendingIntent.getBroadcast(mActivity, Constants.INTENT_ALARM_CODE, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager) mActivity.getSystemService(mActivity.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, Long.parseLong(note.getAlarm()), sender);
	}
	
	
	
	
	
}

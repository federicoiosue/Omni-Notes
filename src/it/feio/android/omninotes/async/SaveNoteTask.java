package it.feio.android.omninotes.async;

import java.io.File;
import it.feio.android.omninotes.DetailActivity;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.StorageManager;
import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class SaveNoteTask extends AsyncTask<Note, Void, Void> {
	private final Activity mActivity;

	public SaveNoteTask(Activity activity) {
		super();
		this.mActivity = activity;
	}

	@Override
	protected Void doInBackground(Note... params) {
		Note note = params[0];
		createAttachmentCopy(note);
		DbHelper db = new DbHelper(mActivity);		
		// Note updating on database
		note = db.updateNote(note);		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);

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
			
//			// Determination of file type
//			ContentResolver cR = mActivity.getContentResolver();
//			MimeTypeMap mime = MimeTypeMap.getSingleton();
//			String ext = mime.getExtensionFromMimeType(cR.getType(attachment.getUri()));
//			
//			
//			// Old copy mode
//			destination = new File(destinationDir, attachment.getUri().getLastPathSegment() + "." + ext);
//			try {
//				destination.createNewFile();
//			} catch (IOException e1) {
//				e1.printStackTrace();
//			}
//			
//			try {
//				StorageManager.copyFile(mActivity.getContentResolver().openInputStream(attachment.getUri()), new FileOutputStream(destination));
//			} catch (FileNotFoundException e) {
//				Log.e(Constants.TAG, "File not found");
//			}
			
					
			destination = StorageManager.createExternalStoragePrivateFile(mActivity, attachment.getUri());
			
			if (destination == null) {				
				Log.e(Constants.TAG, "Can't move file");
				break;
			}
			
			// Replace uri
			attachment.setUri(Uri.fromFile(destination));
		}
	}
	
	
	
	
	
}

package it.feio.android.omninotes.utils.sync.drive;

import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.StorageManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.Metadata;

public class DriveSyncTask extends ApiClientAsyncTask<List<Note>, Void, Boolean> {

	Context mContext;
	private DriveHelper driveHelper;

	public DriveSyncTask(Context mContext) {
		super(mContext);
		this.mContext = mContext;
	}

	@Override
	protected Boolean doInBackgroundConnected(List<Note>... params) {
		driveHelper = DriveHelper.getInstance(mContext, getGoogleApiClient());
		List<Note> notesToSync = params[0];
		for (Note note : notesToSync) {
			syncNote(note.get_id());
			syncAttachments(note.getAttachmentsList());
		}
		
		return true;
	}
	

	@Override
	protected void onPostExecute(Boolean result) {
		if (result) {
			Toast.makeText(mContext, "Sync completed", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(mContext, "Error during synchronization", Toast.LENGTH_SHORT).show();
		}
		super.onPostExecute(result);
	}

	
	
	
	
	private void syncNote(int get_id) {
		File dbSyncDir = StorageManager.getDbSyncDir(mContext);
		
	}
	
	
	
	private void syncAttachments(List<Attachment> attachments) {
		List<File> filesToSync = new ArrayList<File>();
		for (Attachment mAttachment : attachments) {
			File f = new File(mAttachment.getUri().getPath());
			filesToSync.add(f);
		}
		for (File file : filesToSync) {
			DriveFile mDriveFile = driveHelper.getFile(file);
			Metadata mMetadata;
			if (mDriveFile == null) {
				mDriveFile = driveHelper.createFile(file);
				String resourceId = driveHelper.write(file, mDriveFile);
				mMetadata = driveHelper.getMetadata(mDriveFile);
			} else {
				mMetadata = driveHelper.getMetadata(mDriveFile);
				
				// Size and modification date check
//				if (mMetadata.getFileSize() == file.length()
//						&& mMetadata.getModifiedDate().getTime() == file.lastModified()) {
				if (!needsUpdate(mMetadata, file)) {
					continue;
				}
				
				// Another date check to determine if download or upload must be performed
				if (mMetadata.getModifiedDate().getTime() > file.lastModified()) {
					driveHelper.read(mDriveFile, file);
				} else {
					driveHelper.write(file, mDriveFile);
				}
			}
			
			// Updates local file last modification date
			file.setLastModified(mMetadata.getModifiedDate().getTime());
		}
	}
	
	
	private boolean needsUpdate(Metadata mMetadata, File file) {
		if (mMetadata.getFileSize() == file.length()) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			String fileDate = sdf.format(new Date(file.lastModified()));
			String metadataDate = sdf.format(mMetadata.getModifiedDate());
			if (fileDate.equals(metadataDate)) {
				return false;
			}
		}
		return true;

	}
}
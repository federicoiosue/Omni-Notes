package it.feio.android.omninotes.utils.sync.drive;

import it.feio.android.omninotes.utils.Constants;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.Metadata;

public class DriveSyncTask extends ApiClientAsyncTask<List<File>, Void, Boolean> {

	Context mContext;
	private DriveHelper driveHelper;
	private List<File> filesToSync;

	public DriveSyncTask(Context mContext) {
		super(mContext);
		this.mContext = mContext;
	}

	@Override
	protected Boolean doInBackgroundConnected(List<File>... params) {
		driveHelper = DriveHelper.getInstance(mContext, getGoogleApiClient());
		filesToSync = params[0];
		for (File file : filesToSync) {
			DriveFile mDriveFile = driveHelper.getFile(file);
			if (mDriveFile == null) {
				mDriveFile = driveHelper.createFile(file);
				String resourceId = driveHelper.write(file, mDriveFile);
			} else {
				Metadata mMetadata = driveHelper.getMetadata(mDriveFile);
				
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
				
				// Updates local file last modification date
				Log.d(Constants.TAG, "Local file last modification: " + file.lastModified() + ", Drive file last modification: " + mMetadata.getModifiedDate().getTime());
				file.setLastModified(mMetadata.getModifiedDate().getTime());
				Log.d(Constants.TAG, "Update: local file last modification: " + file.lastModified() + ", Drive file last modification: " + mMetadata.getModifiedDate().getTime());
			}
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
package it.feio.android.omninotes.utils.sync.drive;

import it.feio.android.omninotes.R;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.StorageManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.Metadata;

public class DriveActivity extends Activity implements ConnectionCallbacks,
		OnConnectionFailedListener {

	private static final String TAG = Constants.TAG;

	/**
	 * Request code for auto Google Play Services error resolution.
	 */
	protected static final int REQUEST_CODE_RESOLUTION = 1;

	/**
	 * Next available request code.
	 */
	protected static final int NEXT_AVAILABLE_REQUEST_CODE = 2;


	protected int REQUEST_CODE_CREATOR = 0;

	private final String FILE_TEST = "OmniNotesTest";

	protected DriveResultsAdapter mDriveResultsAdapter;
	private Context mContext;

	private ListView listView;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_drive);
		mContext = this;
		listView = (ListView) findViewById(R.id.drive_list);
		mDriveResultsAdapter = new DriveResultsAdapter(mContext);
		listView.setAdapter(mDriveResultsAdapter);
		
//		DriveHelper.getInstance(mContext).connect(this, this);
		
		List<Attachment> attachments = (new DbHelper(mContext)).getAttachmentsOfType(Constants.MIME_TYPE_FILES);
		List<File> list = new ArrayList<>();
		for (Attachment mAttachment : attachments) {
			File f = new File(mAttachment.getUri().getPath());
			list.add(f);
		}
		
		new DriveSyncTask(mContext).execute(list);
	}

	/**
	 * Called when activity gets invisible. Connection to Drive service needs to
	 * be disconnected as soon as an activity is invisible.
	 */
	@Override
	protected void onPause() {
		DriveHelper.getInstance(mContext).disconnect();
		super.onPause();
	}

	/**
	 * Handles resolution callbacks.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == RESULT_OK) {
			DriveHelper.getInstance(mContext).connect(this, this);
		}
	}
	

	
	/**
	 * Called when {@code mGoogleApiClient} is connected.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i(TAG, "GoogleApiClient connected");
	}
	
	
	

	/**
	 * Called when {@code mGoogleApiClient} is disconnected.
	 */
	@Override
	public void onConnectionSuspended(int cause) {
		Log.i(TAG, "GoogleApiClient connection suspended");
	}

	/**
	 * Called when {@code mGoogleApiClient} is trying to connect but failed.
	 * Handle {@code result.getResolution()} if there is a resolution is
	 * available.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
		if (!result.hasResolution()) {
			// show the localized error dialog.
			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
					0).show();
			return;
		}
		try {
			result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
		} catch (SendIntentException e) {
			Log.e(TAG, "Exception while starting resolution activity", e);
		}
	}

	/**
	 * Shows a toast message.
	 */
	public void showMessage(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
	
	

	final private ResultCallback<MetadataBufferResult> resultCallback = new ResultCallback<MetadataBufferResult>() {

		@Override
		public void onResult(MetadataBufferResult result) {
			if (!result.getStatus().isSuccess()) {
				showMessage("Problem while retrieving files");
				return;
			}
			mDriveResultsAdapter.setData(result.getMetadataBuffer());
			listView.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent,
						View view, int position, long id) {
					Metadata metadata = mDriveResultsAdapter.getItem(position);
					StringBuilder sb = new StringBuilder();
					sb.append(metadata.getTitle() + "\n")
					.append(metadata.getMimeType() + "\n")
					.append(metadata.getFileSize() + "\n")
					.append(metadata.getCreatedDate() + "\n")
					.append(metadata.getModifiedDate() + "\n");
					showMessage(sb.toString());
					
					return false;
				}
			});
		}
	};
	


}

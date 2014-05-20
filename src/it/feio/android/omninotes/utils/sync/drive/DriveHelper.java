package it.feio.android.omninotes.utils.sync.drive;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.webkit.MimeTypeMap;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Contents;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveResource.MetadataResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

public class DriveHelper {
	private static DriveHelper instance = null;
	/**
	 * Google API client.
	 */
	private GoogleApiClient mGoogleApiClient;
	private Context mContext;

	protected DriveHelper() {
		// Exists only to defeat instantiation.
	}

	public DriveHelper(Context mContext, GoogleApiClient mGoogleApiClient) {
		this.mContext = mContext;
		this.mGoogleApiClient = mGoogleApiClient;
	}

	public static DriveHelper getInstance(Context mContext) {
		return getInstance(mContext, null);
	}

	public static DriveHelper getInstance(Context mContext,
			GoogleApiClient mGoogleApiClient) {
		if (instance == null) {
			instance = new DriveHelper(mContext, mGoogleApiClient);
		}
		return instance;
	}
	
	public void connect(ConnectionCallbacks mConnectionCallbacks,
			OnConnectionFailedListener mOnConnectionFailedListener) {
		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(mContext)
					.addApi(Drive.API)
					.addScope(Drive.SCOPE_FILE)
					.addScope(Drive.SCOPE_APPFOLDER)		
					// required for App Folder sample
					.addConnectionCallbacks(mConnectionCallbacks)
					.addOnConnectionFailedListener(mOnConnectionFailedListener)
					.build();
		}
		mGoogleApiClient.connect();
	}
	
	public void disconnect() {
		if (mGoogleApiClient != null) {
			mGoogleApiClient.disconnect();
		}
	}
	
	
	public DriveId getId(String title) {
		List<Metadata> list = searchByTitle(title);
		if (list.size() == 0) return null;
		return list.get(0).getDriveId();
	}
	
	
	public DriveFile getFile(File file) {
		DriveId mDriveId = getId(file.getName());
		if (mDriveId == null) return null;
		return  getFile(mDriveId);
	}
	
	
	public DriveFile getFile(DriveId mDriveId) {
		return  Drive.DriveApi.getFile(mGoogleApiClient, mDriveId);
	}
	
	public Metadata getMetadata(DriveFile mDriveFile) {
		MetadataResult mMetadataResult = mDriveFile.getMetadata(mGoogleApiClient).await();
		if (!mMetadataResult.getStatus().isSuccess()) return null;
		return mMetadataResult.getMetadata();		
	}
	
	
	public DriveFile createFile(File file) {
		ContentsResult result = Drive.DriveApi.newContents(mGoogleApiClient).await();
		if (!result.getStatus().isSuccess()) return null;
		
		MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
				.setTitle(file.getName())
				.setMimeType(getMimeType(file.getAbsolutePath()))
				.build();
		DriveFileResult mDriveFileResult = Drive.DriveApi
//				.getAppFolder(mGoogleApiClient)
				.getRootFolder(mGoogleApiClient)
				.createFile(mGoogleApiClient, changeSet,
						result.getContents()).await();
		return mDriveFileResult.getDriveFile();
	}
		
		
	
	
	
	private byte[] toByteArray(File file) {
		FileInputStream fileInputStream = null;

		byte[] bFile = new byte[(int) file.length()];

		try {
			fileInputStream = new FileInputStream(file);
			fileInputStream.read(bFile);
			fileInputStream.close();

			for (int i = 0; i < bFile.length; i++) {
				System.out.print((char) bFile[i]);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return bFile;
	}
	
	
	
	
	
	public boolean read (DriveFile mDriveFile, File file) {
		
		ContentsResult mContentsResult = mDriveFile.openContents(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null).await();
		if (!mContentsResult.getStatus().isSuccess()) return false;
		
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e1) {
			return false;
		}
		
		Contents contents = mContentsResult.getContents();
		BufferedReader reader = new BufferedReader(new InputStreamReader(contents.getInputStream()));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				writer.append(line);
			}
			
			return true;
			
		} catch (IOException e) {
			return false;
		} finally {
			try {
				writer.close();
				fos.close();
			} catch (IOException e) {}
		}

	}
	
	
	
	
	
	public String write (File file) {
		return write(file, getFile(file));
	}
	
	
	
	public String write(File file, DriveFile mDriveFile) {
		
		ContentsResult mContentsResult = mDriveFile.openContents(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null).await();
		if (!mContentsResult.getStatus().isSuccess()) return null;
		
        Contents contents = mContentsResult.getContents();
        try {	
            contents.getOutputStream().write(toByteArray(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Commits and closes 
        if (mDriveFile.commitAndCloseContents(mGoogleApiClient, contents).await().isSuccess()) {
        	return mDriveFile.getDriveId().getResourceId();
        } else {
        	return null;
        }
	}
	
	
	
	
	public List<Metadata> list() {
		DriveId driveId = Drive.DriveApi.getAppFolder(mGoogleApiClient).getDriveId();
		DriveFolder folder = Drive.DriveApi.getFolder(mGoogleApiClient, driveId);
		MetadataBufferResult mMetadataBufferResult = folder.listChildren(mGoogleApiClient).await();
		List<Metadata> list = getMetadata(mMetadataBufferResult);
		return list;		
	}
	
	
	
	public List<Metadata> searchByTitle(String title) {
		Query query = new Query.Builder()
		.addFilter(Filters.eq(SearchableField.TITLE, title))
		.addFilter(Filters.eq(SearchableField.TRASHED, false))
			.build();
		MetadataBufferResult mMetadataBufferResult = Drive.DriveApi.query(mGoogleApiClient, query).await();
		List<Metadata> list = getMetadata(mMetadataBufferResult);
		return list;		
	}
	
	
	
	private List<Metadata> getMetadata(MetadataBufferResult mMetadataBufferResult) {
		if (!mMetadataBufferResult.getStatus().isSuccess()) {
			return null;
		} 
		List<Metadata> list = new ArrayList<Metadata>();
		Iterator<Metadata> i = mMetadataBufferResult.getMetadataBuffer().iterator();
		Metadata mMetadata;
		while (i.hasNext()) {
			mMetadata = (Metadata) i.next();
			list.add(mMetadata);
		}
		return list;
	}
	
	
	public void delete(MetadataBuffer results) {
		DriveId driveId = Drive.DriveApi.getAppFolder(mGoogleApiClient).getDriveId();
    	for (Metadata metadata : results) {
//    		metadata.
		}
		
	}
	
		
	/**
	 * Tries to retrieve mime types from file extension
	 * @param url
	 * @return
	 */
	public static String getMimeType(String url) {
		String type = null;
		String extension = MimeTypeMap.getFileExtensionFromUrl(url);
		if (extension != null) {
			MimeTypeMap mime = MimeTypeMap.getSingleton();
			type = mime.getMimeTypeFromExtension(extension);
		}
		return type;
	}
}

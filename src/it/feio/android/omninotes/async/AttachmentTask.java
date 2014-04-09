package it.feio.android.omninotes.async;

import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.listeners.OnAttachingFileListener;
import it.feio.android.omninotes.utils.FileHelper;
import it.feio.android.omninotes.utils.StorageManager;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Locale;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;


public class AttachmentTask extends AsyncTask<Void, Void, Attachment> {

	private final WeakReference<Fragment> mFragmentWeakReference;
	private OnAttachingFileListener mOnAttachingFileListener;
	private Uri uri;

	public AttachmentTask(Fragment mFragment, Uri uri, OnAttachingFileListener mOnAttachingFileListener) {
		mFragmentWeakReference = new WeakReference<Fragment>(mFragment);
		this.uri = uri;
		this.mOnAttachingFileListener = mOnAttachingFileListener;
	}

	
	@Override
	protected Attachment doInBackground(Void... params) {
		
		String extension = FileHelper.getFileExtension(
				FileHelper.getNameFromUri(mFragmentWeakReference.get()
						.getActivity(), uri)).toLowerCase(Locale.getDefault());
		
		File f = StorageManager.createExternalStoragePrivateFile(
				mFragmentWeakReference.get().getActivity(), uri, extension);
		
		String mimeType = StorageManager.getMimeTypeInternal(
				mFragmentWeakReference.get().getActivity(), uri);
		
		if (f == null) return null;
		
		Attachment mAttachment = new Attachment(Uri.fromFile(f), mimeType);
		mAttachment.setMoveWhenNoteSaved(false);
		return mAttachment;
	}
	

	@Override
	protected void onPostExecute(Attachment mAttachment) {
		if (isAlive()) {
			if (mAttachment != null) {
				mOnAttachingFileListener.onAttachingFileFinished(mAttachment);
			} else {
				mOnAttachingFileListener.onAttachingFileErrorOccurred(mAttachment);
			}
		}
	}

	
	
	private boolean isAlive() {
		if (mFragmentWeakReference != null
				&& mFragmentWeakReference.get() != null
				&& mFragmentWeakReference.get().isAdded()
				&& mFragmentWeakReference.get().getActivity() != null
				&& !mFragmentWeakReference.get().getActivity().isFinishing()) {
			return true;
		}
		return false;
	}

}
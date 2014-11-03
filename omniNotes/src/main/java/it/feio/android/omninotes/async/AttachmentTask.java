package it.feio.android.omninotes.async;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import java.lang.ref.WeakReference;

import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.listeners.OnAttachingFileListener;
import it.feio.android.omninotes.utils.StorageManager;


public class AttachmentTask extends AsyncTask<Void, Void, Attachment> {

	private final WeakReference<Fragment> mFragmentWeakReference;
	private final Activity mActivity;
	private OnAttachingFileListener mOnAttachingFileListener;
	private Uri uri;
	private String fileName;

	public AttachmentTask(Fragment mFragment, Uri uri, OnAttachingFileListener mOnAttachingFileListener) {
		this(mFragment, uri, null, mOnAttachingFileListener);
	}

	public AttachmentTask(Fragment mFragment, Uri uri, String fileName, OnAttachingFileListener mOnAttachingFileListener) {
		mFragmentWeakReference = new WeakReference<Fragment>(mFragment);
		this.uri = uri;
		this.fileName = TextUtils.isEmpty(fileName) ? "" : fileName;
		this.mOnAttachingFileListener = mOnAttachingFileListener;
		this.mActivity = mFragment.getActivity();
	}

	
	@Override
	protected Attachment doInBackground(Void... params) {
		return StorageManager.createAttachmentFromUri(mActivity, uri);
	}
	

	@Override
	protected void onPostExecute(Attachment mAttachment) {
		if (isAlive()) {
			if (mAttachment != null) {
				mOnAttachingFileListener.onAttachingFileFinished(mAttachment);
			} else {
				mOnAttachingFileListener.onAttachingFileErrorOccurred(mAttachment);
			}
		} else {
			if (mAttachment != null) {
				StorageManager.delete(mActivity, mAttachment.getUri().getPath());
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
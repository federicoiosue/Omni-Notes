package it.feio.android.omninotes.models.listeners;

import it.feio.android.omninotes.models.Attachment;

public interface OnAttachingFileListener {
	public void onAttachingFileErrorOccurred(Attachment mAttachment);
	public void onAttachingFileFinished(Attachment mAttachment);
}

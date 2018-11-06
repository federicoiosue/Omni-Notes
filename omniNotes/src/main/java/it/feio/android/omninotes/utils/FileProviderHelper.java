package it.feio.android.omninotes.utils;

import android.net.Uri;
import android.support.v4.content.FileProvider;

import java.io.File;

import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.models.Attachment;

public class FileProviderHelper {

    /**
     * Generates the FileProvider URI for a given existing file
     */
    public static Uri getFileProvider(File file) {
        return FileProvider.getUriForFile(OmniNotes.getAppContext(), OmniNotes.getAppContext().getPackageName() + ".authority", file);
    }

    /**
     * Generates a shareable URI for a given attachment by evaluating its stored (into DB) path
     */
    public static Uri getShareableUri(Attachment attachment) {
        File attachmentFile = new File(attachment.getUri().getPath());
        if (attachmentFile.exists()) {
            return FileProviderHelper.getFileProvider(attachmentFile);
        } else {
            return attachment.getUri();
        }
    }
}

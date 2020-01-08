/*
 * Copyright (C) 2013-2020 Federico Iosue (federico@iosue.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.feio.android.omninotes.utils;

import android.net.Uri;
import androidx.core.content.FileProvider;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.models.Attachment;
import java.io.File;

public class FileProviderHelper {

  private FileProviderHelper () {
    // hides public constructor
  }

  /**
   * Generates the FileProvider URI for a given existing file
   */
  public static Uri getFileProvider (File file) {
    return FileProvider.getUriForFile(OmniNotes.getAppContext(),
        OmniNotes.getAppContext().getPackageName() + ".authority", file);
  }

  /**
   * Generates a shareable URI for a given attachment by evaluating its stored (into DB) path
   */
  public static Uri getShareableUri (Attachment attachment) {
    File attachmentFile = new File(attachment.getUri().getPath());
    if (attachmentFile.exists()) {
      return FileProviderHelper.getFileProvider(attachmentFile);
    } else {
      return attachment.getUri();
    }
  }
}

/*
 * Copyright (C) 2013-2023 Federico Iosue (federico@iosue.it)
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

import static androidx.core.content.FileProvider.getUriForFile;
import static it.feio.android.omninotes.OmniNotes.getAppContext;

import android.net.Uri;
import androidx.annotation.Nullable;
import it.feio.android.omninotes.models.Attachment;
import java.io.File;
import java.io.FileNotFoundException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FileProviderHelper {

  /**
   * Generates the FileProvider URI for a given existing file
   */
  public static Uri getFileProvider(File file) {
    return getUriForFile(getAppContext(), getAppContext().getPackageName() + ".authority", file);
  }

  /**
   * Generates a shareable URI for a given attachment by evaluating its stored (into DB) path
   */
  public static @Nullable Uri getShareableUri(Attachment attachment) throws FileNotFoundException {
    var uri = attachment.getUri();

    if (uri.getScheme().equals("content")
        && uri.getAuthority().equals(getAppContext().getPackageName() + ".authority")) {
      return uri;
    }

    File attachmentFile = new File(uri.getPath());
    if (!attachmentFile.exists()) {
      throw new FileNotFoundException("Required attachment not found in " + attachment.getUriPath());
    }
    return getFileProvider(attachmentFile);
  }

}

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

package it.feio.android.omninotes.helpers;

import static org.junit.Assert.assertEquals;

import android.net.Uri;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import it.feio.android.omninotes.BaseAndroidTestCase;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.StorageHelper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import rx.Observable;

@RunWith(AndroidJUnit4.class)
public class BackupHelperTest extends BaseAndroidTestCase {

  private File targetDir;
  private File targetAttachmentsDir;


  @Before
  public void setUp () throws Exception {
    targetDir = new File(StorageHelper.getCacheDir(InstrumentationRegistry.getInstrumentation().getTargetContext()), "_autobackupTest");
    if (targetDir.exists()) {
      FileUtils.forceDelete(targetDir);
    }
    targetAttachmentsDir = new File(targetDir, StorageHelper.getAttachmentDir().getName());
    targetAttachmentsDir.mkdirs();
  }

  @Test
  public void exportNote () {
    Note note = new Note();
    note.setTitle("test title");
    note.setContent("test content");
    long now = Calendar.getInstance().getTimeInMillis();
    note.setCreation(now);
    note.setLastModification(now);
    BackupHelper.exportNote(targetDir, note);
    Collection<File> noteFiles = FileUtils.listFiles(targetDir, new RegexFileFilter("\\d{13}.json"),
        TrueFileFilter.INSTANCE);
    assertEquals(1, noteFiles.size());
    Note retrievedNote = rx.Observable.from(noteFiles).map(BackupHelper::importNote).toBlocking().first();
    assertEquals(note, retrievedNote);
  }

  @Test
  public void exportNoteWithAttachment () throws IOException {
    Note note = new Note();
    note.setTitle("test title");
    note.setContent("test content");
    File testAttachment = File.createTempFile("testAttachment", ".txt");
    IOUtils.write("some test content for attachment".toCharArray(), new FileOutputStream(testAttachment));
    Attachment attachment = new Attachment(Uri.fromFile(testAttachment), "attachmentName");
    note.setAttachmentsList(Collections.singletonList(attachment));

    long now = Calendar.getInstance().getTimeInMillis();
    note.setCreation(now);
    note.setLastModification(now);
    BackupHelper.exportNote(targetDir, note);
    BackupHelper.exportAttachments(null, targetAttachmentsDir,
        note.getAttachmentsList(), note.getAttachmentsListOld());
    Collection<File> files = FileUtils.listFiles(targetDir, TrueFileFilter.TRUE, TrueFileFilter.TRUE);

    Note retrievedNote = rx.Observable.from(files).filter(file -> file.getName().equals(note
				.getCreation() + ".json")).map(BackupHelper::importNote).toBlocking().first();
    String retrievedAttachmentContent = Observable.from(files).filter(file -> file.getName().equals(FilenameUtils
        .getName(attachment.getUriPath()))).map(file -> {
      try {
        return FileUtils.readFileToString(file);
      } catch (IOException e) {
        return "bau";
      }
    }).toBlocking().first();
    assertEquals(2, files.size());
    assertEquals(note, retrievedNote);
    assertEquals(retrievedAttachmentContent, FileUtils.readFileToString(new File(attachment.getUri().getPath())));
  }


  @After
  public void tearDown () throws Exception {
    FileUtils.forceDelete(targetDir);
  }
}

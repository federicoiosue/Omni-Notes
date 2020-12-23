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

import static org.junit.Assert.*;

import android.net.Uri;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import it.feio.android.omninotes.BaseAndroidTestCase;
import it.feio.android.omninotes.exceptions.BackupException;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.StorageHelper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
  public void setUp() throws IOException {
    targetDir = new File(
        StorageHelper.getCacheDir(InstrumentationRegistry.getInstrumentation().getTargetContext()),
        "_autobackupTest");
    if (targetDir.exists()) {
      FileUtils.forceDelete(targetDir);
    }
    targetAttachmentsDir = new File(targetDir, StorageHelper.getAttachmentDir().getName());
    targetAttachmentsDir.mkdirs();
  }

  @After
  public void tearDown() throws Exception {
    FileUtils.forceDelete(targetDir);
  }

  @Test
  public void checkUtilityClassWellDefined() throws Exception {
    assertUtilityClassWellDefined(BackupHelper.class);
  }

  @Test
  public void exportNotes_nothingToExport() throws IOException {
    File backupDir = Files.createTempDirectory("testBackupFolder").toFile();

    BackupHelper.exportNotes(backupDir);

    assertTrue(backupDir.exists());
    assertEquals(0, backupDir.listFiles().length);
  }

  @Test
  public void exportNotes() throws IOException {
    Observable.range(1, 4).forEach(i -> createTestNote("Note" + i, "content" + i, 1));
    File backupDir = Files.createTempDirectory("testBackupFolder").toFile();

    BackupHelper.exportNotes(backupDir);

    assertTrue(backupDir.exists());
    assertEquals(4, backupDir.listFiles().length);
  }

  @Test
  public void exportNote() {
    Note note = createTestNote("test title", "test content", 0);

    BackupHelper.exportNote(targetDir, note);
    Collection<File> noteFiles = FileUtils.listFiles(targetDir, new RegexFileFilter("\\d{13}.json"),
        TrueFileFilter.INSTANCE);
    assertEquals(1, noteFiles.size());
    Note retrievedNote = rx.Observable.from(noteFiles).map(BackupHelper::importNote).toBlocking()
        .first();
    assertEquals(note, retrievedNote);
  }

  @Test
  public void exportNote_withAttachment() throws IOException {
    Note note = createTestNote("test title", "test content", 1);

    BackupHelper.exportNote(targetDir, note);
    BackupHelper.exportAttachments(null, targetAttachmentsDir,
        note.getAttachmentsList(), note.getAttachmentsListOld());
    Collection<File> files = FileUtils
        .listFiles(targetDir, TrueFileFilter.TRUE, TrueFileFilter.TRUE);

    Note retrievedNote = rx.Observable.from(files).filter(file -> file.getName().equals(note
        .getCreation() + ".json")).map(BackupHelper::importNote).toBlocking().first();
    String retrievedAttachmentContent = Observable.from(files)
        .filter(file -> file.getName().equals(FilenameUtils
            .getName(note.getAttachmentsList().get(0).getUriPath()))).map(file -> {
          try {
            return FileUtils.readFileToString(file);
          } catch (IOException e) {
            return "bau";
          }
        }).toBlocking().first();
    assertEquals(2, files.size());
    assertEquals(note, retrievedNote);
    assertEquals(retrievedAttachmentContent,
        FileUtils.readFileToString(new File(note.getAttachmentsList().get(0).getUri().getPath())));
  }

  @Test
  public void importAttachments() throws IOException {
    File testAttachment = new File(targetAttachmentsDir, "testAttachment");
    if (!testAttachment.createNewFile()) {
      throw new BackupException("Error during test", null);
    }
    Attachment attachment = new Attachment(
        Uri.fromFile(new File(StorageHelper.getAttachmentDir(), testAttachment.getName())),
        Constants.MIME_TYPE_FILES);
    dbHelper.updateAttachment(attachment);

    boolean result = BackupHelper.importAttachments(targetDir, null);

    assertTrue(result);
    assertTrue(new File(attachment.getUriPath().replace("file://", "")).exists());
  }
}

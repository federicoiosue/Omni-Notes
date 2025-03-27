package it.feio.android.omninotes.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.net.Uri;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import it.feio.android.omninotes.models.Attachment;
import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AttachmentsHelperTest {

  private Attachment testAttachment;

  @Before
  public void setUp() {
    Uri uri = Uri.fromFile(new File("/path/to/test/file"));
    testAttachment = new Attachment(uri, "image/jpeg");
    testAttachment.setSize(1024); // Set size to 1KB for testing
  }

  @Test
  public void testGetSize() {
    String expectedSize = "1 KB";
    String actualSize = AttachmentsHelper.getSize(testAttachment);
    assertEquals("The size should be 1 KB", expectedSize, actualSize);
  }

  @Test
  public void testGetSizeWithZeroSize() {
    testAttachment.setSize(0); // Simulate a zero size
    String actualSize = AttachmentsHelper.getSize(testAttachment);
    assertTrue("The size should be greater than 0", actualSize.contains("bytes"));
  }

  @Test
  public void testTypeOf() {
    assertTrue("The attachment should be of type image/jpeg",
        AttachmentsHelper.typeOf(testAttachment, "image/jpeg"));
  }

  @Test
  public void testTypeOfWithMultipleMimeTypes() {
    assertTrue("The attachment should be of type image/jpeg or image/png",
        AttachmentsHelper.typeOf(testAttachment, "image/jpeg", "image/png"));
  }
}
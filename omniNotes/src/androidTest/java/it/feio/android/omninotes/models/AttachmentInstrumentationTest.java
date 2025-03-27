package it.feio.android.omninotes.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.net.Uri;
import android.os.Parcel;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.feio.android.omninotes.commons.models.BaseAttachment;

@RunWith(AndroidJUnit4.class)
public class AttachmentInstrumentationTest {

  @Test
  public void testParcelableImplementation() {
    Uri uri = Uri.parse("content://test/uri");
    String mimeType = "image/png";
    Attachment attachment = new Attachment(uri, mimeType);

    Parcel parcel = Parcel.obtain();
    attachment.writeToParcel(parcel, 0);
    parcel.setDataPosition(0);

    Attachment createdFromParcel = Attachment.CREATOR.createFromParcel(parcel);
    assertNotNull(createdFromParcel);
    assertEquals(attachment.getId(), createdFromParcel.getId());
    assertEquals(attachment.getUri(), createdFromParcel.getUri());
    assertEquals(attachment.getMime_type(), createdFromParcel.getMime_type());
  }

  @Test
  public void testConstructorWithBaseAttachment() {
    Uri uri = Uri.parse("content://test/uri");
    String mimeType = "image/png";
    BaseAttachment baseAttachment = new BaseAttachment(1L, uri.toString(), "Test", 1024L, 0L, mimeType);
    Attachment attachment = new Attachment(baseAttachment);

    assertNotNull(attachment);
    assertEquals(baseAttachment.getId(), attachment.getId());
    assertEquals(baseAttachment.getUriPath(), attachment.getUri().toString());
    assertEquals(baseAttachment.getMime_type(), attachment.getMime_type());
  }
}
package it.feio.android.omninotes.factory;

import android.net.Uri;
import android.provider.MediaStore;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class MediaStoreFactoryInstrumentationTest {

    private MediaStoreFactory mediaStoreFactory;

    @Before
    public void setUp() {
        mediaStoreFactory = new MediaStoreFactory();
    }

    @Test
    public void testCreateURI_image() {
        Uri expectedUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Uri actualUri = mediaStoreFactory.createURI("image");
        assertEquals("URI should match for image type", expectedUri, actualUri);
    }

    @Test
    public void testCreateURI_video() {
        Uri expectedUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Uri actualUri = mediaStoreFactory.createURI("video");
        assertEquals("URI should match for video type", expectedUri, actualUri);
    }

    @Test
    public void testCreateURI_audio() {
        Uri expectedUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri actualUri = mediaStoreFactory.createURI("audio");
        assertEquals("URI should match for audio type", expectedUri, actualUri);
    }

    @Test
    public void testCreateURI_invalidType() {
        Uri actualUri = mediaStoreFactory.createURI("invalid");
        assertNull("URI should be null for invalid type", actualUri);
    }
}
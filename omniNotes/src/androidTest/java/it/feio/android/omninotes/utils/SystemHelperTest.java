package it.feio.android.omninotes.utils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.ClipData;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.Closeable;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.feio.android.omninotes.utils.SystemHelper;

@RunWith(AndroidJUnit4.class)
public class SystemHelperTest {

    @Mock
    private Context contextMock;

    @Mock
    private ClipboardManager clipboardManagerMock;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(contextMock.getSystemService(Context.CLIPBOARD_SERVICE)).thenReturn(clipboardManagerMock);
    }

    @Test
    public void testCloseCloseable() throws IOException {
        Closeable closeableMock = mock(Closeable.class);
        SystemHelper.closeCloseable(closeableMock);
        verify(closeableMock, times(1)).close();

        // Test with null closeable
        SystemHelper.closeCloseable((Closeable) null);
    }

    @Test
    public void testCloseCloseableWithException() throws IOException {
        Closeable closeableMock = mock(Closeable.class);
        IOException testException = new IOException("Test exception");
        doThrow(testException).when(closeableMock).close();

        // Should not throw exception
        SystemHelper.closeCloseable(closeableMock);
        verify(closeableMock, times(1)).close();

        // Try with array of closeables
        Closeable[] closeables = new Closeable[]{closeableMock};
        SystemHelper.closeCloseable(closeables);
        // Should be called twice total (once from above, once from array)
        verify(closeableMock, times(2)).close();
    }

    @Test
    public void testCopyToClipboard() {
        String text = "Sample text";
        SystemHelper.copyToClipboard(contextMock, text);

        // Using argument captor to verify the ClipData content
        ArgumentCaptor<ClipData> clipDataCaptor = ArgumentCaptor.forClass(ClipData.class);
        verify(clipboardManagerMock, times(1)).setPrimaryClip(clipDataCaptor.capture());

        ClipData capturedClipData = clipDataCaptor.getValue();
        assertEquals("text label", capturedClipData.getDescription().getLabel());
        assertEquals(text, capturedClipData.getItemAt(0).getText());
        assertEquals(1, capturedClipData.getItemCount());
        assertNotNull(capturedClipData.getItemAt(0));
    }

    @Test
    public void testCopyToClipboardWithEmptyText() {
        String text = "";
        SystemHelper.copyToClipboard(contextMock, text);

        // Should still copy empty text
        ArgumentCaptor<ClipData> clipDataCaptor = ArgumentCaptor.forClass(ClipData.class);
        verify(clipboardManagerMock, times(1)).setPrimaryClip(clipDataCaptor.capture());

        ClipData capturedClipData = clipDataCaptor.getValue();
        assertEquals("text label", capturedClipData.getDescription().getLabel());
        assertEquals("", capturedClipData.getItemAt(0).getText().toString());
    }
}

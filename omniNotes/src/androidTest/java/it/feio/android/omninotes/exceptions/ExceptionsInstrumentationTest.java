package it.feio.android.omninotes.exceptions;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.feio.android.omninotes.exceptions.unchecked.ExternalDirectoryCreationException;
import it.feio.android.omninotes.exceptions.checked.UnhandledIntentException;

import static org.junit.Assert.assertThrows;

@RunWith(AndroidJUnit4.class)
public class ExceptionsInstrumentationTest {

    @Test
    public void testGenericException() {
        assertThrows(GenericException.class, () -> {
            throw new GenericException("Test GenericException");
        });
    }

    @Test
    public void testDatabaseException() {
        assertThrows(DatabaseException.class, () -> {
            throw new DatabaseException("Test DatabaseException", new Exception("Cause"));
        });
    }

    @Test
    public void testBackupException() {
        assertThrows(BackupException.class, () -> {
            throw new BackupException("Test BackupException", new Exception("Cause"));
        });
    }

    @Test
    public void testNotesLoadingException() {
        assertThrows(NotesLoadingException.class, () -> {
            throw new NotesLoadingException("Test NotesLoadingException", new Exception("Cause"));
        });
    }

    @Test
    public void testUnhandledIntentException() {
        assertThrows(UnhandledIntentException.class, () -> {
            throw new UnhandledIntentException();
        });
    }

    @Test
    public void testExternalDirectoryCreationException() {
        assertThrows(ExternalDirectoryCreationException.class, () -> {
            throw new ExternalDirectoryCreationException("Test ExternalDirectoryCreationException");
        });
    }
}
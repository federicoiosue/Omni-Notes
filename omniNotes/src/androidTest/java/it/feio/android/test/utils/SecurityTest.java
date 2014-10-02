package it.feio.android.test.utils;

import android.test.InstrumentationTestCase;

import it.feio.android.omninotes.utils.Security;

public class SecurityTest extends InstrumentationTestCase {

    private final String PASS = "12345uselessPasswords";
    private final String TEXT = "Today is a good day to test useless things!";

    public void testEncrypt(){
        String encryptedText = Security.encrypt(TEXT, PASS);
        assertFalse(TEXT.equals(encryptedText));
    }

    public void testDecrypt(){
        String encryptedText = Security.encrypt(TEXT, PASS);
        assertEquals(TEXT, Security.decrypt(encryptedText, PASS));
        assertFalse(TEXT.equals(Security.decrypt(encryptedText, "zaza" + PASS)));
    }
}

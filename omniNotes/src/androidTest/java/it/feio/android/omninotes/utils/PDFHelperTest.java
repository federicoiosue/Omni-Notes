package it.feio.android.omninotes.utils;

import static org.junit.Assert.assertEquals;

import android.os.Environment;

import it.feio.android.omninotes.helpers.NoteToPdfHelper;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.testutils.BaseAndroidTestCase;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class PDFHelperTest extends BaseAndroidTestCase {

    @Before
    public void SetUp(){
        Note testNote = new Note();
        testNote.setTitle("a test note to convert to a pdf");
        testNote.setContent("this is a text so that the pdf file can be created\n some more info can be put here");
    }

    @Test
    public void FillePathMadeCorrectly(){
        NoteToPdfHelper pdfHelper = new NoteToPdfHelper();
        File file = pdfHelper.CreateFillePath("aTestPath");
        File controlFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),"aTestPath.pdf");
        assertEquals(file,controlFile);
    }
}

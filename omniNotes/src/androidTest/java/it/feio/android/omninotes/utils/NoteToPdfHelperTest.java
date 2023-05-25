package it.feio.android.omninotes.utils;

import android.graphics.pdf.PdfDocument;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import it.feio.android.omninotes.helpers.NoteToPdfHelper;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.testutils.BaseAndroidTestCase;

@RunWith(AndroidJUnit4.class)
public class NoteToPdfHelperTest extends BaseAndroidTestCase {

    private NoteToPdfHelper pdfConverter;
    private Note note;

    @Before
    public void setup(){
        pdfConverter = new NoteToPdfHelper();
        note = new Note();
    }

    @Test
    public void testNotePdfCreateWithSpaces(){
        note.setContent("this is a test note");
        note.setTitle("test with spaces in name");
        File file = pdfConverter.createFilePath(note.getTitle());
        File filePath = new File("/storage/emulated/0/Documents/test with spaces in name.pdf");
        Assert.assertEquals(file,filePath);
    }

    @Test
    public void testNotePdfCreateWithoutSpaces(){
        note.setContent("this is a test note");
        note.setTitle("testWithoutSpacesInName");
        File file = pdfConverter.createFilePath(note.getTitle());
        File filePath = new File("/storage/emulated/0/Documents/testWithoutSpacesInName.pdf");
        Assert.assertEquals(file,filePath);
    }

    @Test
    public void testNotePdfCreateWithOtherTypeCharSpaces(){
        note.setContent("this is a test note");
        note.setTitle("test.WithoutSpacesInName");
        File file = pdfConverter.createFilePath(note.getTitle());
        Assert.assertNull(file);
    }

    @Test
    public void testIfPdfIsCreated(){
        PdfDocument pdf = new PdfDocument();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(524,830,1).create();
        PdfDocument.Page page = pdf.startPage(pageInfo);

        pdf.finishPage(page);
        File file = pdfConverter.createFilePath(note.getTitle());
        pdfConverter.writePdfToFillePath(pdf,file);
        Assert.assertTrue(file.exists());
    }
    
}

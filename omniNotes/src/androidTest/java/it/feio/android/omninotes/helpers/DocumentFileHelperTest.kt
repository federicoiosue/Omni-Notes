/*
 * Copyright (C) 2013-2023 Federico Iosue (federico@iosue.it)
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

package it.feio.android.omninotes.helpers

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lazygeniouz.dfc.file.DocumentFileCompat
import it.feio.android.omninotes.testutils.BaseAndroidTestCase
import org.apache.commons.io.IOUtils.readLines
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class DocumentFileHelperTest : BaseAndroidTestCase() {

    private val text: String = "some content for the attachment"
    private lateinit var documentFile: DocumentFileCompat

    @Before
    fun setUp() {
        documentFile = DocumentFileCompat.Companion.fromFile(testContext, File.createTempFile("tempFile", "txt"))
        assertTrue(documentFile.exists())
    }

    @Test
    fun readContent() {
        writeIntoFile()
        val riddenText = DocumentFileHelper.readContent(testContext, documentFile);

        assertNotNull(riddenText)
        assertEquals(text, riddenText)
    }

    @Test
    fun write() {
        DocumentFileHelper.write(testContext, documentFile, text);

        val riddentText = readLines(testContext.contentResolver.openInputStream(documentFile.uri)).stream().reduce { t, u -> t + u }

        assertTrue(riddentText.isPresent)
        assertEquals(text, riddentText.get())
    }

    @Test
    fun delete() {
        DocumentFileHelper.delete(documentFile);

        assertFalse(documentFile.exists())
    }

    @Test
    fun copyFileTo() {
        writeIntoFile()
        val destinationFile = File.createTempFile("copyFileTo", Calendar.getInstance().timeInMillis.toString())

        DocumentFileHelper.copyFileTo(testContext, documentFile, destinationFile);

        assertEquals(text, destinationFile.readText())
    }

    @Test
    @Ignore("runned manually")
    fun copyFileTo_performance() {
        for (i in 1..50) copyFileTo()

        assertTrue(true)
    }

    private fun writeIntoFile() {
        val os = testContext.contentResolver.openOutputStream(documentFile.uri)
        os?.write(text.toByteArray())
    }

}
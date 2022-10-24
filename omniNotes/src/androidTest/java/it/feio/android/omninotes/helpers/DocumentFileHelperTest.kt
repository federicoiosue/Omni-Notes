/*
 * Copyright (C) 2013-2022 Federico Iosue (federico@iosue.it)
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

import androidx.documentfile.provider.DocumentFile
import androidx.test.ext.junit.runners.AndroidJUnit4
import it.feio.android.omninotes.BaseAndroidTestCase
import org.apache.commons.io.IOUtils.readLines
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class DocumentFileHelperTest : BaseAndroidTestCase() {

    private lateinit var documentFile: DocumentFile

    @Before
    fun setUp() {
        documentFile = DocumentFile.fromFile(File.createTempFile("tempFile", "txt"));
        assertTrue(documentFile.exists())
    }

    @Test
    fun readContent() {
        val text = "some text"
        val os = testContext.contentResolver.openOutputStream(documentFile.uri)
        os?.write(text.toByteArray())

        val riddenText = DocumentFileHelper.readContent(testContext, documentFile);

        assertNotNull(riddenText)
        assertEquals(text, riddenText)
    }

    @Test
    fun write() {
        val text = "some text"
        DocumentFileHelper.write(testContext, documentFile, text);

        val riddentText = readLines(testContext.contentResolver.openInputStream(documentFile.uri)).stream().reduce { t, u -> t + u }

        assertTrue(riddentText.isPresent)
        assertEquals(text, riddentText.get())
    }

    @Test
    fun delete() {
        DocumentFileHelper.delete(testContext, documentFile);

        assertFalse(documentFile.exists())
    }
}
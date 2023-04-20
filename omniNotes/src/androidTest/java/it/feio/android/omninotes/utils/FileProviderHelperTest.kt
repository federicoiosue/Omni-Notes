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

package it.feio.android.omninotes.utils


import android.net.Uri
import androidx.core.net.toUri
import it.feio.android.omninotes.OmniNotes.getAppContext
import it.feio.android.omninotes.models.Attachment
import it.feio.android.omninotes.testutils.BaseAndroidTestCase
import it.feio.android.omninotes.utils.ConstantsBase.MIME_TYPE_IMAGE
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class FileProviderHelperTest : BaseAndroidTestCase() {

    @Test
    fun getShareableUri() {
        val file = File("/storage/emulated/0/Android/data/it.feio.android.omninotes/files/424242.png")
        assertTrue(file.createNewFile() || file.exists())
        val attachment = Attachment(file.toUri(), MIME_TYPE_IMAGE)

        val res = FileProviderHelper.getShareableUri(attachment)

        assertNotNull(res)
        assertNotEquals(file.toUri(), res)
        assertTrue(res?.scheme.equals("content") && res?.authority.equals(getAppContext().packageName + ".authority"))

        file.deleteOnExit()
    }

    @Test
    fun getShareableUri_contentScheme() {
        val uri = "content://it.feio.android.omninotes.authority/external_files/Android/data/it.feio.android.omninotes/files/20230418_091959_730.jpeg"
        val attachment = Attachment(Uri.parse(uri), "")

        val res = FileProviderHelper.getShareableUri(attachment)

        assertEquals(uri, res.toString())
    }

}
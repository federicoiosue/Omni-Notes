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


import it.feio.android.omninotes.OmniNotes.getAppContext
import it.feio.android.omninotes.testutils.BaseAndroidTestCase
import org.junit.Assert.*
import org.junit.Test

class FileProviderHelperTest : BaseAndroidTestCase() {

    @Test
    fun getShareableUri() {
        val attachment = createTestAttachment("testAttachment.txt")

        val res = FileProviderHelper.getShareableUri(attachment)

        assertNotNull(res)
        assertNotEquals(attachment.uri, res)
        assertTrue(res?.scheme.equals("content") && res?.authority.equals(getAppContext().packageName + ".authority"))
    }

}
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

package it.feio.android.omninotes.async.upgrade


import it.feio.android.omninotes.testutils.BaseAndroidTestCase
import it.feio.android.omninotes.utils.FileProviderHelper.getShareableUri
import it.feio.android.omninotes.utils.StorageHelper.createAttachmentFromUri
import org.junit.Assert.*
import org.junit.Test
import rx.Observable.from

class UpgradeProcessorTest : BaseAndroidTestCase() {

    @Test
    fun onUpgradeTo625() {
        // Preparation of database state existent pre-612 version.
        // Attachment used to be stored with "content://" scheme that allowed sharing but broke backups.
        val note = createTestNote("t", "c", 1)
        var attachment = createAttachmentFromUri(testContext, note.attachmentsList[0].uri)
        attachment?.uri = getShareableUri(attachment)
        note.attachmentsList[0] = attachment
        dbHelper.updateNote(note, false)

        assertFalse(from(dbHelper.allAttachments).all { a -> a.uri.scheme != "content" }.toBlocking().single())

        UpgradeProcessor.process(624, 625)

        assertTrue(from(dbHelper.allAttachments).all { a -> a.uri.scheme != "content" }.toBlocking().single())
    }

}

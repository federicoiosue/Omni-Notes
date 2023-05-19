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

import androidx.test.ext.junit.runners.AndroidJUnit4
import it.feio.android.omninotes.exceptions.checked.ContentSecurityException
import it.feio.android.omninotes.testutils.BaseAndroidTestCase
import it.feio.android.omninotes.utils.Security.Companion.decrypt
import it.feio.android.omninotes.utils.Security.Companion.encrypt
import it.feio.android.omninotes.utils.Security.Companion.validatePath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecurityTest : BaseAndroidTestCase() {
    private val LOREM = ("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor"
            + " incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco"
            + " laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit "
            + "esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa "
            + "qui officia deserunt mollit anim ID est laborum.")

    @Test
    @Throws(Exception::class)
    fun checkUtilityClassWellDefined() {
        assertUtilityClassWellDefined(Security::class.java)
    }

    @Test
    fun encrypt() {
        assertNotEquals(TEXT, encrypt(TEXT, PASS))
    }

    @Test
    fun decrypt() {
        val encryptedText = encrypt(TEXT, PASS)

        assertEquals(TEXT, decrypt(encryptedText, PASS))
        assertNotEquals(TEXT, decrypt(encryptedText, "zaza$PASS"))
    }

    @Test
    fun decryptUnencrypted() {
        assertNotEquals(0, decrypt(LOREM, PASS)!!.length.toLong())
    }

    @Test
    fun validatePath_startsWithData() {
        val path = "file:///data/data/it.feio.android.omninotes.foss/shared_prefs/it.feio.android.omninotes.foss_preferences.xml"

        assertThrows(ContentSecurityException::class.java) { validatePath(path) }
    }

    @Test
    fun validatePath_pathTraversal() {
        val path = "file:///storage/emulated/0/../../../data/data/it.feio.android.omninotes.foss/shared_prefs/it.feio.android.omninotes.foss_preferences.xml"

        assertThrows(ContentSecurityException::class.java) { validatePath(path) }
    }

    @Test
    fun validatePath_valid() {
        val path = "/images/screenshot/16844742322307525633366385236595.jpg"

        validatePath(path)
    }

    companion object {
        private const val PASS = "12345uselessPasswords"
        private const val TEXT = "Today is a - good - day to test useless things!"
    }
}
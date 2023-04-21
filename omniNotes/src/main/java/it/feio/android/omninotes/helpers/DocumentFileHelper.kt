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

import android.content.Context
import android.net.Uri
import com.lazygeniouz.dfc.file.DocumentFileCompat
import lombok.experimental.UtilityClass
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.IOException

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

@UtilityClass
class DocumentFileHelper {

    companion object {
        @JvmStatic
        @Throws(IOException::class)
        fun readContent(context: Context, documentFile: DocumentFileCompat): String {
            context.contentResolver.openInputStream(documentFile.uri).use { `is` ->
                return IOUtils.toString(`is`)
            }
        }

        @JvmStatic
        fun copyFileTo(context: Context, file: DocumentFileCompat, destination: File?): Boolean {
            val contentResolver = context.contentResolver
            try {
                contentResolver.openInputStream(file.uri).use { `is` ->
                    contentResolver.openOutputStream(Uri.fromFile(destination)).use { os ->
                        IOUtils.copy(`is`, os)
                        return true
                    }
                }
            } catch (e: IOException) {
                LogDelegate.e("Error copying file", e)
                return false
            }
        }

        @JvmStatic
        fun copyFileTo(context: Context, file: File?, destination: DocumentFileCompat): Boolean {
            val contentResolver = context.contentResolver
            try {
                contentResolver.openInputStream(Uri.fromFile(file)).use { `is` ->
                    contentResolver.openOutputStream(destination.uri).use { os ->
                        IOUtils.copy(`is`, os)
                        return true
                    }
                }
            } catch (e: IOException) {
                LogDelegate.e("Error copying file", e)
                return false
            }
        }

        @JvmStatic
        @Throws(IOException::class)
        fun write(context: Context, file: DocumentFileCompat, content: String?) {
            val contentResolver = context.contentResolver
            contentResolver.openOutputStream(file.uri).use { os -> IOUtils.write(content, os) }
        }

        @JvmStatic
        @Throws(IOException::class)
        fun delete(file: DocumentFileCompat): Boolean {
            return file.delete()
        }
    }
}
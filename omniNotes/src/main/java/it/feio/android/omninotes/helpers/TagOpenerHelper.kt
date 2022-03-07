/*
 * Copyright (C) 2013-2021 Federico Iosue (federico@iosue.it)
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
import android.content.Intent
import android.net.Uri
import it.feio.android.omninotes.exceptions.checked.UnhandledIntentException
import it.feio.android.omninotes.utils.IntentChecker
import lombok.experimental.UtilityClass

@UtilityClass
class TagOpenerHelper {

    companion object {
        @JvmStatic
        @Throws(UnhandledIntentException::class)
        fun openOrGetIntent(context: Context, tagText: String): Intent? {
            val intent: Intent
            when (tagText.split(":".toRegex()).toTypedArray()[0]) {
                "tel" -> {
                    intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse(tagText)
                }
                "mailto" -> {
                    intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse(tagText)
                }
                "hashtag" -> {
                    intent = Intent(Intent.ACTION_VIEW, Uri.parse(tagText))
                    intent.addCategory(Intent.CATEGORY_BROWSABLE)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    return intent
                }
                else -> intent = Intent(Intent.ACTION_VIEW, Uri.parse(tagText))
            }
            if (IntentChecker.isAvailable(context, intent, null)) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                return null
            }
            throw UnhandledIntentException()
        }
    }
}
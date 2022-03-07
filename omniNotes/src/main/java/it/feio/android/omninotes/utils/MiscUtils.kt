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

package it.feio.android.omninotes.utils

import android.content.Context
import android.content.pm.PackageManager

class MiscUtils private constructor() {

    companion object {
        /**
         * Checks Google Play Store availability
         *
         * @param context Application context
         * @return True if Play Store is installed on the device
         */
        @JvmStatic
        fun isGooglePlayAvailable(context: Context): Boolean {
            return try {
                context.packageManager.getPackageInfo("com.android.vending", 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }
}
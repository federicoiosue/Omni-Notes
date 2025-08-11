/*
 * Copyright (C) 2013-2025 Federico Iosue (developer@omninotes.app)
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
package it.feio.android.omninotes.models.listeners

import android.location.Address
import android.location.Location


interface OnGeoUtilResultListener {

    fun onAddressResolved(address: String?)

    fun onCoordinatesResolved(address: Address?)
    fun onCoordinatesUnresolved(exception: Exception?)

    fun onLocationRetrieved(location: Location?)

    fun onLocationUnavailable(e: Exception?)
    fun onLocationNotEnabled()
}

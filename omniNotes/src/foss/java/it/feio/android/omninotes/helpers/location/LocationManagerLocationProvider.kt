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
package it.feio.android.omninotes.helpers.location

import com.google.android.gms.location.LocationServices
import it.feio.android.omninotes.OmniNotes
import it.feio.android.omninotes.models.listeners.OnGeoUtilResultListener

class FuseLocationProviderLocationManagerLocationProvider : LocationProvider {

    @kotlin.Throws(SecurityException::class)
    override fun getLocation(onGeoUtilResultListener: OnGeoUtilResultListener?) {
        val lastKnownLocationByGps =
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        lastKnownLocationByGps?.let {
            locationByGps = lastKnownLocationByGps
        }

        val lastKnownLocationByNetwork =
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        lastKnownLocationByNetwork?.let {
            locationByNetwork = lastKnownLocationByNetwork
        }

        if (locationByGps != null && locationByNetwork != null) {
            if (locationByGps.accuracy > locationByNetwork!!.accuracy) {
                currentLocation = locationByGps
                latitude = currentLocation.latitude
                longitude = currentLocation.longitude
                // use latitude and longitude as per your need
            } else {
                currentLocation = locationByNetwork
                latitude = currentLocation.latitude
                longitude = currentLocation.longitude
                // use latitude and longitude as per your need
            }
        }

    }

}

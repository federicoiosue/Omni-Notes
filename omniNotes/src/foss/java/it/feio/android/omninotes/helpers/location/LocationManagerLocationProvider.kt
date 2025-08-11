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

import android.content.Context
import android.location.LocationManager
import android.os.Looper
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import it.feio.android.omninotes.OmniNotes
import it.feio.android.omninotes.models.listeners.OnGeoUtilResultListener

class LocationManagerLocationProvider : LocationProvider {

    private var locationManager: LocationManager? = null
    private var listener: LocationListenerCompat? = null

    override fun instantiate() {
        locationManager = OmniNotes.getAppContext().getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    }

    @kotlin.Throws(SecurityException::class)
    override fun getLocation(onGeoUtilResultListener: OnGeoUtilResultListener?) {
        if (locationManager == null) {
            instantiate()
        }
        listener = object : LocationListenerCompat {
            override fun onLocationChanged(location: android.location.Location) {
                onGeoUtilResultListener?.onLocationRetrieved(location)
                locationManager?.removeUpdates(this)
            }
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        locationManager?.let {
            val locationRequest = LocationRequestCompat.Builder(5000L)
                .setMinUpdateDistanceMeters(10f)
                .setQuality(LocationRequestCompat.QUALITY_HIGH_ACCURACY)
                .build()
            LocationManagerCompat.requestLocationUpdates(
                it,
                LocationManager.GPS_PROVIDER,
                locationRequest,
                listener!!,
                Looper.getMainLooper()
            )
        }
    }

}

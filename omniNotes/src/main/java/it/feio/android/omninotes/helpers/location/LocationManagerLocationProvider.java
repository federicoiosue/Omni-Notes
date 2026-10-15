/*
 * Copyright (C) 2013-2026 Federico Iosue (omninotes@iosue.it)
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
package it.feio.android.omninotes.helpers.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.models.listeners.OnGeoUtilResultListener;
import java.util.concurrent.TimeUnit;

/**
 * Retrieves the device position using AOSP's {@link LocationManager} only, so no proprietary
 * libraries are needed and the very same code is used by all the app's flavors.
 */
public class LocationManagerLocationProvider implements LocationProvider {

  private static final long LISTENER_TIMEOUT = TimeUnit.SECONDS.toMillis(20);
  private static final long MIN_UPDATE_INTERVAL_MILLIS = 5000L;
  private static final float MIN_UPDATE_DISTANCE_METERS = 10f;

  private final LocationManager locationManager;
  private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
  private LocationListener listener;
  private OnGeoUtilResultListener callback;
  private boolean completed;

  public LocationManagerLocationProvider() {
    locationManager = (LocationManager) OmniNotes.getAppContext()
        .getSystemService(Context.LOCATION_SERVICE);
  }

  @Override
  public void getLocation(OnGeoUtilResultListener onGeoUtilResultListener) {
    callback = onGeoUtilResultListener;
    if (locationManager == null) {
      onGeoUtilResultListener.onLocationUnavailable(new IllegalStateException(
          "LocationManager not available"));
      return;
    }
    if (!areLocationPermissionsGranted()) {
      throw new SecurityException("Location permissions have not been granted");
    }

    var bestLastKnownLocation = getLastKnownLocation();
    if (bestLastKnownLocation != null) {
      complete(() -> callback.onLocationRetrieved(bestLastKnownLocation));
      return;
    }

    if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
      complete(callback::onLocationNotEnabled);
      return;
    }

    requestSingleUpdate(getBestProvider());
  }

  private boolean areLocationPermissionsGranted() {
    var context = OmniNotes.getAppContext();
    return ContextCompat.checkSelfPermission(context,
        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        || ContextCompat.checkSelfPermission(context,
        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
  }

  private boolean isFineLocationGranted() {
    return ContextCompat.checkSelfPermission(OmniNotes.getAppContext(),
        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
  }

  private String getBestProvider() {
    // GPS is usable only with the fine location permission: with coarse only, rely on network
    var criteria = new Criteria();
    criteria.setAccuracy(isFineLocationGranted()
        ? Criteria.ACCURACY_FINE
        : Criteria.ACCURACY_COARSE);
    var provider = locationManager.getBestProvider(criteria, true);
    if (provider == null) {
      provider = LocationManager.PASSIVE_PROVIDER;
    }
    return provider;
  }

  private Location getLastKnownLocation() {
    Location bestLocation = null;
    for (String provider : locationManager.getAllProviders()) {
      Location location;
      try {
        location = locationManager.getLastKnownLocation(provider);
      } catch (SecurityException | IllegalArgumentException e) {
        continue;
      }
      if (location != null && (bestLocation == null
          || location.getAccuracy() < bestLocation.getAccuracy())) {
        bestLocation = location;
      }
    }
    return bestLocation;
  }

  // Permissions are checked by the caller before instantiation (see LocationProvider contract)
  @android.annotation.SuppressLint("MissingPermission")
  private void requestSingleUpdate(String provider) {
    if (!LocationManagerCompat.hasProvider(locationManager, provider)) {
      complete(() -> callback.onLocationUnavailable(
          new IllegalArgumentException("No location provider available")));
      return;
    }

    listener = new LocationListener() {
      @Override
      public void onLocationChanged(@NonNull Location location) {
        complete(() -> callback.onLocationRetrieved(location));
      }

      @Override
      public void onProviderDisabled(@NonNull String provider) {
        complete(() -> callback.onLocationUnavailable(
            new IllegalStateException("Provider " + provider + " disabled")));
      }

      @Override
      public void onProviderEnabled(@NonNull String provider) {
        // Nothing to do
      }

      @Override
      public void onStatusChanged(String provider, int status, Bundle extras) {
        // Deprecated, nothing to do
      }
    };

    locationManager.requestLocationUpdates(provider, MIN_UPDATE_INTERVAL_MILLIS,
        MIN_UPDATE_DISTANCE_METERS, listener, Looper.getMainLooper());

    // Safety net to avoid leaving the user waiting forever without any feedback
    timeoutHandler.postDelayed(() -> {
      if (!completed) {
        complete(() -> callback.onLocationUnavailable(
            new IllegalStateException("Location retrieval timed out")));
      }
    }, LISTENER_TIMEOUT);
  }

  private synchronized void complete(Runnable action) {
    if (completed) {
      return;
    }
    completed = true;
    cleanup();
    action.run();
  }

  private void cleanup() {
    timeoutHandler.removeCallbacksAndMessages(null);
    if (listener != null && locationManager != null) {
      locationManager.removeUpdates(listener);
      listener = null;
    }
  }

}

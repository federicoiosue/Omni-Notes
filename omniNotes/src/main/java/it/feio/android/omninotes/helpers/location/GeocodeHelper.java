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

package it.feio.android.omninotes.helpers.location;

import static android.os.Build.VERSION_CODES.TIRAMISU;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.helpers.BuildHelper;
import it.feio.android.omninotes.models.listeners.OnGeoUtilResultListener;
import java.io.IOException;
import java.util.Locale;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;


@UtilityClass
public class GeocodeHelper {

  public String getAddressFromCoordinates(Context mContext, double latitude,
      double longitude) throws IOException {
    var geocoder = new Geocoder(mContext, Locale.getDefault());
    var addresses = geocoder.getFromLocation(latitude, longitude, 1);
    if (addresses != null && !addresses.isEmpty()) {
      Address address = addresses.get(0);
      if (address != null) {
        return address.getThoroughfare() + ", " + address.getLocality();
      }
    }
    return "";
  }

  public void getAddressFromCoordinates(Location location,
      final OnGeoUtilResultListener onGeoUtilResultListener) {
    try {
      var address= getAddressFromCoordinates(OmniNotes.getAppContext(), location.getLatitude(),
          location.getLongitude());
      onGeoUtilResultListener.onAddressResolved(address);
    } catch (IOException e) {
      onGeoUtilResultListener.onLocationUnavailable(e);
    }
  }

  public void getCoordinatesFromAddress(String address, final OnGeoUtilResultListener listener) {
    try {
      var geocoder = new Geocoder(OmniNotes.getAppContext(), Locale.getDefault());
      if (BuildHelper.isAboveOrEqual(TIRAMISU)) {
        geocoder.getFromLocationName(address, 1, addresses -> listener.onCoordinatesResolved(addresses.get(0)));
      } else {
        var addresses = geocoder.getFromLocationName(address, 1);
        if (addresses != null && !addresses.isEmpty()) {
          listener.onCoordinatesResolved(addresses.get(0));
        }
      }
    } catch (IOException e) {
      listener.onCoordinatesUnresolved(e);
    }
  }

  public static boolean areCoordinates(String string) {
    var p = Pattern.compile(
        "^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?),\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)$");
    return p.matcher(string).matches();
  }

  /**
   * Checks for location provider between {@link android.location.LocationManager#GPS_PROVIDER}
   * etc...
   */
  public static boolean checkLocationProviderEnabled(Context context, String provider) {
    var locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    return locationManager.isProviderEnabled(provider);
  }

}

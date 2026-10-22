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


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;


@UtilityClass
public class GeocodeHelper {

  /**
   * Translates coordinates into a human readable address using the platform geocoder, so no
   * proprietary library (nor internet permission) is needed.
   *
   * @return the resolved address or an empty string if it cannot be resolved
   */
  public String getAddressFromCoordinates(Context mContext, double latitude,
      double longitude) throws IOException {
    if (!Geocoder.isPresent()) {
      return "";
    }
    var geocoder = new Geocoder(mContext, Locale.getDefault());
    var addresses = geocoder.getFromLocation(latitude, longitude, 1);
    if (addresses == null || addresses.isEmpty()) {
      return "";
    }
    return formatAddress(addresses.get(0));
  }

  /**
   * Builds the most readable representation of the given address.
   *
   * @return the formatted address or an empty string if no meaningful data is available
   */
  public String formatAddress(Address address) {
    if (address == null) {
      return "";
    }
    return formatAddress(address.getAddressLine(0), address.getThoroughfare(),
        address.getLocality());
  }

  /**
   * Builds the most readable representation of the given address parts, preferring the full
   * address line when available.
   *
   * @return the formatted address or an empty string if no meaningful data is available
   */
  public String formatAddress(String addressLine, String thoroughfare, String locality) {
    if (!isBlank(addressLine)) {
      return addressLine;
    }
    List<String> parts = new ArrayList<>();
    if (!isBlank(thoroughfare)) {
      parts.add(thoroughfare);
    }
    if (!isBlank(locality)) {
      parts.add(locality);
    }
    return String.join(", ", parts);
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  public static boolean areCoordinates(String string) {
    var p = Pattern.compile(
        "^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?),\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)$");
    return p.matcher(string).matches();
  }

}

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


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
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

  public static boolean areCoordinates(String string) {
    var p = Pattern.compile(
        "^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?),\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)$");
    return p.matcher(string).matches();
  }

}

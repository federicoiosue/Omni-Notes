/*
 * Copyright (C) 2013-2020 Federico Iosue (federico@iosue.it)
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

package it.feio.android.omninotes.helpers;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;
import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesWithFallbackProvider;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.utils.GeocodeHelper;

public class GeocodeProviderBaseFactory {

  protected GeocodeProviderBaseFactory() {
    // hides public constructor
  }

  public static LocationProvider getProvider(Context context) {
    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        && android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.P
        && checkHighAccuracyLocationProvider(context)) {
      Toast.makeText(context, R.string.location_set_high_accuracy, Toast.LENGTH_SHORT).show();
      context.startActivity((new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)));
    }

    return new LocationGooglePlayServicesWithFallbackProvider(context);
  }

  public static boolean checkHighAccuracyLocationProvider(Context context) {
    return GeocodeHelper.checkLocationProviderEnabled(context, LocationManager.GPS_PROVIDER);
  }

}

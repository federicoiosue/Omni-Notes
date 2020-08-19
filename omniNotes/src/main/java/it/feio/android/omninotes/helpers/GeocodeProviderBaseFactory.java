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

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesWithFallbackProvider;

public class GeocodeProviderBaseFactory {

  protected GeocodeProviderBaseFactory() {
    // hides public constructor
  }

  public static LocationProvider getProvider (Context context) {
    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT&&android.os.Build.VERSION.SDK_INT <Build.VERSION_CODES.P)
    {
      if(getLocationMode(context)!=3)
      {
        context.startActivity((new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)));
      }
    }

    return new LocationGooglePlayServicesWithFallbackProvider(context);
  }
  public static int getLocationMode(Context context)  {
    ContentResolver contentResolver=(ContentResolver)context.getContentResolver();
    try {
      return Settings.Secure.getInt(contentResolver, Settings.Secure.LOCATION_MODE);
    } catch (Settings.SettingNotFoundException e) {
      e.printStackTrace();
    }
    return 0;
  }
}

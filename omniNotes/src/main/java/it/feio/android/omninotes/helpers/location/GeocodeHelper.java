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
import static it.feio.android.omninotes.BuildConfig.MAPS_API_KEY;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.text.TextUtils;

import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.helpers.BuildHelper;
import it.feio.android.omninotes.helpers.LogDelegate;
import it.feio.android.omninotes.models.listeners.OnGeoUtilResultListener;
import it.feio.android.omninotes.utils.SystemHelper;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


@UtilityClass
public class GeocodeHelper {

  private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
  private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
  private static final String OUT_JSON = "/json";

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

  public static List<String> autocomplete(String input) {
    if (TextUtils.isEmpty(MAPS_API_KEY)) {
      return Collections.emptyList();
    }

    ArrayList<String> resultList = null;

    HttpURLConnection conn = null;
    InputStreamReader in = null;
    StringBuilder jsonResults = new StringBuilder();
    try {
      var url = new URL(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON + "?key="
          + MAPS_API_KEY + "&input=" + URLEncoder.encode(input, "utf8"));
      conn = (HttpURLConnection) url.openConnection();
      in = new InputStreamReader(conn.getInputStream());
      // Load the results into a StringBuilder
      int read;
      char[] buff = new char[1024];
      while ((read = in.read(buff)) != -1) {
        jsonResults.append(buff, 0, read);
      }
    } catch (MalformedURLException e) {
      LogDelegate.e("Error processing Places API URL");
      return Collections.emptyList();
    } catch (IOException e) {
      LogDelegate.e("Error connecting to Places API");
      return Collections.emptyList();
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          LogDelegate.e("Error closing address autocompletion InputStream");
        }
      }
    }

    try {
      // Create a JSON object hierarchy from the results
      JSONObject jsonObj = new JSONObject(jsonResults.toString());
      JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
      // Extract the Place descriptions from the results
      resultList = new ArrayList<>(predsJsonArray.length());
      for (int i = 0; i < predsJsonArray.length(); i++) {
        resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
      }
    } catch (JSONException e) {
      LogDelegate.e("Cannot process JSON results", e);
    } finally {
      conn.disconnect();
      SystemHelper.closeCloseable(in);
    }
    return resultList;
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

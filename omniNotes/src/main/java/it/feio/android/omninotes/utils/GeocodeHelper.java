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

package it.feio.android.omninotes.utils;

import static it.feio.android.omninotes.BuildConfig.MAPS_API_KEY;
import static it.feio.android.omninotes.helpers.GeocodeProviderBaseFactory.getProvider;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.text.TextUtils;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.rx.ObservableFactory;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.helpers.LogDelegate;
import it.feio.android.omninotes.models.listeners.OnGeoUtilResultListener;
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
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.Subscriber;


public class GeocodeHelper implements LocationListener {

  private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
  private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
  private static final String OUT_JSON = "/json";

  @Override
  public void onLocationChanged (Location newLocation) {
    // Nothing to do
  }


  @Override
  public void onStatusChanged (String provider, int status, Bundle extras) {
    // Nothing to do
  }


  @Override
  public void onProviderEnabled (String provider) {
    // Nothing to do
  }


  @Override
  public void onProviderDisabled (String provider) {
    // Nothing to do
  }


  public static void getLocation (OnGeoUtilResultListener onGeoUtilResultListener) {
    SmartLocation.LocationControl bod = SmartLocation.with(OmniNotes.getAppContext())
                                                     .location(getProvider(OmniNotes.getAppContext()))
                                                     .config(LocationParams.NAVIGATION).oneFix();

    Observable<Location> locations = ObservableFactory.from(bod).timeout(2, TimeUnit.SECONDS);
    locations.subscribe(new Subscriber<Location>() {
      @Override
      public void onNext (Location location) {
        onGeoUtilResultListener.onLocationRetrieved(location);
        unsubscribe();
      }

      @Override
      public void onCompleted () {
        // Nothing to do
      }

      @Override
      public void onError (Throwable e) {
        onGeoUtilResultListener.onLocationUnavailable();
        unsubscribe();
      }
    });
  }


  public static void stop () {
    SmartLocation.with(OmniNotes.getAppContext()).location().stop();
    if (Geocoder.isPresent()) {
      SmartLocation.with(OmniNotes.getAppContext()).geocoding().stop();
    }
  }


  static String getAddressFromCoordinates (Context mContext, double latitude,
      double longitude) throws IOException {
    String addressString = "";
    Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
    if (!addresses.isEmpty()) {
      Address address = addresses.get(0);
      if (address != null) {
        addressString = address.getThoroughfare() + ", " + address.getLocality();
      }
    }
    return addressString;
  }


  public static void getAddressFromCoordinates (Location location,
      final OnGeoUtilResultListener onGeoUtilResultListener) {
    if (!Geocoder.isPresent()) {
      onGeoUtilResultListener.onAddressResolved("");
    } else {
      SmartLocation.with(OmniNotes.getAppContext()).geocoding().reverse(location, (location1, list) -> {
        String address = list.size() > 0 ? list.get(0).getAddressLine(0) : null;
        onGeoUtilResultListener.onAddressResolved(address);
      });
    }
  }


  public static double[] getCoordinatesFromAddress (Context mContext, String address)
      throws IOException {
    double[] result = new double[2];
    Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
    List<Address> addresses = geocoder.getFromLocationName(address, 1);
    if (!addresses.isEmpty()) {
      double latitude = addresses.get(0).getLatitude();
      double longitude = addresses.get(0).getLongitude();
      result[0] = latitude;
      result[1] = longitude;
    }
    return result;
  }


  public static void getCoordinatesFromAddress (String address, final OnGeoUtilResultListener
      listener) {
    SmartLocation.with(OmniNotes.getAppContext()).geocoding().direct(address, (name, results) -> {
      if (!results.isEmpty()) {
        listener.onCoordinatesResolved(results.get(0).getLocation(), address);
      }
    });
  }


  public static List<String> autocomplete (String input) {
    if (TextUtils.isEmpty(MAPS_API_KEY)) {
      return Collections.emptyList();
    }
    ArrayList<String> resultList = null;

    HttpURLConnection conn = null;
    InputStreamReader in = null;
    StringBuilder jsonResults = new StringBuilder();
    try {
      URL url = new URL(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON + "?key=" + MAPS_API_KEY + "&input=" +
          URLEncoder.encode(input, "utf8"));
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
      if (conn != null) {
        conn.disconnect();
      }
      SystemHelper.closeCloseable(in);
    }
    return resultList;
  }


  public static boolean areCoordinates (String string) {
    Pattern p = Pattern.compile("^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?),\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|" +
        "([1-9]?\\d))(\\.\\d+)?)$");
    Matcher m = p.matcher(string);
    return m.matches();
  }
}

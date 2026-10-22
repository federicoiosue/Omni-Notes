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
package it.feio.android.omninotes.async;

import android.os.AsyncTask;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.helpers.LogDelegate;
import it.feio.android.omninotes.helpers.location.GeocodeHelper;
import java.io.IOException;
import java.util.function.Consumer;


/**
 * Resolves the address of a location in background, so the UI thread is never blocked by the
 * platform geocoder. The address is delivered as an empty string when it cannot be resolved.
 */
public class ReverseGeocodeTask extends AsyncTask<Void, Void, String> {

  private final Consumer<String> onAddressResolved;
  private final double latitude;
  private final double longitude;


  public ReverseGeocodeTask(Consumer<String> onAddressResolved, double latitude,
      double longitude) {
    this.onAddressResolved = onAddressResolved;
    this.latitude = latitude;
    this.longitude = longitude;
  }


  @Override
  protected String doInBackground(Void... params) {
    try {
      return GeocodeHelper.getAddressFromCoordinates(OmniNotes.getAppContext(), latitude,
          longitude);
    } catch (IOException e) {
      LogDelegate.e("Error resolving the address from coordinates", e);
      return "";
    }
  }


  @Override
  protected void onPostExecute(String address) {
    if (onAddressResolved != null) {
      onAddressResolved.accept(address);
    }
  }

}

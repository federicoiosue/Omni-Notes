package it.feio.android.omninotes.helpers.location;

import it.feio.android.omninotes.models.listeners.OnGeoUtilResultListener;

public interface LocationProvider {

  void instantiate();
  void getLocation(OnGeoUtilResultListener onGeoUtilResultListener) throws SecurityException;

}

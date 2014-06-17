package it.feio.android.omninotes.utils;

import it.feio.android.omninotes.MainActivity;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

public class GeocodeHelper {

	public static String getAddressFromCoordinates(Context mContext, double latitude,
			double longitude) throws IOException {
		String addressString = "";
		Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
		List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
		if (addresses.size() > 0) {
			Address address = addresses.get(0);
			if (address != null) {
				addressString = address.getThoroughfare() + ", " + address.getLocality();
			}
		}
		return addressString;
	}


	public static double[] getCoordinatesFromAddress(Context mContext, String address)
			throws IOException {
		double[] result = new double[2];
		Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
		List<Address> addresses = geocoder.getFromLocationName(address, 1);
		// Address address = addresses.get(0);
		if (addresses.size() > 0) {
			double latitude = addresses.get(0).getLatitude();
			double longitude = addresses.get(0).getLongitude();
			result[0] = latitude;
			result[1] = longitude;
		}
		return result;
	}
}

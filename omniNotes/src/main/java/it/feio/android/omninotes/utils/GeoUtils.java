package it.feio.android.omninotes.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import java.io.IOException;

import it.feio.android.omninotes.models.listeners.OnGeoUtilResultListener;

public class GeoUtils {

    private static GeoUtils instance;
    private Context mContext;

    private GeoUtils() {
    }

    private GeoUtils(Context context) {
        mContext = context;
    }

    public static GeoUtils getInstance(Context context) {
        if (instance == null) {
            instance = new GeoUtils(context);
        }
        return instance;
    }

    public void resolveAddress(double lat, double lon, final OnGeoUtilResultListener listener) {
        class AddressResolverTask extends AsyncTask<Double, Void, String> {

            @Override
            protected String doInBackground(Double... params) {
                String addressString;
                try {
                    addressString = GeocodeHelper.getAddressFromCoordinates(mContext, params[0], params[1]);
                } catch (IOException ex) {
                    addressString = null;
                }
                return addressString;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                listener.onAddressResolved(result);
            }
        }

        AddressResolverTask task = new AddressResolverTask();
        if (Build.VERSION.SDK_INT >= 11) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, lat, lon);
        } else {
            task.execute(lat, lon);
        }
    }


    public void resolveCoordinates(String address, final OnGeoUtilResultListener listener) {
        class CoordinatesResolverTask extends AsyncTask<String, Void, double[]> {

            @Override
            protected double[] doInBackground(String... params) {
                double[] coords;
                try {
                    coords = GeocodeHelper.getCoordinatesFromAddress(mContext, params[0]);
                } catch (IOException ex) {
                    coords = null;
                }
                return coords;
            }

            @Override
            protected void onPostExecute(double[] coords) {
                super.onPostExecute(coords);
                listener.onCoordinatesResolved(coords);
            }
        }

        CoordinatesResolverTask task = new CoordinatesResolverTask();
        if (Build.VERSION.SDK_INT >= 11) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, address);
        } else {
            task.execute(address);
        }
    }
}
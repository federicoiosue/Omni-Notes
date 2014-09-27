package it.feio.android.omninotes.models.listeners;


public interface OnGeoUtilResultListener {
    public void onAddressResolved(String address);
    public void onCoordinatesResolved(double[] coords);
}
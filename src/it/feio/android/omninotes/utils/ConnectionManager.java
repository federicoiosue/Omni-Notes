package it.feio.android.omninotes.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;


public class ConnectionManager {

	/**
	 * Controlla se è disponibile una connessione internet e se rispetta i requisiti delle impostazioni dell'applicazione	 * 
	 * @param ctx
	 * @return
	 */
	public static boolean internetAvailable(Context ctx) {
		boolean result = false;
		ConnectivityManager conMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (conMgr.getActiveNetworkInfo() != null) {
			boolean connected = conMgr.getActiveNetworkInfo().isConnected();
			boolean wifi = conMgr.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;
			boolean allowMobileData = PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(
					"settings_allow_mobile_data", false);
			result = connected && (wifi || allowMobileData);
		}
		return result;
	}
}

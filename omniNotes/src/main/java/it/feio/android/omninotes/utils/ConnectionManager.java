/*
 * Copyright (C) 2015 Federico Iosue (federico.iosue@gmail.com)
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

import android.content.Context;
import android.net.ConnectivityManager;


public class ConnectionManager {

	/**
	 * Controlla se � disponibile una connessione internet e se rispetta i requisiti delle impostazioni dell'applicazione	 * 
	 * @param ctx
	 * @return
	 */
	public static boolean internetAvailable(Context ctx) {
		boolean result = false;
		ConnectivityManager conMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (conMgr.getActiveNetworkInfo() != null) {
			boolean connected = conMgr.getActiveNetworkInfo().isConnected();
			boolean wifi = conMgr.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;
			boolean allowMobileData = ctx.getSharedPreferences(Constants.PREFS_NAME, ctx.MODE_MULTI_PROCESS).getBoolean(
					"settings_allow_mobile_data", false);
			result = connected && (wifi || allowMobileData);
		}
		return result;
	}
}

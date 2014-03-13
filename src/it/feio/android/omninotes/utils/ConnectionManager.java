/*******************************************************************************
 * Copyright 2014 Federico Iosue (federico.iosue@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package it.feio.android.omninotes.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;


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

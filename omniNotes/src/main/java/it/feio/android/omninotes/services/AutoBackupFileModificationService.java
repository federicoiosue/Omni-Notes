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

package it.feio.android.omninotes.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import it.feio.android.omninotes.utils.StorageHelper;


public class AutoBackupFileModificationService extends Service {

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (StorageHelper.checkStorage()) {
			AutoBackupFileObserver.getInstance().startWatching();
			Log.d(getClass().getSimpleName(), "Started monitor service");
		}
		return super.onStartCommand(intent, flags, startId);
	}


	@Override
	public void onDestroy() {
		AutoBackupFileObserver.getInstance().stopWatching();
		Log.d(getClass().getSimpleName(), "Stopped monitor service");
	}


	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}

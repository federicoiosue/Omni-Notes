/*
 * Copyright (C) 2018 Federico Iosue (federico.iosue@gmail.com)
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

package it.feio.android.omninotes.helpers;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import com.tbruyelle.rxpermissions.RxPermissions;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.listeners.OnPermissionRequestedListener;


public class PermissionsHelper {


	public static void requestPermission(Activity activity, String permission, int rationaleDescription, View
			messageView, OnPermissionRequestedListener onPermissionRequestedListener) {

		if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {

			if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
				Snackbar.make(messageView, rationaleDescription, Snackbar.LENGTH_INDEFINITE)
						.setAction(R.string.ok, view -> {
							requestPermissionExecute(activity, permission, onPermissionRequestedListener, messageView);
						})
						.show();
			} else {
				requestPermissionExecute(activity, permission, onPermissionRequestedListener, messageView);
			}
		} else {
			onPermissionRequestedListener.onPermissionGranted();
		}
	}


	private static void requestPermissionExecute(Activity activity, String permission, OnPermissionRequestedListener
			onPermissionRequestedListener, View messageView) {
		RxPermissions.getInstance(activity)
				.request(permission)
				.subscribe(granted -> {
					if (granted) {
						onPermissionRequestedListener.onPermissionGranted();
					} else {
						String msg = activity.getString(R.string.permission_not_granted) + ": " + permission;
						Snackbar.make(messageView, msg, Snackbar.LENGTH_LONG).show();
					}
				});
	}
}

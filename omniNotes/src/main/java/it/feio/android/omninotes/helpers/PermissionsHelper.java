/*
 * Copyright (C) 2013-2025 Federico Iosue (developer@omninotes.app)
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

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE;
import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG;

import android.os.Build.VERSION_CODES;
import android.view.View;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.snackbar.Snackbar;
import com.permissionx.guolindev.PermissionX;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.listeners.OnPermissionRequestedListener;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PermissionsHelper {

  public static void requestPermission(Fragment fragment, String permission,
      int rationaleDescription, View messageView,
      OnPermissionRequestedListener onPermissionRequestedListener) {

    if (skipPermissionRequest(permission)) {
      onPermissionRequestedListener.onPermissionGranted();
      return;
    }

    if (ContextCompat.checkSelfPermission(fragment.getActivity(), permission) != PERMISSION_GRANTED) {
      if (ActivityCompat.shouldShowRequestPermissionRationale(fragment.getActivity(), permission)) {
        Snackbar.make(messageView, rationaleDescription, LENGTH_INDEFINITE)
            .setAction(R.string.ok, view -> requestPermissionExecute(fragment, permission,
                onPermissionRequestedListener, messageView))
            .show();
      } else {
        requestPermissionExecute(fragment, permission, onPermissionRequestedListener, messageView);
      }
    } else {
      if (onPermissionRequestedListener != null) {
        onPermissionRequestedListener.onPermissionGranted();
      }
    }
  }

  private static boolean skipPermissionRequest(String permission) {
    return BuildHelper.isAbove(VERSION_CODES.Q) && permission.equals(WRITE_EXTERNAL_STORAGE);
  }

  private static void requestPermissionExecute(Fragment fragment, String permission,
      OnPermissionRequestedListener onPermissionRequestedListener, View messageView) {
    PermissionX.init(fragment)
        .permissions(permission)
        .request((allGranted, grantedList, deniedList) -> {
          if (allGranted) {
            onPermissionRequestedListener.onPermissionGranted();
          } else {
            var msg = fragment.getString(R.string.permission_not_granted) + ": " + permission;
            Snackbar.make(messageView, msg, LENGTH_LONG).show();
          }
        });
  }

}
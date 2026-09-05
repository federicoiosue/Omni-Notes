/*
 * Copyright (C) 2013-2026 Federico Iosue (omninotes@iosue.it)
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
import android.text.TextUtils;
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
    requestPermissions(fragment, new String[]{permission}, rationaleDescription, messageView,
        onPermissionRequestedListener);
  }

  public static void requestPermissions(Fragment fragment, String[] permissions,
      int rationaleDescription, View messageView,
      OnPermissionRequestedListener onPermissionRequestedListener) {
    requestPermissions(fragment, permissions, rationaleDescription, messageView,
        onPermissionRequestedListener, false);
  }

  /**
   * Requests a set of permissions.
   *
   * @param anyGranted true when the requested permissions are alternatives and a single granted
   *                   permission is enough to proceed (e.g. fine OR coarse location)
   */
  public static void requestPermissions(Fragment fragment, String[] permissions,
      int rationaleDescription, View messageView,
      OnPermissionRequestedListener onPermissionRequestedListener, boolean anyGranted) {

    if (skipPermissionRequest(permissions)) {
      onPermissionRequestedListener.onPermissionGranted();
      return;
    }

    if (!arePermissionsGranted(fragment, permissions, anyGranted)) {
      if (shouldShowRequestPermissionRationale(fragment, permissions)) {
        Snackbar.make(messageView, rationaleDescription, LENGTH_INDEFINITE)
            .setAction(R.string.ok, view -> requestPermissionsExecute(fragment, permissions,
                onPermissionRequestedListener, messageView, anyGranted))
            .show();
      } else {
        requestPermissionsExecute(fragment, permissions, onPermissionRequestedListener, messageView,
            anyGranted);
      }
    } else {
      if (onPermissionRequestedListener != null) {
        onPermissionRequestedListener.onPermissionGranted();
      }
    }
  }

  private static boolean arePermissionsGranted(Fragment fragment, String[] permissions,
      boolean anyGranted) {
    boolean granted = !anyGranted;
    for (String permission : permissions) {
      boolean singleGranted =
          ContextCompat.checkSelfPermission(fragment.requireActivity(), permission)
              == PERMISSION_GRANTED;
      granted = anyGranted ? (granted || singleGranted) : (granted && singleGranted);
    }
    return granted;
  }

  private static boolean shouldShowRequestPermissionRationale(Fragment fragment,
      String[] permissions) {
    for (String permission : permissions) {
      if (ActivityCompat.shouldShowRequestPermissionRationale(fragment.requireActivity(),
          permission)) {
        return true;
      }
    }
    return false;
  }

  private static boolean skipPermissionRequest(String[] permissions) {
    return BuildHelper.isAbove(VERSION_CODES.Q) && permissions.length == 1
        && permissions[0].equals(WRITE_EXTERNAL_STORAGE);
  }

  private static void requestPermissionsExecute(Fragment fragment, String[] permissions,
      OnPermissionRequestedListener onPermissionRequestedListener, View messageView,
      boolean anyGranted) {
    PermissionX.init(fragment)
        .permissions(permissions)
        .request((allGranted, grantedList, deniedList) -> {
          if (allGranted || (anyGranted && !grantedList.isEmpty())) {
            onPermissionRequestedListener.onPermissionGranted();
          } else {
            var msg = fragment.getString(R.string.permission_not_granted) + ": "
                + TextUtils.join(", ", deniedList);
            Snackbar.make(messageView, msg, LENGTH_LONG).show();
          }
        });
  }

}
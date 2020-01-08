/*
 * Copyright (C) 2013-2020 Federico Iosue (federico@iosue.it)
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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import it.feio.android.omninotes.helpers.LogDelegate;


public class Display {

  private Display () {
    // hides public constructor
  }


  public static View getRootView (Activity mActivity) {
    return mActivity.getWindow().getDecorView().findViewById(android.R.id.content);
  }


  @SuppressWarnings("deprecation")
  @SuppressLint("NewApi")
  public static Point getUsableSize (Context mContext) {
    Point displaySize = new Point();
    try {
      WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
      if (manager != null) {
        android.view.Display display = manager.getDefaultDisplay();
        if (display != null) {
          display.getSize(displaySize);
        }
      }
    } catch (Exception e) {
      LogDelegate.e("Error checking display sizes", e);
    }
    return displaySize;
  }


  public static Point getVisibleSize (Activity activity) {
    Point displaySize = new Point();
    Rect r = new Rect();
    activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
    displaySize.x = r.right - r.left;
    displaySize.y = r.bottom - r.top;
    return displaySize;
  }


  public static Point getFullSize (View view) {
    Point displaySize = new Point();
    displaySize.x = view.getRootView().getWidth();
    displaySize.y = view.getRootView().getHeight();
    return displaySize;
  }


  public static int getStatusBarHeight (Context mContext) {
    int result = 0;
    int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
    if (resourceId > 0) {
      result = mContext.getResources().getDimensionPixelSize(resourceId);
    }
    return result;
  }


  public static int getNavigationBarHeightStandard (Context mContext) {
    int resourceId = mContext.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
    if (resourceId > 0) {
      return mContext.getResources().getDimensionPixelSize(resourceId);
    }
    return 0;
  }


  public static int getNavigationBarHeight (View view) {
    return (getFullSize(view).y - getUsableSize(view.getContext()).y);
  }


  @SuppressLint("NewApi")
  public static int getActionbarHeight (Object mObject) {
    int res = 0;
    if (AppCompatActivity.class.isAssignableFrom(mObject.getClass())) {
      res = ((AppCompatActivity) mObject).getSupportActionBar().getHeight();
    } else if (Activity.class.isAssignableFrom(mObject.getClass())) {
      res = ((Activity) mObject).getActionBar().getHeight();
    }
    return res;
  }


  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
  public static Point getScreenDimensions (Context mContext) {
    WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
    android.view.Display display = wm.getDefaultDisplay();
    Point size = new Point();
    DisplayMetrics metrics = new DisplayMetrics();
    display.getRealMetrics(metrics);
    size.x = metrics.widthPixels;
    size.y = metrics.heightPixels;
    return size;
  }


  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
  public static int getNavigationBarHeightKitkat (Context mContext) {
    return getScreenDimensions(mContext).y - getUsableSize(mContext).y;
  }


  public static boolean orientationLandscape (Context context) {
    return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
  }

  public static int getSoftButtonsBarHeight (Activity activity) {
    DisplayMetrics metrics = new DisplayMetrics();
    activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
    int usableHeight = metrics.heightPixels;
    activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
    int realHeight = metrics.heightPixels;
    if (realHeight > usableHeight) {
      return realHeight - usableHeight;
    } else {
      return 0;
    }
  }

}

package it.feio.android.omninotes.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class Display {

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static Point checkDisplayUsableSize(Context mContext) {
		Point displaySize = new Point();
		try {
			WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
			if (manager != null) {
				android.view.Display display = manager.getDefaultDisplay();
				if (display != null) {
					if (android.os.Build.VERSION.SDK_INT < 13) {
						displaySize.set(display.getWidth(), display.getHeight());
					} else {
						display.getSize(displaySize);
					}
				}
			}
		} catch (Exception e) {
			Log.e("checkDisplaySize", "Error checking display sizes", e);
		}
		return displaySize;
	}

	public static Point checkDisplayVisibleSize(View view) {
		Point displaySize = new Point();
		Rect r = new Rect();
		view.getWindowVisibleDisplayFrame(r);
		displaySize.x = r.right - r.left;
		displaySize.y = r.bottom - r.top;
		return displaySize;
	}

	public static Point checkDisplayFullSize(View view) {
		Point displaySize = new Point();
		displaySize.x = view.getRootView().getWidth();
		displaySize.y = view.getRootView().getHeight();
		return displaySize;
	}
}

package it.feio.android.omninotes.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;
import android.view.animation.AlphaAnimation;

public class AlphaManager {

	@SuppressLint("NewApi")
	public static void setAlpha(View v, float alpha) {
		if (v != null) {
			if (Build.VERSION.SDK_INT < 11) {
				final AlphaAnimation animation = new AlphaAnimation(1F, alpha);
				// animation.setDuration(500);
				animation.setFillAfter(true);
				v.startAnimation(animation);
			} else {
				v.setAlpha(alpha);
			}
		}
	}
}

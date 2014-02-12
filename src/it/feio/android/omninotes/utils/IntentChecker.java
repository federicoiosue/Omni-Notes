package it.feio.android.omninotes.utils;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public class IntentChecker {
	/**
	 * Checks intent and features availability
	 * 
	 * @param ctx
	 * @param intent
	 * @param features
	 * @return
	 */
	public static boolean isAvailable(Context ctx, Intent intent, String[] features) {
		boolean res = true;
		final PackageManager mgr = ctx.getPackageManager();
		// Intent resolver
		List<ResolveInfo> list = mgr.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		res = res && list.size() > 0;
		// Features
		if (features != null) {
			for (String feature : features) {
				res = res && mgr.hasSystemFeature(feature);
			}
		}
		return res;
	}
}

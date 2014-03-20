package it.feio.android.omninotes.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

public class AppTourHelper {

	private static final String[] showcasesSuffixes = {"list", "navdrawer", "detail", "list2"};
	private static ArrayList<String> showcases;

	private static SharedPreferences init(Context mContext) {
		showcases = new ArrayList<String>();
		for (String showcaseSuffix : showcasesSuffixes) {
			showcases.add(Constants.PREF_TOUR_PREFIX + showcaseSuffix);
		}
		return mContext.getSharedPreferences(Constants.PREFS_NAME, mContext.MODE_MULTI_PROCESS);
	}

	public static boolean neverDone(Context mContext) {
		SharedPreferences prefs = init(mContext);
		boolean res = true;
		Map<String, ?> prefsMap = prefs.getAll();
		final Iterator<?> it = prefsMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry mapEntry = (Map.Entry) it.next();
			String key = mapEntry.getKey().toString();
			if (key.contains(Constants.PREF_TOUR_PREFIX)) {
				res = false;
				break;
			}
		}
		// for (String showcase : showcases) {
		// if (prefs.contains(showcase)) {
		// res = false;
		// break;
		// }
		// }
		return res;
	}

	public static void skip(Context mContext) {
		SharedPreferences prefs = init(mContext);
		prefs.edit().putBoolean(Constants.PREF_TOUR_PREFIX + "skipped", true).commit();
		for (String showcase : showcases) {
			prefs.edit().putBoolean(showcase, true).commit();
			break;
		}
	}

	public static void reset(Context mContext) {
		SharedPreferences prefs = init(mContext);
		prefs.edit().remove(Constants.PREF_TOUR_PREFIX + "skipped").commit();
		for (String showcase : showcases) {
			if (prefs.contains(showcase)) {
				prefs.edit().remove(showcase).commit();
			}
		}
	}

	public static void complete(Context mContext, String showcase) {
		SharedPreferences prefs = init(mContext);
		prefs.edit().putBoolean(showcase, true).commit();
	}

}

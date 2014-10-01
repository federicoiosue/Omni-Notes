package it.feio.android.omninotes.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class AppTourHelper {

    public static final String PREF_TOUR_COMPLETE = Constants.PREF_TOUR_PREFIX + "skipped";
	private static final String[] SHOWCASES_SUFFIXES = {"list", "navdrawer", "detail", "list2"};

    private static ArrayList<String> showcases;

	private static SharedPreferences init(Context mContext) {
		showcases = getShowcases();
		return mContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS);
	}
	
	
	public static ArrayList<String> getShowcases() {
		ArrayList<String> showcases = new ArrayList<String>();
		for (String showcaseSuffix : SHOWCASES_SUFFIXES) {
			showcases.add(Constants.PREF_TOUR_PREFIX + showcaseSuffix);
		}
		return showcases;
	}
	
	
	public static boolean isStepTurn(Context mContext, String showcaseName) {
		boolean res = true;
		SharedPreferences prefs = init(mContext);
		
		// If user skipped tour or showcase has already been showed returns false
		if (prefs.getBoolean(Constants.PREF_TOUR_PREFIX + "skipped", false)
				|| prefs.getBoolean(showcaseName, false) ) {
			return false;
		}
		
		// Otherwise cycles showcases
		for (String showcase : showcases) {
			if (showcase.equals(showcaseName) && res) {
				return true;
			} else {
				if (res) {
					res = res && prefs.getBoolean(showcase, false);					
				} else {
					return false;
				}
			}
		}
		return res;
	}
	

	public static boolean isPlaying(Context mContext) {
		SharedPreferences prefs = init(mContext);
		boolean isPlaying = false;
		Map<String, ?> prefsMap = prefs.getAll();
		for (String showcase : showcases) {
			if (!prefsMap.containsKey(showcase)) {
				isPlaying = true;
				break;
			}
		}
		return isPlaying;
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
        return res;
    }

	
	public static void complete(Context mContext) {
		SharedPreferences prefs = init(mContext);
		prefs.edit().putBoolean(PREF_TOUR_COMPLETE, true).commit();
		for (String showcase : showcases) {
			prefs.edit().putBoolean(showcase, true).commit();
			break;
		}
	}

	
	public static void reset(Context mContext) {
		SharedPreferences prefs = init(mContext);
        prefs.edit().remove(PREF_TOUR_COMPLETE).commit();
		for (String showcase : showcases) {
			if (prefs.contains(showcase)) {
				prefs.edit().remove(showcase).commit();
			}
		}
	}


    public static void completeStep(Context mContext, String showcase) {
        SharedPreferences prefs = init(mContext);
        prefs.edit().putBoolean(showcase, true).commit();
    }


    public static boolean mustRun(Context mContext) {
        SharedPreferences prefs = init(mContext);
        return !prefs.getBoolean(PREF_TOUR_COMPLETE, false);
    }

}

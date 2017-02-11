package it.feio.android.omninotes.helpers;

import android.app.Activity;
import android.app.Application;
import android.content.res.Resources;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import it.feio.android.omninotes.BuildConfig;
import it.feio.android.omninotes.utils.Constants;
import org.piwik.sdk.Piwik;
import org.piwik.sdk.Tracker;

import java.net.MalformedURLException;


public class AnalyticsHelper {

	private static Tracker tracker;

	private static boolean enabled;

	public enum CATEGORIES {ACTION, SETTING, UPDATE}


	public static void init(Application application, boolean enableAnalytics) {
		enabled = enableAnalytics && !TextUtils.isEmpty(BuildConfig.ANALYTICS_URL);
		if (tracker == null && enabled) {
			try {
				tracker = Piwik.getInstance(application).newTracker(BuildConfig.ANALYTICS_URL, 1);
				tracker.setUserId(Settings.Secure.getString(application.getContentResolver(), Settings.Secure
						.ANDROID_ID));
				tracker.trackAppDownload();
			} catch (MalformedURLException e) {
				Log.e(Constants.TAG, "Malformed url to get analytics tracker", e);
			}
		}
	}


	public static Tracker getTracker() {
		return tracker;
	}


	public static void trackScreenView(String screenName) {
		if (checkInit()) {
			tracker.trackScreenView(screenName);
		}
	}


	public static void trackEvent(CATEGORIES category, String action) {
		if (checkInit()) {
			tracker.trackEvent(category.name(), action);
		}
	}


	public static void trackActionFromResourceId(Activity activity, int resourceId) {
		if (checkInit()) {
			try {
				tracker.trackEvent(CATEGORIES.ACTION.name(), activity.getResources().getResourceEntryName(resourceId));
			} catch (Resources.NotFoundException e) {
				Log.w(Constants.TAG, "No resource name found for request id");
			}
		}
	}


	private static boolean checkInit() {
		if (enabled && tracker == null) {
			throw new NullPointerException("Call AnalyticsHelper.init() before using analytics tracker");
		}
		return enabled;
	}
}

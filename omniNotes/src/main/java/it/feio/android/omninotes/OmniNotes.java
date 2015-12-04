/*
 * Copyright (C) 2015 Federico Iosue (federico.iosue@gmail.com)
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

package it.feio.android.omninotes;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import it.feio.android.omninotes.helpers.AnalyticsHelper;
import it.feio.android.omninotes.utils.Constants;
import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender.Method;
import org.acra.sender.HttpSender.Type;

import java.util.Locale;

@ReportsCrashes(httpMethod = Method.POST, reportType = Type.FORM, formUri = "http://collector.tracepot.com/3f39b042", mode = ReportingInteractionMode.TOAST, forceCloseDialogAfterToast = false, resToastText = R.string.crash_toast)
public class OmniNotes extends Application {

	private static Context mContext;

	private final static String PREF_LANG = "settings_language";
	static SharedPreferences prefs;
	private static RefWatcher refWatcher;

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = getApplicationContext();
		prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);
		refWatcher = LeakCanary.install(this);

		if (isDebugBuild()) {
			StrictMode.enableDefaults();
		}

		initAcra(this);

		// Checks selected locale or default one
		updateLanguage(this, null);

		// Analytics initialization
		AnalyticsHelper.init(this);
	}

	private void initAcra(Application application) {
		ACRA.init(application);
		ACRA.getErrorReporter().putCustomData("TRACEPOT_DEVELOP_MODE", isDebugBuild() ? "1" : "0");
	}


	@NonNull
	public static boolean isDebugBuild() {
		return BuildConfig.BUILD_TYPE.equals("debug");
	}

	@Override
	// Used to restore user selected locale when configuration changes
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		String language = prefs.getString(PREF_LANG, "");
		super.onConfigurationChanged(newConfig);
		updateLanguage(this, language);
	}

	public static Context getAppContext() {
		return OmniNotes.mContext;
	}

	public static RefWatcher getRefWatcher() {
		return OmniNotes.refWatcher;
	}

	/**
	 * Updates default language with forced one
	 */
	public static void updateLanguage(Context ctx, String lang) {
		Configuration cfg = new Configuration();
		String language = prefs.getString(PREF_LANG, "");

		if (TextUtils.isEmpty(language) && lang == null) {
			cfg.locale = Locale.getDefault();
			prefs.edit().putString(PREF_LANG, Locale.getDefault().toString()).commit();

		} else if (lang != null) {
			// Adds language from parameter to configuration and preferences if
			// it is not null
			cfg.locale = new Locale(setLanguageString(lang));
			prefs.edit().putString(PREF_LANG, lang).commit();

		} else if (!TextUtils.isEmpty(language)) {
			// Adds language from preferences to configuration if the string is
			// not empty
			cfg.locale = new Locale(setLanguageString(language));
		}

		ctx.getResources().updateConfiguration(cfg, null);
	}

	public static String setLanguageString(String lang) {

		if (lang.contains("_"))
			return lang.split("_")[0];
		else
			return lang;
	}

}

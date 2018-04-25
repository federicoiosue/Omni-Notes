/*
 * Copyright (C) 2018 Federico Iosue (federico.iosue@gmail.com)
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
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDexApplication;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import it.feio.android.analitica.AnalyticsHelper;
import it.feio.android.analitica.AnalyticsHelperFactory;
import it.feio.android.analitica.MockAnalyticsHelper;
import it.feio.android.analitica.exceptions.AnalyticsInstantiationException;
import it.feio.android.analitica.exceptions.InvalidIdentifierException;
import it.feio.android.omninotes.helpers.LanguageHelper;
import it.feio.android.omninotes.utils.Constants;
import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender.Method;
import org.acra.sender.HttpSender.Type;


@ReportsCrashes(httpMethod = Method.POST, reportType = Type.FORM, formUri = BuildConfig.CRASH_REPORTING_URL, mode =
		ReportingInteractionMode.TOAST, forceCloseDialogAfterToast = false, resToastText = R.string.crash_toast)
public class OmniNotes extends MultiDexApplication {

	private static Context mContext;

	static SharedPreferences prefs;
	private static RefWatcher refWatcher;
	private AnalyticsHelper analyticsHelper;

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = getApplicationContext();
		prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);

		if (isDebugBuild()) {
			StrictMode.enableDefaults();
		}

		initAcra(this);

		initLeakCanary();
	}

	private void initLeakCanary() {
		if (!LeakCanary.isInAnalyzerProcess(this)) {
			refWatcher = LeakCanary.install(this);
		}
	}

	private void initAcra(Application application) {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				ACRA.init(application);
				ACRA.getErrorReporter().putCustomData("TRACEPOT_DEVELOP_MODE", isDebugBuild() ? "1" : "0");
				return null;
			}
		}.execute();
	}

	@NonNull
	public static boolean isDebugBuild() {
		return BuildConfig.BUILD_TYPE.equals("debug");
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		String language = prefs.getString(Constants.PREF_LANG, "");
		LanguageHelper.updateLanguage(this, language);
	}

	public static Context getAppContext() {
		return OmniNotes.mContext;
	}

	public static RefWatcher getRefWatcher() {
		return OmniNotes.refWatcher;
	}

	/**
	 * Statically returns app's default SharedPreferences instance
	 *
	 * @return SharedPreferences object instance
	 */
	public static SharedPreferences getSharedPreferences() {
		return getAppContext().getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);
	}

	public AnalyticsHelper getAnalyticsHelper() {
		if (analyticsHelper == null) {
			boolean enableAnalytics = prefs.getBoolean(Constants.PREF_SEND_ANALYTICS, true);
			try {
				String[] analyticsParams = BuildConfig.ANALYTICS_PARAMS.split(Constants.PROPERTIES_PARAMS_SEPARATOR);
				analyticsHelper = new AnalyticsHelperFactory().getAnalyticsHelper(this, enableAnalytics,
						analyticsParams);
			} catch (AnalyticsInstantiationException | InvalidIdentifierException e) {
				analyticsHelper = new MockAnalyticsHelper();
			}
		}
		return analyticsHelper;
	}
}

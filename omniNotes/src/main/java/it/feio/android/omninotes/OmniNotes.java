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

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.text.TextUtils;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import it.feio.android.omninotes.utils.BitmapCache;
import it.feio.android.omninotes.utils.Constants;
import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender.Method;
import org.acra.sender.HttpSender.Type;

import java.util.Locale;


@ReportsCrashes(httpMethod = Method.PUT, reportType = Type.JSON,
        formUri = "http://feio.cloudant.com/acra-omninotes/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "thelescivessiandesedclik", formUriBasicAuthPassword = "uScXIHpchNKfuCdgbm3nHTjo",
        mode = ReportingInteractionMode.TOAST,
        forceCloseDialogAfterToast = false,
        resToastText = R.string.crash_toast)
public class OmniNotes extends Application {

    private static Context mContext;

    private final static String PREF_LANG = "settings_language";
    static SharedPreferences prefs;
    private static Tracker mTracker;
    private static GoogleAnalytics mGa;
    private static BitmapCache mBitmapCache;


    @Override
    public void onCreate() {
        mContext = getApplicationContext();
        prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);
        // The following line triggers the initialization of ACRA
        ACRA.init(this);
        // Instantiate bitmap cache
        mBitmapCache = new BitmapCache(getApplicationContext(), 0, 0, getExternalCacheDir());
        // Checks selected locale or default one
        updateLanguage(this, null);
        // Google Analytics initialization
        initializeGa();
        super.onCreate();
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


    /**
     * Updates default language with forced one
     */
    public static void updateLanguage(Context ctx, String lang) {
        Configuration cfg = new Configuration();
        String language = prefs.getString(PREF_LANG, "");

        if (TextUtils.isEmpty(language) && lang == null) {
            cfg.locale = Locale.getDefault();
            String tmp = Locale.getDefault().toString().substring(0, 2);
            prefs.edit().putString(PREF_LANG, tmp).commit();

        } else if (lang != null) {
            // Checks country
            if (lang.contains("_")) {
                cfg.locale = new Locale(lang.split("_")[0], lang.split("_")[1]);
            } else {
                cfg.locale = new Locale(lang);
            }
            prefs.edit().putString(PREF_LANG, lang).commit();

        } else if (!TextUtils.isEmpty(language)) {
            // Checks country
            if (language.contains("_")) {
                cfg.locale = new Locale(language.split("_")[0], language.split("_")[1]);
            } else {
                cfg.locale = new Locale(language);
            }
        }

        ctx.getResources().updateConfiguration(cfg, null);
    }


    /*
     * Method to handle basic Google Analytics initialization. This call will not block as all Google Analytics work
     * occurs off the main thread.
     */
    private void initializeGa() {
        mGa = GoogleAnalytics.getInstance(this);
        mTracker = mGa.getTracker("UA-45502770-1");
    }


    /*
     * Returns the Google Analytics tracker.
     */
    public static Tracker getGaTracker() {
        return mTracker;
    }


    /*
     * Returns the Google Analytics instance.
     */
    public static GoogleAnalytics getGaInstance() {
        return mGa;
    }


    /*
     * Returns the Google Analytics instance.
     */
    public static BitmapCache getBitmapCache() {
        return mBitmapCache;
    }


    /**
     * Performs a full app restart
     */
    public static void restartApp(final Context mContext) {
        if (MainActivity.getInstance() != null) {
            MainActivity.getInstance().finish();
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            int mPendingIntentId = 123456;
            PendingIntent mPendingIntent = PendingIntent.getActivity(mContext, mPendingIntentId, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        }
    }

}

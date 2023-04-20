/*
 * Copyright (C) 2013-2023 Federico Iosue (federico@iosue.it)
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

import static it.feio.android.omninotes.utils.Constants.PACKAGE;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_LANG;

import android.content.Context;
import android.content.res.Configuration;
import android.os.StrictMode;
import android.text.TextUtils;
import androidx.multidex.MultiDexApplication;
import com.pixplicity.easyprefs.library.Prefs;
import it.feio.android.omninotes.helpers.LanguageHelper;
import it.feio.android.omninotes.helpers.notifications.NotificationsHelper;
import org.acra.ACRA;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.HttpSenderConfigurationBuilder;
import org.acra.config.ToastConfigurationBuilder;
import org.acra.sender.HttpSender.Method;


public class OmniNotes extends MultiDexApplication {

  private static Context mContext;

  public static boolean isDebugBuild() {
    return BuildConfig.BUILD_TYPE.equals("debug");
  }

  public static Context getAppContext() {
    return OmniNotes.mContext;
  }

  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    initAcra();
  }

  @Override
  public void onCreate() {
    super.onCreate();
    mContext = getApplicationContext();
    initSharedPreferences();
    enableStrictMode();
    new NotificationsHelper(this).initNotificationChannels();
  }

  private void initAcra() {
    if (!TextUtils.isEmpty(BuildConfig.CRASH_REPORTING_URL)) {
      HttpSenderConfigurationBuilder httpBuilder = new HttpSenderConfigurationBuilder()
          .withUri(BuildConfig.CRASH_REPORTING_URL)
          .withBasicAuthLogin(BuildConfig.CRASH_REPORTING_LOGIN)
          .withBasicAuthPassword(BuildConfig.CRASH_REPORTING_PASSWORD)
          .withHttpMethod(Method.POST)
          .withEnabled(true);

      ToastConfigurationBuilder toastBuilder = new ToastConfigurationBuilder()
          .withText(this.getString(R.string.crash_toast))
          .withEnabled(true);

      CoreConfigurationBuilder builder = new CoreConfigurationBuilder()
          .withPluginConfigurations(httpBuilder.build(), toastBuilder.build());

      ACRA.init(this, builder);
      ACRA.getErrorReporter().putCustomData("TRACEPOT_DEVELOP_MODE", isDebugBuild() ? "1" : "0");
    }
  }

  private void initSharedPreferences() {
    new Prefs.Builder()
        .setContext(this)
        .setMode(MODE_PRIVATE)
        .setPrefsName(PACKAGE)
        .setUseDefaultSharedPreference(true)
        .build();
  }

  private void enableStrictMode() {
    if (isDebugBuild()) {
      StrictMode.enableDefaults();
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    String language = Prefs.getString(PREF_LANG, "");
    LanguageHelper.updateLanguage(this, language);
  }

}

/*
 * Copyright (C) 2013-2020 Federico Iosue (federico@iosue.it)
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

package it.feio.android.omninotes.helpers;

import static android.content.Context.MODE_MULTI_PROCESS;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_LANG;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import it.feio.android.omninotes.utils.Constants;
import java.util.Locale;


public class LanguageHelper {

  private LanguageHelper() {
    // hides public constructor
  }

  /**
   * Updates default language with forced one
   */
  @SuppressLint("ApplySharedPref")
  public static Context updateLanguage (Context ctx, String lang) {
    SharedPreferences prefs = ctx.getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);
    String language = prefs.getString(PREF_LANG, "");

    Locale locale = null;
    if (TextUtils.isEmpty(language) && lang == null) {
      locale = Locale.getDefault();
      prefs.edit().putString(PREF_LANG, locale.toString()).commit();
    } else if (lang != null) {
      locale = getLocale(lang);
      prefs.edit().putString(PREF_LANG, lang).commit();
    } else if (!TextUtils.isEmpty(language)) {
      locale = getLocale(language);
    }

    return setLocale(ctx, locale);
  }

  private static Context setLocale (Context context, Locale locale) {
    Configuration configuration = context.getResources().getConfiguration();
    configuration.locale = locale;
    context.getResources().updateConfiguration(configuration, null);
    return context;
  }

  /**
   * Checks country AND region
   */
  private static Locale getLocale (String lang) {
    if (lang.contains("_")) {
      return new Locale(lang.split("_")[0], lang.split("_")[1]);
    } else {
      return new Locale(lang);
    }
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
  @NonNull
  static String getLocalizedString (Context context, String desiredLocale, int resourceId) {
    if (desiredLocale.equals(getCurrentLocaleAsString(context))) {
      return context.getResources().getString(resourceId);
    }
    Configuration conf = context.getResources().getConfiguration();
    conf = new Configuration(conf);
    conf.setLocale(getLocale(desiredLocale));
    Context localizedContext = context.createConfigurationContext(conf);
    return localizedContext.getResources().getString(resourceId);
  }

  public static String getCurrentLocaleAsString (Context context) {
    return getCurrentLocale(context).toString();
  }

  public static Locale getCurrentLocale (Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      return context.getResources().getConfiguration().getLocales().get(0);
    } else {
      return context.getResources().getConfiguration().locale;
    }
  }

}

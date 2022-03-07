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

package it.feio.android.omninotes.helpers.count;

import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.helpers.LanguageHelper;
import it.feio.android.omninotes.helpers.LogDelegate;

public class CountFactory {

  private CountFactory() {
  }

  public static WordCounter getWordCounter() {
    try {
      String locale = LanguageHelper.getCurrentLocaleAsString(OmniNotes.getAppContext());
      return getCounterInstanceByLocale(locale);
    } catch (Exception e) {
      LogDelegate.w("Error retrieving locale or context: " + e.getLocalizedMessage(), e);
      return new DefaultWordCounter();
    }
  }

  static WordCounter getCounterInstanceByLocale(String locale) {
    switch (locale) {
      case "ja_JP":
      case "zh_CN":
      case "zh_TW":
        return new IdeogramsWordCounter();
      default:
        return new DefaultWordCounter();
    }
  }
}

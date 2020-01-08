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

package it.feio.android.omninotes.intro;

import static it.feio.android.omninotes.utils.Constants.PREFS_NAME;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_TOUR_COMPLETE;

import android.content.Context;
import android.os.Bundle;
import com.github.paolorotolo.appintro.AppIntro2;
import it.feio.android.omninotes.OmniNotes;


public class IntroActivity extends AppIntro2 {

  @Override
  public void init (Bundle savedInstanceState) {
    addSlide(new IntroSlide1(), getApplicationContext());
    addSlide(new IntroSlide2(), getApplicationContext());
    addSlide(new IntroSlide3(), getApplicationContext());
    addSlide(new IntroSlide4(), getApplicationContext());
    addSlide(new IntroSlide5(), getApplicationContext());
    addSlide(new IntroSlide6(), getApplicationContext());
  }

  @Override
  public void onDonePressed () {
    OmniNotes.getAppContext().getSharedPreferences(PREFS_NAME, Context.MODE_MULTI_PROCESS).edit()
             .putBoolean(PREF_TOUR_COMPLETE, true).apply();
    finish();
  }

  public static boolean mustRun () {
    return !OmniNotes.isDebugBuild() && !OmniNotes.getAppContext().getSharedPreferences(PREFS_NAME,
        Context.MODE_MULTI_PROCESS).getBoolean(PREF_TOUR_COMPLETE, false);
  }

  @Override
  public void onBackPressed () {
    // Does nothing, you HAVE TO SEE THE INTRO!
  }

}

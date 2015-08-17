package it.feio.android.omninotes.intro;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.github.paolorotolo.appintro.AppIntro2;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.utils.Constants;


public class IntroActivity extends AppIntro2 {

	@Override
	public void init(Bundle savedInstanceState) {
		addSlide(new IntroSlide1(), getApplicationContext());
		addSlide(new IntroSlide2(), getApplicationContext());
		addSlide(new IntroSlide3(), getApplicationContext());
		addSlide(new IntroSlide4(), getApplicationContext());
		addSlide(new IntroSlide5(), getApplicationContext());
		addSlide(new IntroSlide6(), getApplicationContext());
	}


	@Override
	public void onDonePressed() {
		SharedPreferences prefs = OmniNotes.getAppContext().getSharedPreferences(Constants.PREFS_NAME, Context
				.MODE_MULTI_PROCESS);
		prefs.edit().putBoolean(Constants.PREF_TOUR_COMPLETE, true).apply();
		finish();
	}


	public static boolean mustRun(Context mContext) {
		SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS);
		return !prefs.getBoolean(Constants.PREF_TOUR_COMPLETE, false);
	}


	@Override
	public void onBackPressed() {
		// Does nothing, you HAVE TO SEE THE INTRO!
	}
}






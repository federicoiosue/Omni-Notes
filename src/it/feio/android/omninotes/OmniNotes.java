package it.feio.android.omninotes;

import it.feio.android.omninotes.utils.ACRAPostSender;
import java.util.HashMap;
import java.util.Locale;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

@ReportsCrashes(formKey = "", 
//				mailTo = Constants.DEV_EMAIL, 
				mode = ReportingInteractionMode.TOAST, 
				resToastText = R.string.crash_dialog_text
//				mode = ReportingInteractionMode.DIALOG,
//				resDialogIcon = R.drawable.ic_launcher,
//				resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
//				resDialogTitle = R.string.crash_dialog_title, 
//				resDialogText = R.string.crash_dialog_text
				)
public class OmniNotes extends Application {
	
	
	SharedPreferences prefs;
	
	@Override
	public void onCreate() {
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
				
		// The following line triggers the initialization of ACRA
		ACRA.init(this);
		HashMap<String, String> ACRAData = new HashMap<String, String>();
		ACRAData.put("my_app_info", "custom data");
		ACRA.getErrorReporter().setReportSender(new ACRAPostSender(ACRAData));

		// Checks selected locale or default one
//		checkLocale(getResources().getConfiguration());

		super.onCreate();
	}
	
	
//	@Override
//	// Used to restore user selected locale when configuration changes
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//		checkLocale(newConfig);
//    }
//
//	/**
//	 * Set custom locale on application start
//	 */
//	protected void checkLocale(Configuration config) {
//		Locale targetLocale = new Locale(prefs.getString("settings_language", Locale.getDefault().getCountry()));
//		if (targetLocale.getLanguage() != config.locale.getLanguage()) {
//			config.locale = targetLocale;
//	
//	        Locale.setDefault(config.locale);
//	        getBaseContext().getResources().updateConfiguration(config, getResources().getDisplayMetrics());
//		}
//	}
}
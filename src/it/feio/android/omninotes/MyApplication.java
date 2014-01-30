package it.feio.android.omninotes;

import it.feio.android.omninotes.utils.ACRAPostSender;
import java.util.HashMap;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

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
public class MyApplication extends Application {
	@Override
	public void onCreate() {
		
		// The following line triggers the initialization of ACRA
		ACRA.init(this);
		HashMap<String, String> ACRAData = new HashMap<String, String>();
		ACRAData.put("my_app_info", "custom data");
		ACRA.getErrorReporter().setReportSender(new ACRAPostSender(ACRAData));

		super.onCreate();
	}
}
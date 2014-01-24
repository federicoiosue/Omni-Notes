package it.feio.android.omninotes;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formKey = "", formUri = "http://www.yourselectedbackend.com/reportpath")
public class MyApplication extends Application {
	@Override
	public void onCreate() {
		// The following line triggers the initialization of ACRA
		super.onCreate();
		ACRA.init(this);
	}
}
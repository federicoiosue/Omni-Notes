package it.feio.android.omninotes;

import it.feio.android.omninotes.utils.Constants;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;

public class SettingsActivity extends PreferenceActivity {

	public final static String KEEP_USER_DATA = "settings_keep_user_data";
	public final static String ALLOW_GEOLOCATION = "settings_allow_geolocation";
	public final static String ALLOW_MOBILE_DATA = "settings_allow_mobile_data";
	final Context context = this;
	final Activity activity = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		// Evento di pressione sul pulsante di reset delle impostazioni
//		Preference resetData = findPreference("reset_all_data");
//		resetData.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//
//			@Override
//			public boolean onPreferenceClick(Preference arg0) {
//				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
//
//				// set dialog message
//				alertDialogBuilder.setMessage("Resettare?").setCancelable(false)
//						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//
//							public void onClick(DialogInterface dialog, int id) {
//								SharedPreferences prefs = PreferenceManager
//										.getDefaultSharedPreferences(context);
//								prefs.edit().clear().commit();
//								Log.i(Constants.TAG, "Impostazioni e dati resettati");
//							}
//						}).setNegativeButton("No", new DialogInterface.OnClickListener() {
//
//							public void onClick(DialogInterface dialog, int id) {
//								dialog.cancel();
//							}
//						});
//
//				// create alert dialog
//				AlertDialog alertDialog = alertDialogBuilder.create();
//
//				// show it
//				alertDialog.show();
//				return false;
//			}
//
//		});


		// Evento di pressione sul pulsante di About
		Preference about = findPreference("about");
		about.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				Log.i(Constants.TAG, "About clicked");	
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(getResources().getString(R.string.dev_googleplus)));
				startActivity(intent);
				return false;
			}
		});
	}
	
}
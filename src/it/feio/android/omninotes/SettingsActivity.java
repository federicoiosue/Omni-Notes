package it.feio.android.omninotes;

import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.DbHelper;
import it.feio.android.omninotes.utils.ImportExportExcel;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.widget.Toast;

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


		// Export notes
		Preference export = findPreference("settings_export_data");
		export.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				try {
					ImportExportExcel importExportExcel = new ImportExportExcel(context);
					if (importExportExcel.exportDataToCSV())
						Toast.makeText(context, getString(R.string.export_success) + " " + Constants.EXPORT_FILE_PATH, Toast.LENGTH_LONG).show();
					else
						Toast.makeText(context, getString(R.string.export_fail), Toast.LENGTH_LONG).show();
				} catch (Exception e) {
					Toast.makeText(context, getString(R.string.export_fail), Toast.LENGTH_LONG).show();				
				}
				return false;
			}					
		});

		// Import notes
		Preference importData = findPreference("settings_import_data");
		importData.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

				// set dialog message
				alertDialogBuilder.setMessage(getString(R.string.import_warning))
						.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int id) {
								try {
									ImportExportExcel importExportExcel = new ImportExportExcel(context);
									if (importExportExcel.importDataFromCSV())
										Toast.makeText(context, getString(R.string.import_success), Toast.LENGTH_LONG).show();
									else
										Toast.makeText(context, getString(R.string.import_fail), Toast.LENGTH_LONG).show();
								} catch (Exception e) {
									Toast.makeText(context, getString(R.string.import_fail), Toast.LENGTH_LONG).show();				
								}
							}
						}).setNegativeButton("No", new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();

				// show it
				alertDialog.show();
				return false;
			}					
		});
		

		// Evento di pressione sul pulsante di reset delle impostazioni
		Preference resetData = findPreference("reset_all_data");
		resetData.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

				// set dialog message
				alertDialogBuilder.setMessage(getString(R.string.reset_all_data_confirmation))
						.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int id) {
								PreferenceManager.getDefaultSharedPreferences(context).edit().clear()
										.commit();
								DbHelper db = new DbHelper(context);
								db.clear();
								Log.i(Constants.TAG, "Settings back to default");
							}
						}).setNegativeButton("No", new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();

				// show it
				alertDialog.show();
				return false;
			}

		});
		

		// Popup About
		Preference about = findPreference("settings_about");
		about.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				Intent aboutIntent = new Intent(context, AboutActivity.class);
		        startActivity(aboutIntent);
				return false;
			}					
		});

	}
	


//	@Override
//	public void onBackPressed() {	
//		NavUtils.navigateUpFromSameTask(this);		
//	}
}
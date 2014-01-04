/*******************************************************************************
 * Copyright 2013 Federico Iosue (federico.iosue@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package it.feio.android.omninotes;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.async.DataBackupIntentService;
import it.feio.android.omninotes.utils.StorageManager;
import it.feio.android.omninotes.R;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsActivity extends PreferenceActivity {

	public final static String KEEP_USER_DATA = "settings_keep_user_data";
	public final static String ALLOW_GEOLOCATION = "settings_allow_geolocation";
	public final static String ALLOW_MOBILE_DATA = "settings_allow_mobile_data";
	final Activity activity = this;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);


		// Export notes
		Preference export = findPreference("settings_export_data");
		export.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
				
				// Inflate layout
				LayoutInflater inflater = activity.getLayoutInflater();
				View v = inflater.inflate(R.layout.export_dialog_layout, null);
				alertDialogBuilder.setView(v);
				
				// Finds actually saved backups names
				final List<String> backups = Arrays.asList(StorageManager.getExternalStoragePublicDir().list());

				// Sets default export file name
				SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_EXPORT);
				String fileName = sdf.format(Calendar.getInstance().getTime());
				final EditText fileNameEditText = (EditText) v.findViewById(R.id.export_file_name);
				final TextView backupExistingTextView = (TextView) v.findViewById(R.id.backup_existing);
				fileNameEditText.setText(fileName);
				
				fileNameEditText.addTextChangedListener(new TextWatcher() {					
					@Override
					public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}					
					@Override
					public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}					
					@Override
					public void afterTextChanged(Editable arg0) {
						if (backups.contains(arg0.toString())) {
							backupExistingTextView.setText(R.string.backup_existing);
						} else {
							backupExistingTextView.setText("");
						}
					}
				});

				// Creates dialog to choose backup name
				alertDialogBuilder
						.setTitle(R.string.data_export_message)
						.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int id) {
								// An IntentService will be launched to accomplish the export task
								Intent service = new Intent(activity, DataBackupIntentService.class);
								service.setAction(Constants.ACTION_DATA_EXPORT);
								service.putExtra(Constants.INTENT_BACKUP_NAME, fileNameEditText.getText().toString());
								activity.startService(service);
							}
						}).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

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

		// Import notes
		Preference importData = findPreference("settings_import_data");
		importData.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

				// set dialog message
//				alertDialogBuilder.setMessage(getString(R.string.import_warning)).setCancelable(false)
//						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//
//							public void onClick(DialogInterface dialog, int id) {
//								try {
//									ImportExportExcel importExportExcel = new ImportExportExcel(activity);
//									if (importExportExcel.importDataFromCSV(StorageManager.getStorageDir()))
//										Toast.makeText(activity, getString(R.string.import_success),
//												Toast.LENGTH_LONG).show();
//									else
//										Toast.makeText(activity, getString(R.string.import_fail),
//												Toast.LENGTH_LONG).show();
//								} catch (Exception e) {
//									Toast.makeText(activity, getString(R.string.import_fail),
//											Toast.LENGTH_LONG).show();
//								}
//							}
//						}).setNegativeButton("No", new DialogInterface.OnClickListener() {
//
//							public void onClick(DialogInterface dialog, int id) {
//								dialog.cancel();
//							}
//						});
				
				final CharSequence[] backups = StorageManager.getExternalStoragePublicDir().list();
				alertDialogBuilder.setTitle(R.string.data_import_message)
									.setItems(backups, new OnClickListener() {										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											// An IntentService will be launched to accomplish the import task
											Intent service = new Intent(activity, DataBackupIntentService.class);
											service.setAction(Constants.ACTION_DATA_IMPORT);
											service.putExtra(Constants.INTENT_BACKUP_NAME, backups[which]);
											activity.startService(service);
										}
									});

				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();

				// show it
				alertDialog.show();
				return false;
			}
		});


		// Changelog 
		Preference changelog = findPreference("settings_changelog");
		changelog.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				Intent changelogIntent = new Intent(activity, ChangelogActivity.class);
				startActivity(changelogIntent);
//				ChangelogFragment cf = new ChangelogFragment();
//				cf.show(getFragmentManager(), Constants.TAG);
				return false;
			}
		});


		// Evento di pressione sul pulsante di reset delle impostazioni
		Preference resetData = findPreference("reset_all_data");
		resetData.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
//				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
//
//				// set dialog message
//				alertDialogBuilder.setMessage(getString(R.string.reset_all_data_confirmation))
//						.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//
//							public void onClick(DialogInterface dialog, int id) {
//								PreferenceManager.getDefaultSharedPreferences(activity).edit().clear()
//										.commit();
//								DbHelper db = new DbHelper(activity);
//								db.clear();
//								Log.i(Constants.TAG, "Settings back to default");
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
				

				String packageName = getApplicationContext().getPackageName();

				try {
				    //Open the specific App Info page:
				    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				    intent.setData(Uri.parse("package:" + packageName));
				    startActivity(intent);

				} catch ( ActivityNotFoundException e ) {
				    //e.printStackTrace();

				    //Open the generic Apps page:
				    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
				    startActivity(intent);

				}

				return false;
				
			}

		});


		// Popup About
		Preference about = findPreference("settings_about");
		about.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				Intent aboutIntent = new Intent(activity, AboutActivity.class);
				startActivity(aboutIntent);
				return false;
			}
		});
		
		// Languages 
//		Preference lang = findPreference("settings_language");
//		lang.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
//			
//			@Override
//			public boolean onPreferenceChange(Preference preference, Object value) {
//				Locale locale = new Locale(value.toString());
//				Configuration config = new Configuration();
//				config.locale = locale;
//				getBaseContext().getResources().updateConfiguration(config,
//						getBaseContext().getResources().getDisplayMetrics());
//				PreferenceManager.getDefaultSharedPreferences(activity).edit().putString("settings_language", value.toString()).commit();
//				finish();
//				startActivity(new Intent(getApplicationContext(), ListActivity.class));
//				return false;
//			}
//		});

	}
}

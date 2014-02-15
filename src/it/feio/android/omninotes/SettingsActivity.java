/*******************************************************************************
 * Copyright 2014 Federico Iosue (federico.iosue@gmail.com)
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

import it.feio.android.omninotes.async.DataBackupIntentService;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.StorageManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
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
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);


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
				
				final CharSequence[] backups = StorageManager.getExternalStoragePublicDir().list();
				alertDialogBuilder.setTitle(R.string.data_import_message)
									.setItems(backups, null);

				// create alert dialog
				final AlertDialog alertDialog = alertDialogBuilder.create();
				
				// OnShow is overridden to allow long-click on item so user can remove them
				alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

				    @Override
				    public void onShow(final DialogInterface dialog) {

				    	ListView lv = alertDialog.getListView();
				    	lv.setOnItemClickListener(new OnItemClickListener() {
				    		
				    		@Override
							public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
								final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
										activity);
								
								// Retrieves backup size
								File backupDir = StorageManager.getBackupDir(backups[position].toString());
								long size = StorageManager.getSize(backupDir) / 1024;
								String sizeString = size > 1024 ? size/1024 + "Mb" : size + "Kb";
								
								// Set dialog message and button
								alertDialogBuilder.setMessage(
										getString(R.string.confirm_restoring_backup) + " " + backups[position] + " (" + sizeString + ")")
										.setPositiveButton(R.string.confirm, new OnClickListener() {
											
											@Override
											public void onClick(DialogInterface dialogInner, int which) {
												dialogInner.dismiss();
												dialog.dismiss();
												// An IntentService will be launched to accomplish the import task
												Intent service = new Intent(activity, DataBackupIntentService.class);
												service.setAction(Constants.ACTION_DATA_IMPORT);
												service.putExtra(Constants.INTENT_BACKUP_NAME, backups[position]);
												activity.startService(service);
											}
										})
										.setNegativeButton(R.string.cancel, new OnClickListener() {
											
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.cancel();											
											}
										});
								
								alertDialogBuilder.create().show();
							}
				        });
				    	
				    	// Creation of backup removal dialog
				    	lv.setOnItemLongClickListener(new OnItemLongClickListener() {

							@Override
							public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
								final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
										activity);
								
								// Retrieves backup size
								File backupDir = StorageManager.getBackupDir(backups[position].toString());
								long size = StorageManager.getSize(backupDir) / 1024;
								String sizeString = size > 1024 ? size/1024 + "Mb" : size + "Kb";
								
								// Set dialog message and button
								alertDialogBuilder.setMessage(
										getString(R.string.confirm_removing_backup) + " " + backups[position] + " (" + sizeString + ")")
										.setPositiveButton(R.string.confirm, new OnClickListener() {
											
											@Override
											public void onClick(DialogInterface dialogInner, int which) {
												dialogInner.dismiss();
												dialog.dismiss();	
												// An IntentService will be launched to accomplish the import task
												Intent service = new Intent(activity, DataBackupIntentService.class);
												service.setAction(Constants.ACTION_DATA_DELETE);
												service.putExtra(Constants.INTENT_BACKUP_NAME, backups[position]);
												activity.startService(service);
											}
										})
										.setNegativeButton(R.string.cancel, new OnClickListener() {
											
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.cancel();											
											}
										});
								
								alertDialogBuilder.create().show();
								return true;
							}
						});
				    }
				});

				// show it
				alertDialog.show();
				return false;
			}
		});
		
		
		// Maximum video attachment size
		final EditTextPreference maxVideoSize = (EditTextPreference) findPreference("settings_max_video_size");
		String maxVideoSizeValue = prefs.getString("settings_max_video_size", "");
		maxVideoSize.setSummary(getString(R.string.settings_max_video_size_summary) + ": " + String.valueOf(maxVideoSizeValue));
		maxVideoSize.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				maxVideoSize.setSummary(getString(R.string.settings_max_video_size_summary) + ": " + String.valueOf(newValue));
				prefs.edit().putString("settings_notification_snooze_delay", newValue.toString()).commit();
				return false;
			}
		});
		

		
		// Set notes' protection password
		Preference password = findPreference("settings_password");
		password.setOnPreferenceClickListener(new OnPreferenceClickListener() {			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent passwordIntent = new Intent(activity, PasswordActivity.class);
				startActivity(passwordIntent);
				return false;
			}
		});
		
		
		
		// Instructions
		Preference instructions = findPreference("settings_tour_show_again");
		instructions.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
				// set dialog message
				alertDialogBuilder
						.setMessage(getString(R.string.settings_tour_show_again_summary))
						.setCancelable(false).setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// All  the preferences will be cycled until 
								Map<String, ?> prefsMap = prefs.getAll();
								Iterator<?> it = prefsMap.entrySet().iterator();
								while (it.hasNext()) {
									Map.Entry mapEntry = (Map.Entry) it.next();
									String key = mapEntry.getKey().toString();
									if (key.contains(Constants.PREF_INSTRUCTIONS_PREFIX)) {
										prefs.edit().putBoolean(key, false).commit();
										restartApp();
									}
								}
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

		
		
		// Notification snooze delay
		final EditTextPreference snoozeDelay = (EditTextPreference) findPreference("settings_notification_snooze_delay");
		String snoozeDelayValue = prefs.getString("settings_notification_snooze_delay", "10");
		snoozeDelay.setSummary(String.valueOf(snoozeDelayValue)	+ " " + getString(R.string.minutes));
		snoozeDelay.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				snoozeDelay.setSummary(String.valueOf(newValue)	+ " " + getString(R.string.minutes));
				prefs.edit().putString("settings_notification_snooze_delay", newValue.toString()).commit();
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
		// Retrieval of installed app version to write it as summary
		PackageInfo pInfo;
		String versionString = "";
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			versionString = pInfo.versionName;
		} catch (NameNotFoundException e) {
			Log.e(Constants.TAG, "Error retrieving version", e);
		}
		changelog.setSummary(versionString);

		
		// Settings reset
		Preference resetData = findPreference("reset_all_data");
		resetData.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
				
				// Inflate layout
//				LayoutInflater inflater = activity.getLayoutInflater();
//				View v = inflater.inflate(R.layout.reset_data_dialog_layout, null);
//				alertDialogBuilder.setView(v);

				// set dialog message
				alertDialogBuilder
						.setMessage(getString(R.string.reset_all_data_confirmation))
						.setCancelable(false).setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int id) {
								prefs.edit().clear().commit();
								File db = activity.getDatabasePath(Constants.DATABASE_NAME);
								StorageManager.delete(activity, db.getAbsolutePath());
								File attachmentsDir = StorageManager.getAttachmentDir(activity);
								StorageManager.delete(activity, attachmentsDir.getAbsolutePath());
//								Intent intent = new Intent(activity, ListActivity.class);
//								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
//								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//							    startActivity(intent);
								restartApp();
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


		
		// About
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
		ListPreference lang = (ListPreference)findPreference("settings_language");	
		String languageName = getResources().getConfiguration().locale.getDisplayName();
		lang.setSummary( languageName.substring(0, 1).toUpperCase(getResources().getConfiguration().locale)
						+ languageName.substring(1, languageName.length()) );
		lang.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object value) {
				Locale locale = new Locale(value.toString());
				Configuration config = getResources().getConfiguration();
				
				if (!config.locale.getCountry().equals(locale)) {
					OmniNotes.updateLanguage(getApplicationContext(), value.toString());
					
//					Intent i = getBaseContext()
//							.getPackageManager()
//							.getLaunchIntentForPackage(getBaseContext()
//							.getPackageName());							                               
//					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//					startActivity(i);
					restartApp();
				}
				return false;
			}
		});

	}
		
	
	
	private void restartApp() {
//		Intent intent = getBaseContext()
//				.getPackageManager()
//				.getLaunchIntentForPackage(getBaseContext()
//				.getPackageName());	
		Intent intent = new Intent(this, ListActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    startActivity(intent);
	}
	
	
}

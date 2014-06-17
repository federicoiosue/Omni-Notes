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
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.ImageAndTextItem;
import it.feio.android.omninotes.models.ONStyle;
import it.feio.android.omninotes.models.PasswordValidator;
import it.feio.android.omninotes.models.adapters.ImageAndTextAdapter;
import it.feio.android.omninotes.utils.AppTourHelper;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.FileHelper;
import it.feio.android.omninotes.utils.StorageManager;
import it.feio.android.springpadimporter.Importer;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import de.keyboardsurfer.android.widget.crouton.Crouton;

public class SettingsActivity extends PreferenceActivity {

	final Activity activity = this;
	private SharedPreferences prefs;

	AboutOrStatsThread mAboutOrStatsThread;
	private int aboutClickCounter = 0;
	private final int SPRINGPAD_IMPORT = 0;
		
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);		
		prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();

		// Export notes
		Preference export = findPreference("settings_export_data");
		export.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
				
				// Inflate layout
				LayoutInflater inflater = activity.getLayoutInflater();
				View v = inflater.inflate(R.layout.dialog_backup_layout, null);
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
								service.setAction(DataBackupIntentService.ACTION_DATA_EXPORT);
								service.putExtra(DataBackupIntentService.INTENT_BACKUP_NAME, fileNameEditText.getText().toString());
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
								
								// Check preference presence
								String prefName = StorageManager.getSharedPreferencesFile(activity).getName();
								boolean hasPreferences = (new File(backupDir, prefName)).exists();
								
								String message = getString(R.string.confirm_restoring_backup) + " " 
												+ backups[position] 
												+ " (" + sizeString
												+ (hasPreferences ? " " + getString(R.string.settings_included) : "")
												+ ")";
								
								// Set dialog message and button
								alertDialogBuilder.setMessage(message)
										.setPositiveButton(R.string.confirm, new OnClickListener() {
											
											@Override
											public void onClick(DialogInterface dialogInner, int which) {
												dialogInner.dismiss();
												dialog.dismiss();
												// An IntentService will be launched to accomplish the import task
												Intent service = new Intent(activity, DataBackupIntentService.class);
												service.setAction(DataBackupIntentService.ACTION_DATA_IMPORT);
												service.putExtra(DataBackupIntentService.INTENT_BACKUP_NAME, backups[position]);
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
												service.setAction(DataBackupIntentService.ACTION_DATA_DELETE);
												service.putExtra(DataBackupIntentService.INTENT_BACKUP_NAME, backups[position]);
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
		
		
		
		
		
		
		
		
		// Import notes from Springpad export zip file
		Preference importFromSpringpad = findPreference("settings_import_from_springpad");
		importFromSpringpad.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
//				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
//
//				final CharSequence[] backups = StorageManager.getExternalStoragePublicDir().list();
//				alertDialogBuilder.setTitle(R.string.data_import_message).setItems(backups, null);
//
//				// create alert dialog
//				final AlertDialog alertDialog = alertDialogBuilder.create();
//
//				// OnShow is overridden to allow long-click on item so user can remove them
//				alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//
//					@Override
//					public void onShow(final DialogInterface dialog) {
//
//						ListView lv = alertDialog.getListView();
//						lv.setOnItemClickListener(new OnItemClickListener() {
//
//							@Override
//							public void onItemClick(AdapterView<?> parent, View view,
//									final int position, long id) {
//								final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
//										activity);
//
//								// Retrieves backup size
//								File backupDir = StorageManager.getBackupDir(backups[position]
//										.toString());
//								long size = StorageManager.getSize(backupDir) / 1024;
//								String sizeString = size > 1024 ? size / 1024 + "Mb" : size + "Kb";
//
//								// Check preference presence
//								String prefName = StorageManager.getSharedPreferencesFile(activity)
//										.getName();
//								boolean hasPreferences = (new File(backupDir, prefName)).exists();
//
//								String message = getString(R.string.confirm_restoring_backup)
//										+ " "
//										+ backups[position]
//										+ " ("
//										+ sizeString
//										+ (hasPreferences ? " "
//												+ getString(R.string.settings_included) : "") + ")";
//
//								// Set dialog message and button
//								alertDialogBuilder
//										.setMessage(message)
//										.setPositiveButton(R.string.confirm, new OnClickListener() {
//
//											@Override
//											public void onClick(DialogInterface dialogInner,
//													int which) {
//												dialogInner.dismiss();
//												dialog.dismiss();
//												// An IntentService will be launched to accomplish the import task
//												Intent service = new Intent(activity,
//														DataBackupIntentService.class);
//												service.setAction(Constants.ACTION_DATA_IMPORT);
//												service.putExtra(Constants.INTENT_BACKUP_NAME,
//														backups[position]);
//												activity.startService(service);
//											}
//										})
//										.setNegativeButton(R.string.cancel, new OnClickListener() {
//
//											@Override
//											public void onClick(DialogInterface dialog, int which) {
//												dialog.cancel();
//											}
//										});
//
//								alertDialogBuilder.create().show();
//							}
//						});
//
//						// Creation of backup removal dialog
//						lv.setOnItemLongClickListener(new OnItemLongClickListener() {
//
//							@Override
//							public boolean onItemLongClick(AdapterView<?> parent, View view,
//									final int position, long id) {
//								final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
//										activity);
//
//								// Retrieves backup size
//								File backupDir = StorageManager.getBackupDir(backups[position]
//										.toString());
//								long size = StorageManager.getSize(backupDir) / 1024;
//								String sizeString = size > 1024 ? size / 1024 + "Mb" : size + "Kb";
//
//								// Set dialog message and button
//								alertDialogBuilder
//										.setMessage(
//												getString(R.string.confirm_removing_backup) + " "
//														+ backups[position] + " (" + sizeString
//														+ ")")
//										.setPositiveButton(R.string.confirm, new OnClickListener() {
//
//											@Override
//											public void onClick(DialogInterface dialogInner,
//													int which) {
//												dialogInner.dismiss();
//												dialog.dismiss();
//												// An IntentService will be launched to accomplish the import task
//												Intent service = new Intent(activity,
//														DataBackupIntentService.class);
//												service.setAction(Constants.ACTION_DATA_DELETE);
//												service.putExtra(Constants.INTENT_BACKUP_NAME,
//														backups[position]);
//												activity.startService(service);
//											}
//										})
//										.setNegativeButton(R.string.cancel, new OnClickListener() {
//
//											@Override
//											public void onClick(DialogInterface dialog, int which) {
//												dialog.cancel();
//											}
//										});
//
//								alertDialogBuilder.create().show();
//								return true;
//							}
//						});
//					}
//				});
//
//				// show it
//				alertDialog.show();
				
				
				
				Intent intent;
				intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("application/zip");
				startActivityForResult(intent, SPRINGPAD_IMPORT );
				
				
				
				
				
				
				return false;
			}
		});
		
		
		
		
		
		
		
		
		
		// Swiping action
		final CheckBoxPreference swipeToTrash = (CheckBoxPreference) findPreference("settings_swipe_to_trash");
		if (prefs.getBoolean("settings_swipe_to_trash", false)) {
			swipeToTrash.setChecked(true);
			swipeToTrash.setSummary(getResources().getString(R.string.settings_swipe_to_trash_summary_2));
		} else {
			swipeToTrash.setSummary(getResources().getString(R.string.settings_swipe_to_trash_summary_1));
			swipeToTrash.setChecked(false);
		}
		swipeToTrash.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {			
			@Override
			public boolean onPreferenceChange(Preference preference, final Object newValue) {
				if ((Boolean) newValue) {
					swipeToTrash.setSummary(getResources().getString(R.string.settings_swipe_to_trash_summary_2));
				} else {
					swipeToTrash.setSummary(getResources().getString(R.string.settings_swipe_to_trash_summary_1));
				}
				swipeToTrash.setChecked((Boolean) newValue);
				return false;
			}
		});
		
		
		
		// Maximum video attachment size
		final EditTextPreference maxVideoSize = (EditTextPreference) findPreference("settings_max_video_size");
		String maxVideoSizeValue = prefs.getString("settings_max_video_size", getString(R.string.not_set));
		maxVideoSize.setSummary(getString(R.string.settings_max_video_size_summary) + ": " + String.valueOf(maxVideoSizeValue));
		maxVideoSize.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				maxVideoSize.setSummary(getString(R.string.settings_max_video_size_summary) + ": " + String.valueOf(newValue));
				prefs.edit().putString("settings_max_video_size", newValue.toString()).commit();
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
		
		
		// Use password to grant application access
		final CheckBoxPreference passwordAccess = (CheckBoxPreference) findPreference("settings_password_access");
		if (prefs.getString(Constants.PREF_PASSWORD, null) == null) {
			passwordAccess.setEnabled(false);
			passwordAccess.setChecked(false);
		} else {
			passwordAccess.setEnabled(true);
		}
		passwordAccess.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {			
			@Override
			public boolean onPreferenceChange(Preference preference, final Object newValue) {
				BaseActivity.requestPassword(activity, new PasswordValidator() {					
					@Override
					public void onPasswordValidated(boolean result) {
						if (result) {			
							passwordAccess.setChecked((Boolean) newValue);
						}
					}
				});
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
					OmniNotes.restartApp(getApplicationContext());
				}
				return false;
			}
		});
		

		
		// Text size
		final ListPreference textSize = (ListPreference) findPreference("settings_text_size");
		int textSizeIndex = textSize.findIndexOfValue(prefs.getString("settings_text_size", "default"));
		String textSizeString = getResources().getStringArray(R.array.text_size)[textSizeIndex];		
		textSize.setSummary(textSizeString);		
		textSize.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				int textSizeIndex = textSize.findIndexOfValue(newValue.toString());
				String checklistString = getResources().getStringArray(R.array.text_size)[textSizeIndex];	
				textSize.setSummary(checklistString);		
				prefs.edit().putString("settings_text_size", newValue.toString()).commit();
				textSize.setValueIndex(textSizeIndex);
				return false;
			}
		});

		
		
		// Application's colors
		final ListPreference colorsApp = (ListPreference) findPreference("settings_colors_app");
		int colorsAppIndex = colorsApp.findIndexOfValue(prefs.getString("settings_colors_app", Constants.PREF_COLORS_APP_DEFAULT));
		String colorsAppString = getResources().getStringArray(R.array.colors_app)[colorsAppIndex];		
		colorsApp.setSummary(colorsAppString);		
		colorsApp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				int colorsAppIndex = colorsApp.findIndexOfValue(newValue.toString());
				String colorsAppString = getResources().getStringArray(R.array.colors_app)[colorsAppIndex];	
				colorsApp.setSummary(colorsAppString);		
				prefs.edit().putString("settings_colors_app", newValue.toString()).commit();
				colorsApp.setValueIndex(colorsAppIndex);
				return false;
			}
		});

		
		
		// Checklists
		final ListPreference checklist = (ListPreference) findPreference("settings_checked_items_behavior");
		int checklistIndex = checklist.findIndexOfValue(prefs.getString("settings_checked_items_behavior", "0"));
		String checklistString = getResources().getStringArray(R.array.checked_items_behavior)[checklistIndex];		
		checklist.setSummary(checklistString);		
		checklist.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				int checklistIndex = checklist.findIndexOfValue(newValue.toString());
				String checklistString = getResources().getStringArray(R.array.checked_items_behavior)[checklistIndex];	
				checklist.setSummary(checklistString);		
				prefs.edit().putString("settings_checked_items_behavior", newValue.toString()).commit();
				checklist.setValueIndex(checklistIndex);
				return false;
			}
		});

		
		
		// Widget's colors
		final ListPreference colorsWidget = (ListPreference) findPreference("settings_colors_widget");
		int colorsWidgetIndex = colorsWidget.findIndexOfValue(prefs.getString("settings_colors_widget", Constants.PREF_COLORS_APP_DEFAULT));
		String colorsWidgetString = getResources().getStringArray(R.array.colors_widget)[colorsWidgetIndex];		
		colorsWidget.setSummary(colorsWidgetString);		
		colorsWidget.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				int colorsWidgetIndex = colorsWidget.findIndexOfValue(newValue.toString());
				String colorsWidgetString = getResources().getStringArray(R.array.colors_widget)[colorsWidgetIndex];	
				colorsWidget.setSummary(colorsWidgetString);		
				prefs.edit().putString("settings_colors_widget", newValue.toString()).commit();
				colorsWidget.setValueIndex(colorsWidgetIndex);
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
								File cacheDir = StorageManager.getCacheDir(activity);
								StorageManager.delete(activity, cacheDir.getAbsolutePath());
								// App tour is flagged as skipped anyhow
								prefs.edit().putBoolean(Constants.PREF_TOUR_PREFIX + "skipped", true).commit();
								OmniNotes.restartApp(getApplicationContext());
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
		
		
		
		// Instructions
		Preference instructions = findPreference("settings_tour_show_again");
		instructions.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
				// set dialog message
				alertDialogBuilder
						.setMessage(getString(R.string.settings_tour_show_again_summary) + "?")
						.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								AppTourHelper.reset(activity);
								prefs.edit()
										.putString(Constants.PREF_NAVIGATION,
												getResources().getStringArray(R.array.navigation_list_codes)[0])
										.commit();
								OmniNotes.restartApp(getApplicationContext());
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
		
		
		
		
		// Donations
		Preference donation = findPreference("settings_donation");
		donation.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
				
				ArrayList<ImageAndTextItem> options = new ArrayList<ImageAndTextItem>();
				options.add(new ImageAndTextItem(R.drawable.ic_paypal, getString(R.string.paypal)) );
				options.add(new ImageAndTextItem(R.drawable.ic_bitcoin, getString(R.string.bitcoin)) );
				
				alertDialogBuilder
				.setAdapter(new ImageAndTextAdapter(activity, options), new DialogInterface.OnClickListener() {			
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							Intent intentPaypal = new Intent(Intent.ACTION_VIEW);
							intentPaypal.setData(Uri.parse(getString(R.string.paypal_url)));
							startActivity(intentPaypal);
							break;
						case 1:
							Intent intentBitcoin = new Intent(Intent.ACTION_VIEW);
							intentBitcoin.setData(Uri.parse(getString(R.string.bitcoin_url)));
							startActivity(intentBitcoin);
							break;
						}				
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
				if (mAboutOrStatsThread != null && !mAboutOrStatsThread.isAlive()) {
					aboutClickCounter = 0;
				}
				if (aboutClickCounter == 0) {
					aboutClickCounter++;			
					mAboutOrStatsThread = new AboutOrStatsThread(activity, aboutClickCounter);
					mAboutOrStatsThread.start();
				} else {
					mAboutOrStatsThread.setAboutClickCounter(++aboutClickCounter);
				}
				return false;
			}
		});
	}
	

	
	@Override
	public void onStart() {
		// GA tracking
		OmniNotes.getGaTracker().set(Fields.SCREEN_NAME, getClass().getName());
		OmniNotes.getGaTracker().send(MapBuilder.createAppView().build());		
		super.onStart();
	}
	
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case SPRINGPAD_IMPORT:
					if (resultCode == Activity.RESULT_OK) {
						Uri filesUri = intent.getData();
						String path = FileHelper.getPath(this, filesUri);
						// An IntentService will be launched to accomplish the import task
						Intent service = new Intent(activity, DataBackupIntentService.class);
						service.setAction(DataBackupIntentService.ACTION_DATA_IMPORT_SPRINGPAD);
						service.putExtra(DataBackupIntentService.EXTRA_SPRINGPAD_BACKUP, path);
						activity.startService(service);
					} else {
						Crouton.makeText(this, R.string.error_saving_attachments, ONStyle.ALERT)
								.show();
					}
					break;
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
}




/**
 * Thread to launch about screen or stats dialog depending on clicks
 * @author fede
 *
 */
class AboutOrStatsThread extends Thread {

	private final int ABOUT_CLICK_DELAY = 400;
	private int ABOUT_CLICKS_REQUIRED = 3;
	private int aboutClickCounter;
	private Context mContext;
	private boolean startAbout = true;

	private int aboutClickCounterInternal;

	AboutOrStatsThread(Context mContext, int aboutClickCounter) {
		this.mContext = mContext;
		this.aboutClickCounterInternal = aboutClickCounter;

	}

	@Override
	public void run() {
		try {
			Thread.sleep(ABOUT_CLICK_DELAY);
			while (aboutClickCounterInternal != aboutClickCounter) {
				if (aboutClickCounter >= ABOUT_CLICKS_REQUIRED) {
					// Launches StatsActivity
					Intent statsIntent = new Intent(mContext,
							StatsActivity.class);
					mContext.startActivity(statsIntent);
					startAbout = false;
					break;
				}
				Thread.sleep(ABOUT_CLICK_DELAY);
				aboutClickCounterInternal = aboutClickCounter;
			}
			if (startAbout) {
				// Launches about Activity
				Intent aboutIntent = new Intent(mContext, AboutActivity.class);
				mContext.startActivity(aboutIntent);
			}
		} catch (InterruptedException e) {
		}
	}

	public void setAboutClickCounter(int aboutClickCounter) {
		this.aboutClickCounter = aboutClickCounter;
	}	
}
















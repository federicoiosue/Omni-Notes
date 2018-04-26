/*
 * Copyright (C) 2018 Federico Iosue (federico.iosue@gmail.com)
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
package it.feio.android.omninotes;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.*;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import it.feio.android.analitica.AnalyticsHelper;
import it.feio.android.omninotes.async.DataBackupIntentService;
import it.feio.android.omninotes.helpers.AppVersionHelper;
import it.feio.android.omninotes.helpers.LanguageHelper;
import it.feio.android.omninotes.helpers.PermissionsHelper;
import it.feio.android.omninotes.models.ONStyle;
import it.feio.android.omninotes.utils.*;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


public class SettingsFragment extends PreferenceFragment {

	private SharedPreferences prefs;

	private final int SPRINGPAD_IMPORT = 0;
	private final int RINGTONE_REQUEST_CODE = 100;
	public final static String XML_NAME = "xmlName";
	private Activity activity;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int xmlId = R.xml.settings;
		if (getArguments() != null && getArguments().containsKey(XML_NAME)) {
			xmlId = ResourcesUtils.getXmlId(OmniNotes.getAppContext(), ResourcesUtils.ResourceIdentifiers.xml, String
					.valueOf(getArguments().get(XML_NAME)));
		}
		addPreferencesFromResource(xmlId);
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
		prefs = activity.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS);
		setTitle();
	}


	private void setTitle() {
		String title = getString(R.string.settings);
		if (getArguments() != null && getArguments().containsKey(XML_NAME)) {
			String xmlName = getArguments().getString(XML_NAME);
			if (!TextUtils.isEmpty(xmlName)) {
				int stringResourceId = getActivity().getResources().getIdentifier(xmlName.replace("settings_",
						"settings_screen_"), "string", getActivity().getPackageName());
				title = stringResourceId != 0 ? getString(stringResourceId) : title;
			}
		}
		Toolbar toolbar = ((Toolbar) getActivity().findViewById(R.id.toolbar));
		if (toolbar != null) toolbar.setTitle(title);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				getActivity().onBackPressed();
				break;
			default:
				Log.e(Constants.TAG, "Wrong element choosen: " + item.getItemId());
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		super.onPreferenceTreeClick(preferenceScreen, preference);
		if (preference instanceof PreferenceScreen) {
			((SettingsActivity) getActivity()).switchToScreen(preference.getKey());
		}
		return false;
	}


	@SuppressWarnings("deprecation")
	@Override
	public void onResume() {
		super.onResume();

		// Export notes
		Preference export = findPreference("settings_export_data");
		if (export != null) {
			export.setOnPreferenceClickListener(arg0 -> {

				// Inflate layout
				LayoutInflater inflater = getActivity().getLayoutInflater();
				View v = inflater.inflate(R.layout.dialog_backup_layout, null);

				// Finds actually saved backups names
				PermissionsHelper.requestPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, R
						.string.permission_external_storage, activity.findViewById(R.id.crouton_handle), () -> export
						(v));

				return false;
			});
		}

		// Import notes
		Preference importData = findPreference("settings_import_data");
		if (importData != null) {
			importData.setOnPreferenceClickListener(arg0 -> {
				PermissionsHelper.requestPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, R
						.string.permission_external_storage, activity.findViewById(R.id.crouton_handle), () -> importNotes());
				return false;
			});
		}


		// Import notes from Springpad export zip file
		Preference importFromSpringpad = findPreference("settings_import_from_springpad");
		if (importFromSpringpad != null) {
			importFromSpringpad.setOnPreferenceClickListener(arg0 -> {
				Intent intent;
				intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("application/zip");
				if (!IntentChecker.isAvailable(getActivity(), intent, null)) {
					Toast.makeText(getActivity(), R.string.feature_not_available_on_this_device,
							Toast.LENGTH_SHORT).show();
					return false;
				}
				startActivityForResult(intent, SPRINGPAD_IMPORT);
				return false;
			});
		}


//		Preference syncWithDrive = findPreference("settings_backup_drive");
//		importFromSpringpad.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//			@Override
//			public boolean onPreferenceClick(Preference arg0) {
//				Intent intent;
//				intent = new Intent(Intent.ACTION_GET_CONTENT);
//				intent.addCategory(Intent.CATEGORY_OPENABLE);
//				intent.setType("application/zip");
//				if (!IntentChecker.isAvailable(getActivity(), intent, null)) {
//					Crouton.makeText(getActivity(), R.string.feature_not_available_on_this_device,
// ONStyle.ALERT).show();
//					return false;
//				}
//				startActivityForResult(intent, SPRINGPAD_IMPORT);
//				return false;
//			}
//		});


		// Swiping action
		final SwitchPreference swipeToTrash = (SwitchPreference) findPreference("settings_swipe_to_trash");
		if (swipeToTrash != null) {
			if (prefs.getBoolean("settings_swipe_to_trash", false)) {
				swipeToTrash.setChecked(true);
				swipeToTrash.setSummary(getResources().getString(R.string.settings_swipe_to_trash_summary_2));
			} else {
				swipeToTrash.setChecked(false);
				swipeToTrash.setSummary(getResources().getString(R.string.settings_swipe_to_trash_summary_1));
			}
			swipeToTrash.setOnPreferenceChangeListener((preference, newValue) -> {
				if ((Boolean) newValue) {
					swipeToTrash.setSummary(getResources().getString(R.string.settings_swipe_to_trash_summary_2));
				} else {
					swipeToTrash.setSummary(getResources().getString(R.string.settings_swipe_to_trash_summary_1));
				}
				return true;
			});
		}


		// Show uncategorized notes in menu
		final SwitchPreference showUncategorized = (SwitchPreference) findPreference(Constants
				.PREF_SHOW_UNCATEGORIZED);
		if (showUncategorized != null) {
			showUncategorized.setOnPreferenceChangeListener((preference, newValue) -> {
				return true;
			});
		}


		// Show Automatically adds location to new notes
		final SwitchPreference autoLocation = (SwitchPreference) findPreference(Constants.PREF_AUTO_LOCATION);
		if (autoLocation != null) {
			autoLocation.setOnPreferenceChangeListener((preference, newValue) -> {
				return true;
			});
		}


		// Maximum video attachment size
		final EditTextPreference maxVideoSize = (EditTextPreference) findPreference("settings_max_video_size");
		if (maxVideoSize != null) {
			String maxVideoSizeValue = prefs.getString("settings_max_video_size", getString(R.string.not_set));
			maxVideoSize.setSummary(getString(R.string.settings_max_video_size_summary) + ": " + String.valueOf
					(maxVideoSizeValue));
			maxVideoSize.setOnPreferenceChangeListener((preference, newValue) -> {
				maxVideoSize.setSummary(getString(R.string.settings_max_video_size_summary) + ": " + String
						.valueOf(newValue));
				prefs.edit().putString("settings_max_video_size", newValue.toString()).commit();
				return false;
			});
		}


		// Set notes' protection password
		Preference password = findPreference("settings_password");
		if (password != null) {
			password.setOnPreferenceClickListener(preference -> {
				Intent passwordIntent = new Intent(getActivity(), PasswordActivity.class);
				startActivity(passwordIntent);
				return false;
			});
		}


		// Use password to grant application access
		final SwitchPreference passwordAccess = (SwitchPreference) findPreference("settings_password_access");
		if (passwordAccess != null) {
			if (prefs.getString(Constants.PREF_PASSWORD, null) == null) {
				passwordAccess.setEnabled(false);
				passwordAccess.setChecked(false);
			} else {
				passwordAccess.setEnabled(true);
			}
			passwordAccess.setOnPreferenceChangeListener((preference, newValue) -> {
				PasswordHelper.requestPassword(getActivity(), passwordConfirmed -> {
					if (passwordConfirmed) {
						passwordAccess.setChecked((Boolean) newValue);
					}
				});
				return true;
			});
		}


		// Languages
		ListPreference lang = (ListPreference) findPreference("settings_language");
		if (lang != null) {
			String languageName = getResources().getConfiguration().locale.getDisplayName();
			lang.setSummary(languageName.substring(0, 1).toUpperCase(getResources().getConfiguration().locale)
					+ languageName.substring(1, languageName.length()));
			lang.setOnPreferenceChangeListener((preference, value) -> {
				LanguageHelper.updateLanguage(getActivity(), value.toString());
				SystemHelper.restartApp(getActivity().getApplicationContext(), MainActivity.class);
				return false;
			});
		}


		// Text size
		final ListPreference textSize = (ListPreference) findPreference("settings_text_size");
		if (textSize != null) {
			int textSizeIndex = textSize.findIndexOfValue(prefs.getString("settings_text_size", "default"));
			String textSizeString = getResources().getStringArray(R.array.text_size)[textSizeIndex];
			textSize.setSummary(textSizeString);
			textSize.setOnPreferenceChangeListener((preference, newValue) -> {
				int textSizeIndex1 = textSize.findIndexOfValue(newValue.toString());
				String checklistString = getResources().getStringArray(R.array.text_size)[textSizeIndex1];
				textSize.setSummary(checklistString);
				prefs.edit().putString("settings_text_size", newValue.toString()).commit();
				textSize.setValueIndex(textSizeIndex1);
				return false;
			});
		}


		// Application's colors
		final ListPreference colorsApp = (ListPreference) findPreference("settings_colors_app");
		if (colorsApp != null) {
			int colorsAppIndex = colorsApp.findIndexOfValue(prefs.getString("settings_colors_app",
					Constants.PREF_COLORS_APP_DEFAULT));
			String colorsAppString = getResources().getStringArray(R.array.colors_app)[colorsAppIndex];
			colorsApp.setSummary(colorsAppString);
			colorsApp.setOnPreferenceChangeListener((preference, newValue) -> {
				int colorsAppIndex1 = colorsApp.findIndexOfValue(newValue.toString());
				String colorsAppString1 = getResources().getStringArray(R.array.colors_app)[colorsAppIndex1];
				colorsApp.setSummary(colorsAppString1);
				prefs.edit().putString("settings_colors_app", newValue.toString()).commit();
				colorsApp.setValueIndex(colorsAppIndex1);
				return false;
			});
		}


		// Checklists
		final ListPreference checklist = (ListPreference) findPreference("settings_checked_items_behavior");
		if (checklist != null) {
			int checklistIndex = checklist.findIndexOfValue(prefs.getString("settings_checked_items_behavior", "0"));
			String checklistString = getResources().getStringArray(R.array.checked_items_behavior)[checklistIndex];
			checklist.setSummary(checklistString);
			checklist.setOnPreferenceChangeListener((preference, newValue) -> {
				int checklistIndex1 = checklist.findIndexOfValue(newValue.toString());
				String checklistString1 = getResources().getStringArray(R.array.checked_items_behavior)
						[checklistIndex1];
				checklist.setSummary(checklistString1);
				prefs.edit().putString("settings_checked_items_behavior", newValue.toString()).commit();
				checklist.setValueIndex(checklistIndex1);
				return false;
			});
		}


		// Widget's colors
		final ListPreference colorsWidget = (ListPreference) findPreference("settings_colors_widget");
		if (colorsWidget != null) {
			int colorsWidgetIndex = colorsWidget.findIndexOfValue(prefs.getString("settings_colors_widget",
					Constants.PREF_COLORS_APP_DEFAULT));
			String colorsWidgetString = getResources().getStringArray(R.array.colors_widget)[colorsWidgetIndex];
			colorsWidget.setSummary(colorsWidgetString);
			colorsWidget.setOnPreferenceChangeListener((preference, newValue) -> {
				int colorsWidgetIndex1 = colorsWidget.findIndexOfValue(newValue.toString());
				String colorsWidgetString1 = getResources().getStringArray(R.array.colors_widget)[colorsWidgetIndex1];
				colorsWidget.setSummary(colorsWidgetString1);
				prefs.edit().putString("settings_colors_widget", newValue.toString()).commit();
				colorsWidget.setValueIndex(colorsWidgetIndex1);
				return false;
			});
		}

		// Notification snooze delay
		final EditTextPreference snoozeDelay = (EditTextPreference) findPreference
				("settings_notification_snooze_delay");
		if (snoozeDelay != null) {
			String snooze = prefs.getString("settings_notification_snooze_delay", Constants.PREF_SNOOZE_DEFAULT);
			snooze = TextUtils.isEmpty(snooze) ? Constants.PREF_SNOOZE_DEFAULT : snooze;
			snoozeDelay.setSummary(String.valueOf(snooze) + " " + getString(R.string.minutes));
			snoozeDelay.setOnPreferenceChangeListener((preference, newValue) -> {
				String snoozeUpdated = TextUtils.isEmpty(String.valueOf(newValue)) ? Constants
						.PREF_SNOOZE_DEFAULT : String.valueOf(newValue);
				snoozeDelay.setSummary(snoozeUpdated + " " + getString(R.string.minutes));
				prefs.edit().putString("settings_notification_snooze_delay", snoozeUpdated).apply();
				return false;
			});
		}


		// NotificationServiceListener shortcut
		final Preference norificationServiceListenerPreference = findPreference("settings_notification_service_listener");
		if (norificationServiceListenerPreference != null) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
				getPreferenceScreen().removePreference(norificationServiceListenerPreference);
			}
		}


		// Changelog
		Preference changelog = findPreference("settings_changelog");
		if (changelog != null) {
			changelog.setOnPreferenceClickListener(arg0 -> {

				((OmniNotes)getActivity().getApplication()).getAnalyticsHelper().trackEvent(AnalyticsHelper.CATEGORIES.SETTING, "settings_changelog");

				new MaterialDialog.Builder(activity)
						.customView(R.layout.activity_changelog, false)
						.positiveText(R.string.ok)
						.build().show();
				return false;
			});
			try {
				changelog.setSummary(AppVersionHelper.getCurrentAppVersionName(getActivity()));
			} catch (NameNotFoundException e) {
				Log.e(Constants.TAG, "Error retrieving version", e);
			}
		}


		// Settings reset
		Preference resetData = findPreference("reset_all_data");
		if (resetData != null) {
			resetData.setOnPreferenceClickListener(arg0 -> {

				new MaterialDialog.Builder(activity)
						.content(R.string.reset_all_data_confirmation)
						.positiveText(R.string.confirm)
						.callback(new MaterialDialog.ButtonCallback() {
							@Override
							public void onPositive(MaterialDialog dialog) {
								prefs.edit().clear().commit();
								File db = getActivity().getDatabasePath(Constants.DATABASE_NAME);
								StorageHelper.delete(getActivity(), db.getAbsolutePath());
								File attachmentsDir = StorageHelper.getAttachmentDir(getActivity());
								StorageHelper.delete(getActivity(), attachmentsDir.getAbsolutePath());
								File cacheDir = StorageHelper.getCacheDir(getActivity());
								StorageHelper.delete(getActivity(), cacheDir.getAbsolutePath());
								SystemHelper.restartApp(getActivity().getApplicationContext(), MainActivity.class);
							}
						})
						.build().show();

				return false;
			});
		}


		// Instructions
		Preference instructions = findPreference("settings_tour_show_again");
		if (instructions != null) {
			instructions.setOnPreferenceClickListener(arg0 -> {
				new MaterialDialog.Builder(getActivity())
						.content(getString(R.string.settings_tour_show_again_summary) + "?")
						.positiveText(R.string.confirm)
						.callback(new MaterialDialog.ButtonCallback() {
							@Override
							public void onPositive(MaterialDialog materialDialog) {

								((OmniNotes)getActivity().getApplication()).getAnalyticsHelper().trackEvent(AnalyticsHelper.CATEGORIES.SETTING, "settings_tour_show_again");

								prefs.edit().putBoolean(Constants.PREF_TOUR_COMPLETE, false).commit();
								SystemHelper.restartApp(getActivity().getApplicationContext(), MainActivity.class);
							}
						}).build().show();
				return false;
			});
		}


		// Donations
//        Preference donation = findPreference("settings_donation");
//        if (donation != null) {
//            donation.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//                @Override
//                public boolean onPreferenceClick(Preference preference) {
//                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
//
//                    ArrayList<ImageAndTextItem> options = new ArrayList<ImageAndTextItem>();
//                    options.add(new ImageAndTextItem(R.drawable.ic_paypal, getString(R.string.paypal)));
//                    options.add(new ImageAndTextItem(R.drawable.ic_bitcoin, getString(R.string.bitcoin)));
//
//                    alertDialogBuilder
//                            .setAdapter(new ImageAndTextAdapter(getActivity(), options),
//                                    new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            switch (which) {
//                                                case 0:
//                                                    Intent intentPaypal = new Intent(Intent.ACTION_VIEW);
//                                                    intentPaypal.setData(Uri.parse(getString(R.string.paypal_url)));
//                                                    startActivity(intentPaypal);
//                                                    break;
//                                                case 1:
//                                                    Intent intentBitcoin = new Intent(Intent.ACTION_VIEW);
//                                                    intentBitcoin.setData(Uri.parse(getString(R.string.bitcoin_url)));
//                                                    startActivity(intentBitcoin);
//                                                    break;
//                                            }
//                                        }
//                                    });
//
//
//                    // create alert dialog
//                    AlertDialog alertDialog = alertDialogBuilder.create();
//                    // show it
//                    alertDialog.show();
//                    return false;
//                }
//            });
//        }
	}


	private void importNotes() {
		final CharSequence[] backups = StorageHelper.getExternalStoragePublicDir().list();

		if (backups != null && backups.length == 0) {
			((SettingsActivity)getActivity()).showMessage(R.string.no_backups_available, ONStyle.WARN);
		} else {

			MaterialDialog importDialog = new MaterialDialog.Builder(getActivity())
					.title(R.string.data_import_message)
					.items(backups)
					.positiveText(R.string.confirm)
					.callback(new MaterialDialog.ButtonCallback() {
						@Override
						public void onPositive(MaterialDialog materialDialog) {

						}
					}).build();

			// OnShow is overridden to allow long-click on item so user can remove them
			importDialog.setOnShowListener(dialog -> {

				ListView lv = importDialog.getListView();
				assert lv != null;
				lv.setOnItemClickListener((parent, view, position, id) -> {

					// Retrieves backup size
					File backupDir = StorageHelper.getBackupDir(backups[position].toString());
					long size = StorageHelper.getSize(backupDir) / 1024;
					String sizeString = size > 1024 ? size / 1024 + "Mb" : size + "Kb";

					// Check preference presence
					String prefName = StorageHelper.getSharedPreferencesFile(getActivity()).getName();
					boolean hasPreferences = (new File(backupDir, prefName)).exists();

					String message = backups[position]
							+ " (" + sizeString
							+ (hasPreferences ? " " + getString(R.string.settings_included) : "")
							+ ")";

					new MaterialDialog.Builder(getActivity())
							.title(R.string.confirm_restoring_backup)
							.content(message)
							.positiveText(R.string.confirm)
							.callback(new MaterialDialog.ButtonCallback() {
								@Override
								public void onPositive(MaterialDialog materialDialog) {

									((OmniNotes)getActivity().getApplication()).getAnalyticsHelper().trackEvent(AnalyticsHelper.CATEGORIES.SETTING,
											"settings_import_data");

									importDialog.dismiss();

									// An IntentService will be launched to accomplish the import task
									Intent service = new Intent(getActivity(),
											DataBackupIntentService.class);
									service.setAction(DataBackupIntentService.ACTION_DATA_IMPORT);
									service.putExtra(DataBackupIntentService.INTENT_BACKUP_NAME,
											backups[position]);
									getActivity().startService(service);
								}
							}).build().show();
				});

				// Creation of backup removal dialog
				lv.setOnItemLongClickListener((parent, view, position, id) -> {

					// Retrieves backup size
					File backupDir = StorageHelper.getBackupDir(backups[position].toString());
					long size = StorageHelper.getSize(backupDir) / 1024;
					String sizeString = size > 1024 ? size / 1024 + "Mb" : size + "Kb";

					new MaterialDialog.Builder(getActivity())
							.title(R.string.confirm_removing_backup)
							.content(backups[position] + "" + " (" + sizeString + ")")
							.positiveText(R.string.confirm)
							.callback(new MaterialDialog.ButtonCallback() {
								@Override
								public void onPositive(MaterialDialog materialDialog) {
									importDialog.dismiss();
									// An IntentService will be launched to accomplish the deletion task
									Intent service = new Intent(getActivity(),
											DataBackupIntentService.class);
									service.setAction(DataBackupIntentService.ACTION_DATA_DELETE);
									service.putExtra(DataBackupIntentService.INTENT_BACKUP_NAME,
											backups[position]);
									getActivity().startService(service);
								}
							}).build().show();

					return true;
				});
			});

			importDialog.show();
		}
	}


	private void export(View v) {
		final List<String> backups = Arrays.asList(StorageHelper.getExternalStoragePublicDir().list());

		// Sets default export file name
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_EXPORT);
		String fileName = sdf.format(Calendar.getInstance().getTime());
		final EditText fileNameEditText = (EditText) v.findViewById(R.id.export_file_name);
		final TextView backupExistingTextView = (TextView) v.findViewById(R.id.backup_existing);
		fileNameEditText.setHint(fileName);
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

		new MaterialDialog.Builder(getActivity())
				.title(R.string.data_export_message)
				.customView(v, false)
				.positiveText(R.string.confirm)
				.callback(new MaterialDialog.ButtonCallback() {
					@Override
					public void onPositive(MaterialDialog materialDialog) {
						((OmniNotes)getActivity().getApplication()).getAnalyticsHelper().trackEvent(AnalyticsHelper.CATEGORIES.SETTING, "settings_export_data");
						// An IntentService will be launched to accomplish the export task
						Intent service = new Intent(getActivity(), DataBackupIntentService.class);
						service.setAction(DataBackupIntentService.ACTION_DATA_EXPORT);
						String backupName = StringUtils.isEmpty(fileNameEditText.getText().toString()) ?
								fileNameEditText.getHint().toString() : fileNameEditText.getText().toString();
						service.putExtra(DataBackupIntentService.INTENT_BACKUP_NAME, backupName);
						getActivity().startService(service);
					}
				}).build().show();
	}


	@Override
	public void onStart() {
		((OmniNotes)getActivity().getApplication()).getAnalyticsHelper().trackScreenView(getClass().getName());
		super.onStart();
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case SPRINGPAD_IMPORT:
					Uri filesUri = intent.getData();
					String path = FileHelper.getPath(getActivity(), filesUri);
					// An IntentService will be launched to accomplish the import task
					Intent service = new Intent(getActivity(), DataBackupIntentService.class);
					service.setAction(DataBackupIntentService.ACTION_DATA_IMPORT_SPRINGPAD);
					service.putExtra(DataBackupIntentService.EXTRA_SPRINGPAD_BACKUP, path);
					getActivity().startService(service);
					break;

				case RINGTONE_REQUEST_CODE:
					Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
					String notificationSound = uri == null ? null : uri.toString();
					prefs.edit().putString("settings_notification_ringtone", notificationSound).apply();
					break;

				default:
					Log.e(Constants.TAG, "Wrong element choosen: " + requestCode);
			}
		}
	}
}

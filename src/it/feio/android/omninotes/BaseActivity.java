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

import it.feio.android.checklistview.utils.DensityUtil;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.PasswordValidator;
import it.feio.android.omninotes.utils.AlphaManager;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.Security;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.ShowcaseViews;
import com.espian.showcaseview.ShowcaseViews.ItemViewProperties;
import com.espian.showcaseview.ShowcaseViews.OnShowcaseAcknowledged;
import com.espian.showcaseview.targets.ActionItemTarget;
import com.espian.showcaseview.targets.ActionViewTarget;
import com.espian.showcaseview.targets.Target;
import com.espian.showcaseview.targets.ViewTarget;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

@SuppressLint("Registered") 
public class BaseActivity extends ActionBarActivity {

	private final boolean TEST = false;
	
	protected DbHelper db;	
	protected Activity mActivity;
	protected Tracker tracker;
	
	protected SharedPreferences prefs;
	
	// Location variables
	protected LocationManager locationManager;
	protected LocationListener locationListener;
	protected Location currentLocation;
	protected double currentLatitude;
	protected double currentLongitude;

	protected String navigation;

	protected String date_time_format, time_format;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
				
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/*
		 * Executing the application in test will activate ScrictMode to debug
		 * heavy i/o operations on main thread and data sending to GA will be
		 * disabled
		 */
		if (TEST) {
			StrictMode.enableDefaults();
			GoogleAnalytics.getInstance(this).setDryRun(true);
		}
		
		mActivity = this;

		// Preloads shared preferences for all derived classes
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		// The localized (12 or 24 hours) time format is initialized
		date_time_format = prefs.getBoolean("settings_hours_format", true) ? Constants.DATE_FORMAT_SHORT
				: Constants.DATE_FORMAT_SHORT_12;
		time_format = prefs.getBoolean("settings_hours_format", true) ? Constants.DATE_FORMAT_SHORT_TIME
				: Constants.DATE_FORMAT_SHORT_TIME_12;
		
		// Preparation of DbHelper
		db = new DbHelper(this);
		
		// Starts location manager
		setLocationManager();

		// Force menu overflow icon
		try {
	        ViewConfiguration config = ViewConfiguration.get(this);
	        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
	        if(menuKeyField != null) {
	            menuKeyField.setAccessible(true);
	            menuKeyField.setBoolean(config, false);
	        }
	    } catch (Exception ex) {
	        // Ignore exceptions
	    }
		
		
		
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
		// Google Analytics
		EasyTracker.getInstance(this).activityStart(this);
		tracker = GoogleAnalytics.getInstance(this).getTracker("UA-45502770-1");
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		// Navigation selected
		String navNotes = getResources().getStringArray(R.array.navigation_list_codes)[0];
		navigation = prefs.getString(Constants.PREF_NAVIGATION, navNotes);
	}

	@Override
	public void onStop() {
		super.onStop();
		// Google Analytics
		EasyTracker.getInstance(this).activityStop(this);
		if (locationManager != null)
			locationManager.removeUpdates(locationListener);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent settingsIntent = new Intent(this, SettingsActivity.class);
			startActivity(settingsIntent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	

	private void setLocationManager() {
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				updateLocation(location);
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		// A check is done to avoid crash when NETWORK_PROVIDER is not 
		// available (ex. on emulator with API >= 11)
		if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
			locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 60000, 50, locationListener);
		} else {
			locationManager.requestLocationUpdates(
					LocationManager.PASSIVE_PROVIDER, 60000, 50, locationListener);
		}
	}
    
    void updateLocation(Location location){
        currentLocation = location;
        currentLatitude = currentLocation.getLatitude();
        currentLongitude = currentLocation.getLongitude();
    }
    
    

	protected boolean navigationArchived() {
		return "1".equals(prefs.getString(Constants.PREF_NAVIGATION, "0"));
	}

	protected void showToast(CharSequence text, int duration) {
		if (prefs.getBoolean("settings_enable_info", true)) {
			Toast.makeText(getApplicationContext(), text, duration).show();
		}
	}
	
	
	
	/**
	 * Method to validate security password to protect notes.
	 * It uses an interface callback.
	 * @param password
	 * @param mPasswordValidator
	 */
	protected void requestPassword(final PasswordValidator mPasswordValidator) {
		
		final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				this);

		// Inflate layout
		LayoutInflater inflater = getLayoutInflater();
		final View v = inflater.inflate(R.layout.password_request_dialog_layout, null);
		alertDialogBuilder.setView(v);

		// Set dialog message and button
		alertDialogBuilder
			.setCancelable(false)
			.setMessage(getString(R.string.insert_security_password))
			.setPositiveButton(R.string.confirm, null)
			.setNegativeButton(R.string.cancel, null);
		
		AlertDialog dialog = alertDialogBuilder.create();
		
		// Set a listener for dialog button press
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {

		    @Override
		    public void onShow(final DialogInterface dialog) {

		        Button pos = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
		        pos.setOnClickListener(new View.OnClickListener() {

		            @Override
		            public void onClick(View view) {
		            	// When positive button is pressed password correctness is checked
		            	String oldPassword = prefs.getString(
								Constants.PREF_PASSWORD, "");
		            	TextView passwordTextView = (TextView)v.findViewById(R.id.password_request);
						String password = passwordTextView.getText().toString();
						// The check is done on password's hash stored in preferences
						boolean result = Security.md5(password).equals(oldPassword);

						// In case password is ok dialog is dismissed and result sent to callback
		                if (result) {
		                	dialog.dismiss();
							mPasswordValidator.onPasswordValidated(true);
						// If password is wrong the auth flow is not interrupted and simply a message is shown
		                } else {
		                	passwordTextView.setError(getString(R.string.wrong_password));
		                }
		            }
		        });
		        Button neg = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
		        neg.setOnClickListener(new View.OnClickListener() {

		            @Override
		            public void onClick(View view) {
	                	dialog.dismiss();
						mPasswordValidator.onPasswordValidated(false);
		            }
		        });
		    }
		});
		

		dialog.show();
	}
	
	
	
	
	protected void updateNavigation(String nav){
		prefs.edit().putString(Constants.PREF_NAVIGATION, nav).commit();
		navigation = nav;
	}
	

	
	/**
	 * Used for ShowCase library instructions
	 * @param istructionsName
	 * @param target
	 * @param type
	 */
	protected void showCase(String istructionName, int targetId, int type) {
		if (!prefs.getBoolean(istructionName, false)) {
//			ShowcaseView.insertShowcaseViewWithType(type,
//					target, this, istructionsName + "_title", istructionsName + "_detail", new ShowcaseView.ConfigOptions());
			Target target;
			switch (type) {
			case ShowcaseView.ITEM_ACTION_ITEM:
				target = new ActionItemTarget(this, targetId);
				break;
			case ShowcaseView.ITEM_ACTION_HOME:
				target = new ActionViewTarget(this, ActionViewTarget.Type.HOME);
				break;

			default:
				target = new ViewTarget(targetId, this);
				break;
			}
			ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
            //can only dismiss by button click
            co.hideOnClickOutside = false;
            //show only once
            co.shotType = ShowcaseView.TYPE_ONE_SHOT;
            ShowcaseView sv = ShowcaseView.insertShowcaseView(target, this,
            		getStringResourceByName(istructionName + "_title"), getStringResourceByName(istructionName + "_detail"));
            
            // set black background
            sv.setBackgroundColor(getResources().getColor(android.R.color.black));
            // make background a bit transparent
            AlphaManager.setAlpha(sv, 0.8f);
            
//			prefs.edit().putBoolean(istructionsName, true).commit();
		}
	}
	
	
	/**
	 * Builds ShowcaseView and show it
	 * @param viewsArrays
	 *            List of Integer arrays containing the following informations
	 *            that have to be used for ItemViewProperties building: id,
	 *            titleResId, messageResId, itemType, scale, configOptions
	 */
	protected void showCase2(ArrayList<Integer[]> viewsArrays, OnShowcaseAcknowledged mOnShowcaseAcknowledged) {
		
		final float scale = 0.5F;
		ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
		ShowcaseViews mViews;
		if (mOnShowcaseAcknowledged != null) {
			mViews = new ShowcaseViews(this, mOnShowcaseAcknowledged);
		} else {
			mViews = new ShowcaseViews(this);
		}
		
//		LayoutParams lp = new LayoutParams(300, 300);
//		lp.bottomMargin = DensityUtil.convertPixelsToDp(100, this);
//		lp.setMargins(200, 200, 200, 200);
//		co.buttonLayoutParams = lp;
				
//		co.fadeInDuration = 2000;
//		co.centerText = true;
		co.block = true;
		
		
		ItemViewProperties ivp;
		for (Integer[] view : viewsArrays) {
			// No showcase
			if (view[0] == null) {
				ivp = new ItemViewProperties(view[1], view[2], co);
			// No actionbar or reflection types
			} else if (view[3] == null) {
				ivp = new ItemViewProperties(view[0], view[1], view[2], scale, co);
			} else {
				ivp = new ItemViewProperties(view[0], view[1], view[2], view[3], scale, co);
			}
			mViews.addView(ivp);
		}
		
		mViews.show();
		
	}
	
	
	
	
	/**
	 * Retrieves resource by name
	 * @param aString
	 * @return
	 */
	private String getStringResourceByName(String aString) {
		String packageName = getApplicationContext().getPackageName();
		int resId = getResources().getIdentifier(aString, "string", packageName);
		return getString(resId);
	}

	

}

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

import java.lang.reflect.Field;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.utils.Constants;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.Toast;

public class BaseActivity extends ActionBarActivity {

	private final boolean TEST = true;
	
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
	protected double noteLatitude;
	protected double noteLongitude;

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

		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 60000, 50, locationListener);
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
	 * Set custom locale on application start
	 */
	// protected void checkLocale(){
	// String languageToLoad = prefs.getString("settings_language",
	// Locale.getDefault().getCountry());
	// Locale locale = new Locale(languageToLoad);
	// Locale.setDefault(locale);
	// Configuration config = new Configuration();
	// config.locale = locale;
	// getBaseContext().getResources().updateConfiguration(config,
	// getBaseContext().getResources().getDisplayMetrics());
	// }
	


	

}

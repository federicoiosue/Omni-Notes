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
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.KeyboardUtils;
import it.feio.android.omninotes.utils.Security;
import it.feio.android.omninotes.widget.ListWidgetProvider;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.ShowcaseViews;
import com.espian.showcaseview.ShowcaseViews.ItemViewProperties;
import com.espian.showcaseview.ShowcaseViews.OnShowcaseAcknowledged;

@SuppressLint("Registered") 
public class BaseActivity extends ActionBarActivity {

	private final boolean TEST = false;

	protected final int TRANSITION_VERTICAL = 0;
	protected final int TRANSITION_HORIZONTAL = 1;
	
	protected DbHelper db;	
	protected Activity mActivity;
	
	protected SharedPreferences prefs;
	
	// Location variables
	protected LocationManager locationManager;
	protected LocationListener locationListener;
	protected Location currentLocation;
	protected double currentLatitude;
	protected double currentLongitude;

	protected String navigation;
	protected String navigationTmp; // used for widget navigation


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_list, menu);
				
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
//			GoogleAnalytics.getInstance(this).setDryRun(true);
		}
		
		mActivity = this;

		// Preloads shared preferences for all derived classes
		prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);
		
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
	    } catch (Exception ex) {}
				
		
		super.onCreate(savedInstanceState);
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		// Navigation selected
		String navNotes = getResources().getStringArray(R.array.navigation_list_codes)[0];
		navigation = prefs.getString(Constants.PREF_NAVIGATION, navNotes);
		Log.d(Constants.TAG, prefs.getAll().toString());
	}

	
	@Override
	public void onStop() {
		super.onStop();
		if (locationManager != null)
			locationManager.removeUpdates(locationListener);
	}

	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case R.id.menu_settings:
//			Intent settingsIntent = new Intent(this, SettingsActivity.class);
//			startActivity(settingsIntent);
//			break;
//		}
//		return super.onOptionsItemSelected(item);
//	}
	
	

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
	public static void requestPassword(final Activity mActivity, final PasswordValidator mPasswordValidator) {
		
		final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);

		// Inflate layout
		LayoutInflater inflater = mActivity.getLayoutInflater();
		final View v = inflater.inflate(R.layout.password_request_dialog_layout, null);
		alertDialogBuilder.setView(v);

		// Set dialog message and button
		alertDialogBuilder
			.setCancelable(false)
			.setMessage(mActivity.getString(R.string.insert_security_password))
			.setPositiveButton(R.string.confirm, null)
			.setNegativeButton(R.string.cancel, null);
		
		AlertDialog dialog = alertDialogBuilder.create();
    	final EditText passwordEditText = (EditText)v.findViewById(R.id.password_request);
		
		// Set a listener for dialog button press
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {

		    @Override
		    public void onShow(final DialogInterface dialog) {

		        Button pos = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
		        pos.setOnClickListener(new View.OnClickListener() {

		            @Override
		            public void onClick(View view) {
		            	// When positive button is pressed password correctness is checked
		            	String oldPassword = mActivity.getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS)
		            			.getString(Constants.PREF_PASSWORD, "");
						String password = passwordEditText.getText().toString();
						// The check is done on password's hash stored in preferences
						boolean result = Security.md5(password).equals(oldPassword);

						// In case password is ok dialog is dismissed and result sent to callback
		                if (result) {
		                	dialog.dismiss();
			            	KeyboardUtils.hideKeyboard(passwordEditText);
							mPasswordValidator.onPasswordValidated(true);
						// If password is wrong the auth flow is not interrupted and simply a message is shown
		                } else {
		                	passwordEditText.setError(mActivity.getString(R.string.wrong_password));
		                }
		            }
		        });
		        Button neg = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
		        neg.setOnClickListener(new View.OnClickListener() {

		            @Override
		            public void onClick(View view) {
		            	KeyboardUtils.hideKeyboard(passwordEditText);
	                	dialog.dismiss();
		            	KeyboardUtils.hideKeyboard(passwordEditText);
						mPasswordValidator.onPasswordValidated(false);
		            }
		        });
		    }
		});
		

		dialog.show();
		
		// Force focus and shows soft keyboard
//		passwordEditText.requestFocus();
//		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		KeyboardUtils.showKeyboard(passwordEditText);

	}
	
	
	
	protected void updateNavigation(String nav){
		prefs.edit().putString(Constants.PREF_NAVIGATION, nav).commit();
		navigation = nav;
		navigationTmp = null;
	}
	
	
	
	/**
	 * Builds ShowcaseView and show it
	 * @param viewsArrays
	 *            List of Integer arrays containing the following informations
	 *            that have to be used for ItemViewProperties building: id,
	 *            titleResId, messageResId, itemType, scale, configOptions
	 */
	protected void showCaseView(ArrayList<Integer[]> viewsArrays, OnShowcaseAcknowledged mOnShowcaseAcknowledged) {
		
		final float scale = 0.6F;
		ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
		ShowcaseViews mViews;
		if (mOnShowcaseAcknowledged != null) {
			mViews = new ShowcaseViews(this, mOnShowcaseAcknowledged);
		} else {
			mViews = new ShowcaseViews(this);
		}
		
		LayoutParams lp = new LayoutParams(300, 300);
		lp.bottomMargin = DensityUtil.convertPixelsToDp(100, this);
		lp.setMargins(12, 12, 12, getResources().getDimensionPixelSize(R.dimen.showcase_margin_bottom));
		co.buttonLayoutParams = lp;				
		
		co.fadeInDuration = 700;
		
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
			
			// Animated hand gesture
			if (view.length > 4) {
				int index = viewsArrays.indexOf(view);
				mViews.addAnimatedGestureToView(index, view[4], view[5], view[6], view[7], true);
			}			
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

	
	/**
	 * Notifies App Widgets about data changes so they can update theirselves
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void notifyAppWidgets(Context mActivity) {
		// Home widgets
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			AppWidgetManager mgr = AppWidgetManager.getInstance(mActivity);
			int[] ids = mgr.getAppWidgetIds(new ComponentName(mActivity,
					ListWidgetProvider.class));
			Log.d(Constants.TAG, "Notifies AppWidget data changed for widgets " + ids);
			mgr.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
		}
		
		// Dashclock
	    LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(Constants.INTENT_UPDATE_DASHCLOCK));
	}
	
	
	@SuppressLint("InlinedApi")
	protected void animateTransition(FragmentTransaction transaction, int direction) {
		boolean rtl = false;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			rtl = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL;
		}
		if (direction == TRANSITION_HORIZONTAL) {
			if (rtl) {
				transaction.setCustomAnimations(R.animator.slide_left, R.animator.slide_right,
						R.animator.slide_back_right, R.animator.slide_back_left);
			} else {
				transaction.setCustomAnimations(R.animator.slide_back_right, R.animator.slide_back_left,
						R.animator.slide_left, R.animator.slide_right);
			}
		}
		if (direction == TRANSITION_VERTICAL && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {		
			transaction.setCustomAnimations(
	                R.animator.anim_in, R.animator.anim_out, R.animator.anim_in_pop, R.animator.anim_out_pop);
		}
	}
	
	
	protected void setActionBarTitle(String title) {
		// Creating a spannable to support custom fonts on ActionBar
		int actionBarTitle = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
		android.widget.TextView actionBarTitleView = (android.widget.TextView) getWindow().findViewById(actionBarTitle);
		Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
		if (actionBarTitleView != null) {
			actionBarTitleView.setTypeface(font);
		}

		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(title);
		}
	}
	
	
	
	public String getNavigationTmp() {
		return navigationTmp;
	}
	
	

	

}

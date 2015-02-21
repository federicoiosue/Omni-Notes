/*
 * Copyright (C) 2015 Federico Iosue (federico.iosue@gmail.com)
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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.*;
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
import android.support.v7.app.ActionBarActivity;
import android.view.*;
import android.widget.EditText;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.PasswordValidator;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.GeocodeHelper;
import it.feio.android.omninotes.utils.KeyboardUtils;
import it.feio.android.omninotes.utils.Security;
import it.feio.android.omninotes.widget.ListWidgetProvider;
import roboguice.util.Ln;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

//import com.espian.showcaseview.ShowcaseView;
//import com.espian.showcaseview.ShowcaseViews;
//import com.espian.showcaseview.ShowcaseViews.ItemViewProperties;
//import com.espian.showcaseview.ShowcaseViews.OnShowcaseAcknowledged;


@SuppressLint("Registered")
public class BaseActivity extends ActionBarActivity implements LocationListener {

    protected final int TRANSITION_VERTICAL = 0;
    protected final int TRANSITION_HORIZONTAL = 1;

    protected SharedPreferences prefs;

    // Location variables
    protected LocationManager locationManager;
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
        StrictMode.enableDefaults();
        // Preloads shared preferences for all derived classes
        prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);
        // Starts location manager
        locationManager = GeocodeHelper.getLocationManager(this, this);
        // Force menu overflow icon
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            Ln.d("Just a little issue in physical menu button management", e);
        }
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Navigation selected
        String navNotes = getResources().getStringArray(R.array.navigation_list_codes)[0];
        navigation = prefs.getString(Constants.PREF_NAVIGATION, navNotes);
        Ln.d(prefs.getAll().toString());
    }


    @Override
    public void onStop() {
        super.onStop();
        if (locationManager != null)
            locationManager.removeUpdates(this);
    }


    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        currentLatitude = currentLocation.getLatitude();
        currentLongitude = currentLocation.getLongitude();
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }


    @Override
    public void onProviderEnabled(String provider) {

    }


    @Override
    public void onProviderDisabled(String provider) {

    }


    protected void showToast(CharSequence text, int duration) {
        if (prefs.getBoolean("settings_enable_info", true)) {
            Toast.makeText(getApplicationContext(), text, duration).show();
        }
    }


    /**
     * Method to validate security password to protect notes.
     * It uses an interface callback.
     */
    public static void requestPassword(final Activity mActivity, final PasswordValidator mPasswordValidator) {

        // Inflate layout
        LayoutInflater inflater = mActivity.getLayoutInflater();
        final View v = inflater.inflate(R.layout.password_request_dialog_layout, null);
        final EditText passwordEditText = (EditText) v.findViewById(R.id.password_request);

        MaterialDialog dialog = new MaterialDialog.Builder(mActivity)
                .autoDismiss(false)
                .title(R.string.insert_security_password)
                .customView(v, false)
                .positiveText(R.string.ok)
                .callback(new MaterialDialog.SimpleCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        // When positive button is pressed password correctness is checked
                        String oldPassword = mActivity.getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS)
                                .getString(Constants.PREF_PASSWORD, "");
                        String password = passwordEditText.getText().toString();
                        // The check is done on password's hash stored in preferences
                        boolean result = Security.md5(password).equals(oldPassword);

                        // In case password is ok dialog is dismissed and result sent to callback
                        if (result) {
                            KeyboardUtils.hideKeyboard(passwordEditText);
                            dialog.dismiss();
                            mPasswordValidator.onPasswordValidated(true);
                            // If password is wrong the auth flow is not interrupted and simply a message is shown
                        } else {
                            passwordEditText.setError(mActivity.getString(R.string.wrong_password));
                        }
                    }
                }).build();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                KeyboardUtils.hideKeyboard(passwordEditText);
                dialog.dismiss();
                mPasswordValidator.onPasswordValidated(false);
            }
        });

        dialog.show();

        // Force focus and shows soft keyboard
        KeyboardUtils.showKeyboard(passwordEditText);
    }


    /**
     * Method to validate security password to protect a list of notes.
     * When "Request password on access" in switched on this check not required all the times.
     * It uses an interface callback.
     */
    public void requestPassword(final Activity mActivity, List<Note> notes, 
                                final PasswordValidator mPasswordValidator) {
        if (prefs.getBoolean("settings_password_access", false)) {
            mPasswordValidator.onPasswordValidated(true);
            return;
        }

        boolean askForPassword = false;
        for (Note note : notes) {
            if (note.isLocked()) {
                askForPassword = true;
                break;
            }
        }
        if (askForPassword) {
            BaseActivity.requestPassword(mActivity, new PasswordValidator() {
                @Override
                public void onPasswordValidated(boolean passwordConfirmed) {
                    mPasswordValidator.onPasswordValidated(passwordConfirmed);
                }
            });
        } else {
            mPasswordValidator.onPasswordValidated(true);
        }
    }


    public void updateNavigation(String nav) {
        prefs.edit().putString(Constants.PREF_NAVIGATION, nav).apply();
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
//	protected void showCaseView(ArrayList<Integer[]> viewsArrays, OnShowcaseAcknowledged mOnShowcaseAcknowledged) {
//
//		final float scale = 0.6F;
//		ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
//		ShowcaseViews mViews;
//		if (mOnShowcaseAcknowledged != null) {
//			mViews = new ShowcaseViews(this, mOnShowcaseAcknowledged);
//		} else {
//			mViews = new ShowcaseViews(this);
//		}
//
//		LayoutParams lp = new LayoutParams(300, 300);
//		lp.bottomMargin = DensityUtil.dpToPx(100, this);
//		lp.setMargins(12, 12, 12, getResources().getDimensionPixelSize(R.dimen.showcase_margin_bottom));
//		co.buttonLayoutParams = lp;
//
//		co.fadeInDuration = 700;
//
//		ItemViewProperties ivp;
//		for (Integer[] view : viewsArrays) {
//
//			// No showcase
//			if (view[0] == null) {
//				ivp = new ItemViewProperties(view[1], view[2], co);
//
//			// No actionbar or reflection types
//			} else if (view[3] == null) {
//				ivp = new ItemViewProperties(view[0], view[1], view[2], scale, co);
//			} else {
//				ivp = new ItemViewProperties(view[0], view[1], view[2], view[3], scale, co);
//			}
//			mViews.addView(ivp);
//
//			// Animated hand gesture
//			if (view.length > 4) {
//				int index = viewsArrays.indexOf(view);
//				mViews.addAnimatedGestureToView(index, view[4], view[5], view[6], view[7], true);
//			}
//		}
//
//		mViews.show();
//
//	}


    /**
     * Retrieves resource by name
     *
     * @param aString
     * @returnnotifyAppWidgets
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
        AppWidgetManager mgr = AppWidgetManager.getInstance(mActivity);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(mActivity, ListWidgetProvider.class));
        Ln.d("Notifies AppWidget data changed for widgets " + Arrays.toString(ids));
        mgr.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);

        // Dashclock
        LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(Constants.INTENT_UPDATE_DASHCLOCK));
    }


    @SuppressLint("InlinedApi")
    protected void animateTransition(FragmentTransaction transaction, int direction) {
        if (direction == TRANSITION_HORIZONTAL) {
            transaction.setCustomAnimations(R.animator.fade_in_support, R.animator.fade_out_support, 
                    R.animator.fade_in_support, R.animator.fade_out_support);
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

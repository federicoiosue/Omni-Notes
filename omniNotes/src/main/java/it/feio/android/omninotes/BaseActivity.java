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
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.PasswordValidator;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.KeyboardUtils;
import it.feio.android.omninotes.utils.Navigation;
import it.feio.android.omninotes.utils.Security;
import it.feio.android.omninotes.widget.ListWidgetProvider;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    protected final int TRANSITION_VERTICAL = 0;
    protected final int TRANSITION_HORIZONTAL = 1;

    public SharedPreferences prefs;

    protected String navigation;
    protected String navigationTmp; // used for widget navigation


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);
        // Force menu overflow icon
        try {
            ViewConfiguration config = ViewConfiguration.get(this.getApplicationContext());
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            Log.d(Constants.TAG, "Just a little issue in physical menu button management", e);
        }
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onResume() {
        super.onResume();
        String navNotes = getResources().getStringArray(R.array.navigation_list_codes)[0];
        navigation = prefs.getString(Constants.PREF_NAVIGATION, navNotes);
        Log.d(Constants.TAG, prefs.getAll().toString());
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
                .callback(new MaterialDialog.ButtonCallback() {
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

        dialog.setOnCancelListener(dialog1 -> {
			KeyboardUtils.hideKeyboard(passwordEditText);
			dialog1.dismiss();
			mPasswordValidator.onPasswordValidated(false);
		});

        dialog.show();

        // Force focus and shows soft keyboard
		new Handler().postDelayed(() -> KeyboardUtils.showKeyboard(passwordEditText), 100);
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
            BaseActivity.requestPassword(mActivity, mPasswordValidator::onPasswordValidated);
        } else {
            mPasswordValidator.onPasswordValidated(true);
        }
    }


	public boolean updateNavigation(String nav) {
		if (nav.equals(navigationTmp) || (navigationTmp == null && Navigation.getNavigationText().equals(nav))) {
			return false;
		}
		prefs.edit().putString(Constants.PREF_NAVIGATION, nav).apply();
		navigation = nav;
		navigationTmp = null;
		return true;
	}


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
    public static void notifyAppWidgets(Context context) {
        // Home widgets
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(context, ListWidgetProvider.class));
        Log.d(Constants.TAG, "Notifies AppWidget data changed for widgets " + Arrays.toString(ids));
        mgr.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);

        // Dashclock
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.INTENT_UPDATE_DASHCLOCK));
    }


    @SuppressLint("InlinedApi")
    protected void animateTransition(FragmentTransaction transaction, int direction) {
        if (direction == TRANSITION_HORIZONTAL) {
            transaction.setCustomAnimations(R.anim.fade_in_support, R.anim.fade_out_support,
                    R.anim.fade_in_support, R.anim.fade_out_support);
        }
        if (direction == TRANSITION_VERTICAL && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            transaction.setCustomAnimations(
                    R.anim.anim_in, R.anim.anim_out, R.anim.anim_in_pop, R.anim.anim_out_pop);
        }
    }


    protected void setActionBarTitle(String title) {
        // Creating a spannable to support custom fonts on ActionBar
        int actionBarTitle = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        android.widget.TextView actionBarTitleView = (android.widget.TextView) getWindow().findViewById(actionBarTitle);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        //if (actionBarTitleView != null) {
        //    actionBarTitleView.setTypeface(font);
        //}

//        CollapsingToolbarLayout collapsingToolbar =
//                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
//        collapsingToolbar.setCollapsedTitleTypeface(font);
//        collapsingToolbar.setTitle("Title");


        //if (getSupportActionBar() != null) {
        //    getSupportActionBar().setTitle(title);
        //}
    }


    public String getNavigationTmp() {
        return navigationTmp;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_MENU || super.onKeyDown(keyCode, event);
    }
}

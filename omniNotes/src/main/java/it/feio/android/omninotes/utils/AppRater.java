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

package it.feio.android.omninotes.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import com.neopixl.pixlui.components.textview.TextView;
import it.feio.android.omninotes.R;


public class AppRater {

    private final static String APP_PNAME = "it.feio.android.omninotes";

    private final static int DAYS_UNTIL_PROMPT = 0;
    private final static int LAUNCHES_UNTIL_PROMPT = 2;


    public static void appLaunched(Activity mActivity, String message, String rateBtn, String dismissBtn, 
                                   String laterBtn) {
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS);
        if (prefs.getBoolean(Constants.PREF_RATE_DISMISSED, false)) {
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong(Constants.PREF_LAUNCH_COUNT, 0) + 1;
        editor.putLong(Constants.PREF_LAUNCH_COUNT, launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong(Constants.PREF_FIRST_LAUNCH, 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong(Constants.PREF_FIRST_LAUNCH, date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(mActivity, message, rateBtn, dismissBtn, laterBtn, editor);
            }
        }

        editor.commit();
    }


    private static void showRateDialog(final Activity mActivity, String message, String rateBtn, String dismissBtn,
                                       String laterBtn, final SharedPreferences.Editor editor) {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);

        // Inflate layout
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View v = inflater.inflate(R.layout.rate_app_dialog_layout, null);
        dialog.setView(v);

        // Instantiate and populate views
        TextView messageTextView = (TextView) v.findViewById(R.id.rate_message);
        messageTextView.setText(message);
        final CheckBox dismissCheckbox = (CheckBox) v.findViewById(R.id.rate_checkbox);
        dismissCheckbox.setText(dismissBtn);

        dialog
//			.setTitle(APP_TITLE)
//			.setMessage(message)
                .setPositiveButton(rateBtn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + 
                                APP_PNAME)));
                        editor.putBoolean(Constants.PREF_RATE_DISMISSED, true).commit();
                        dialog.dismiss();
                    }
                })
                .setNeutralButton(laterBtn, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dismissCheckbox.isChecked()) {
                            editor.putBoolean(Constants.PREF_RATE_DISMISSED, true).commit();
                        } else {
                            editor.putLong(Constants.PREF_FIRST_LAUNCH, System.currentTimeMillis()).commit();
                        }
                        dialog.dismiss();
                    }
                });
//			.setNegativeButton(dismissBtn, new DialogInterface.OnClickListener() {
//				
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					if (editor != null) {
//						editor.putBoolean(Constants.PREF_RATE_DISMISSED, true).commit();
//					}
//					dialog.dismiss();
//				}
//			});

        dialog.show();
    }
}
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
package it.feio.android.omninotes.async;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.utils.Constants;
import org.json.JSONObject;
import roboguice.util.Ln;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GooglePlayServicesUtil;


public class UpdaterTask extends AsyncTask<String, Void, Void> {

    private final String BETA = " Beta ";
    private final WeakReference<Activity> mActivityReference;
    private final Activity mActivity;
    String url;
    private String packageName;
    private boolean promptUpdate = false;


    public UpdaterTask(Activity mActivity) {
        this.mActivityReference = new WeakReference<>(mActivity);
        this.mActivity = mActivity;
    }


    @Override
    protected void onPreExecute() {
        String packageName = mActivity.getApplicationContext().getPackageName();
        url = Constants.PS_METADATA_FETCHER_URL + Constants.PLAY_STORE_URL
                + packageName;
        super.onPreExecute();
    }


    @Override
    protected Void doInBackground(String... params) {

        String appData = getAppData();
        try {
            // Creation of json object
            JSONObject json = new JSONObject(appData);

            promptUpdate = isVersionUpdated(json.getString("softwareVersion"));

            // Getting from preferences last update check
            @SuppressWarnings("static-access")
            SharedPreferences prefs = mActivity.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS);

            long now = System.currentTimeMillis();
            if (promptUpdate
                    && now > prefs.getLong(Constants.PREF_LAST_UPDATE_CHECK, 0)
                    + Constants.UPDATE_MIN_FREQUENCY) {
                promptUpdate = true;
                prefs.edit().putLong(Constants.PREF_LAST_UPDATE_CHECK, now)
                        .commit();
            } else {
                promptUpdate = false;
            }

        } catch (Exception e) {
            Ln.w(e, "Error fetching app metadata");
        }

        return null;
    }


    private void promptUpdate() {

        // Confirm dialog creation
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                mActivityReference.get());
        alertDialogBuilder
                .setCancelable(false)
                .setMessage(R.string.new_update_available)
                .setPositiveButton(R.string.update,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                if (isGooglePlayAvailable()) {
                                    mActivityReference.get().startActivity(new Intent(
                                            Intent.ACTION_VIEW, Uri
                                            .parse("market://details?id="
                                                    + packageName)));
                                } else {

                                    // MapBuilder.createEvent().build() returns a Map of event fields and values
                                    // that are set and sent with the hit.
                                    OmniNotes.getGaTracker().send(MapBuilder
                                            .createEvent("ui_action",     // Event category (required)
                                                    "button_press",  // Event action (required)
                                                    "Google Drive Update",   // Event label
                                                    null)            // Event value
                                            .build());

                                    mActivityReference.get().startActivity(new Intent(
                                            Intent.ACTION_VIEW, Uri
                                            .parse(Constants.DRIVE_FOLDER_LAST_BUILD)));
                                }

                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(R.string.not_now,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    @Override
    protected void onPostExecute(Void result) {
        if (isAlive(mActivityReference)) {
            if (promptUpdate) {
                promptUpdate();
            } else {
                showChangelog();
            }
        }
    }


    private void showChangelog() {
        try {
            String newVersion = mActivity.getPackageManager().getPackageInfo(
                    mActivity.getPackageName(), 0).versionName;
            String currentVersion = mActivity.getSharedPreferences(Constants.PREFS_NAME,
                    Context.MODE_MULTI_PROCESS).getString(Constants.PREF_CURRENT_APP_VERSION, "");
            if (!newVersion.equals(currentVersion)) {
                new MaterialDialog.Builder(mActivity)
                        .customView(R.layout.activity_changelog, false)
                        .positiveText(R.string.ok)
                        .build().show();
                mActivity.getSharedPreferences(Constants.PREFS_NAME,
                        Context.MODE_MULTI_PROCESS).edit().putString(Constants.PREF_CURRENT_APP_VERSION,
                        newVersion).commit();
            }
        } catch (NameNotFoundException e) {
            Ln.e("Error retrieving app version", e);
        }
    }


    /**
     * Cheks if activity is still alive and not finishing
     *
     * @param weakActivityReference
     * @return True or false
     */
    private boolean isAlive(WeakReference<Activity> weakActivityReference) {
        return !(weakActivityReference.get() == null || weakActivityReference.get().isFinishing());
    }


    /**
     * Fecth application data from internet
     *
     * @return
     */
    public String getAppData() {
        StringBuilder sb = new StringBuilder();

        packageName = mActivity.getPackageName();

        try {
            // get URL content
            URL url = new URL(Constants.PS_METADATA_FETCHER_URL
                    + Constants.PLAY_STORE_URL + packageName);
            URLConnection conn = url.openConnection();

            // open the stream and put it into BufferedReader
            InputStream is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String inputLine;

            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            is.close();

        } catch (MalformedURLException e) {
            Ln.e(e, "Error fetching app metadata");
        } catch (IOException e) {
            Ln.w(e, "Error fetching app metadata");
        }

        return sb.toString();
    }


    /**
     * Checks parsing "android:versionName" if app has been updated
     *
     * @throws NameNotFoundException
     */
    private boolean isVersionUpdated(String playStoreVersion)
            throws NameNotFoundException {

        boolean result = false;

        // Retrieval of installed app version
        PackageInfo pInfo = mActivity.getPackageManager().getPackageInfo(
                mActivity.getPackageName(), 0);
        String installedVersion = pInfo.versionName;

        // Parsing version string to obtain major.minor.point (excluding eventually beta)
        String[] playStoreVersionArray = playStoreVersion.split(BETA)[0].split("\\.");
        String[] installedVersionArray = installedVersion.split(BETA)[0].split("\\.");

        // Versions strings are converted into integer
        String playStoreVersionString = playStoreVersionArray[0];
        String installedVersionString = installedVersionArray[0];
        for (int i = 1; i < playStoreVersionArray.length; i++) {
            playStoreVersionString += String.format("%02d", Integer.parseInt(playStoreVersionArray[i]));
            installedVersionString += String.format("%02d", Integer.parseInt(installedVersionArray[i]));
        }

        // And then compared
        if (Integer.parseInt(playStoreVersionString) > Integer.parseInt(installedVersionString)) {
            result = true;
        }

        // And then compared again to check if we're out of Beta
        else if (Integer.parseInt(playStoreVersionString) == Integer.parseInt(installedVersionString)
                && playStoreVersion.split("b").length == 1 && installedVersion.split("b").length == 2) {
            result = true;
        }

        return result;
    }


    /**
     * Checks Google Play availability
     *
     * @return
     */
    private boolean isGooglePlayAvailable() {
        boolean googlePlayStoreInstalled;
        int val = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mActivity);
        googlePlayStoreInstalled = val == ConnectionResult.SUCCESS;
        return googlePlayStoreInstalled;
    }
}

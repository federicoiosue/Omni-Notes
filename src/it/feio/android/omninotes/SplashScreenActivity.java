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

import it.feio.android.omninotes.utils.AppTourHelper;
import it.feio.android.omninotes.utils.Constants;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

public class SplashScreenActivity extends BaseActivity {
	
	private Intent launchMainActivity = new Intent();
	private Activity mActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		// Hiding actionbar for splashscreen
		getSupportActionBar().hide();
		
		mActivity = this;
		init();
	}

	
	
	/**
	 * Checks if splashscreen must be shown and launches ListActivity
	 */
	private void init() {
		
		// Getting last opening time
		long openTime = Calendar.getInstance().getTimeInMillis();
		long lastOpenTime = prefs.getLong("last_app_open", openTime - Constants.SPLASH_MIN_OFFSET);
	
		// Saving application opening time
		prefs.edit().putLong("last_app_open", openTime).commit();
		
		// If is not passed enough time splash image is skipped
		if (openTime - lastOpenTime < Constants.SPLASH_MIN_OFFSET) {
			launchMainActivity();
			return;
		}
			
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// This method will be executed once the timer is over
				requestShowCaseViewVisualization();
			}
		}, Constants.SPLASH_TIME_OUT);
	}
	

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		Window window = getWindow();
		window.setFormat(PixelFormat.RGBA_8888);
	}
	
	
	private void launchMainActivity() {
		launchMainActivity.setClass(this, ListActivity.class);
		startActivity(launchMainActivity);
		finish();
	}




	/**
	 * Showcase view displaying request for first launch
	 */
	private void requestShowCaseViewVisualization() {
		
		if (AppTourHelper.neverDone(this)) {		
			final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder
				.setTitle(R.string.app_name)
				.setMessage(R.string.tour_request_start)
				.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						launchMainActivity();
					}			
			}).setNegativeButton(R.string.not_now, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					AppTourHelper.skip(mActivity);
					launchMainActivity();
				}
			});
			if (!mActivity.isFinishing()) {
				alertDialogBuilder.create().show();
			} else {
				launchMainActivity();
			}
		} else {
			launchMainActivity();
		}
	}

}

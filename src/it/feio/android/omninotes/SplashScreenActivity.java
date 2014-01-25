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

import it.feio.android.omninotes.utils.Constants;

import java.util.Calendar;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

public class SplashScreenActivity extends BaseActivity {
	
	private Intent launchMainActivity = new Intent();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		// Hiding actionbar for splashscreen
		getSupportActionBar().hide();
		
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

			/*
			 * Showing splash screen with a timer. This will be useful when you
			 * want to show case your app logo / company
			 */

			@Override
			public void run() {
				// This method will be executed once the timer is over
				// Start your app main activity
				launchMainActivity();

				// close this activity
				finish();
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
	}

}

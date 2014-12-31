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

import android.os.Bundle;
import android.webkit.WebView;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

public class AboutActivity extends BaseActivity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		WebView webview = (WebView) findViewById(R.id.webview);
		webview.loadUrl("file:///android_asset/html/about.html");

		// Show the Up button in the action bar.
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	
	@Override
	public void onStart() {
		// GA tracking
		OmniNotes.getGaTracker().set(Fields.SCREEN_NAME, getClass().getName());
		OmniNotes.getGaTracker().send(MapBuilder.createAppView().build());		
		super.onStart();
	}
	

	@Override
	public boolean onNavigateUp() {
		onBackPressed();
		return super.onNavigateUp();
	}

}

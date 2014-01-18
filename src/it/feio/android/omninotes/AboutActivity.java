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

import android.os.Bundle;
import android.webkit.WebView;

public class AboutActivity extends BaseActivity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WebView webview = new WebView(this);
		setContentView(webview);

		setContentView(webview);
		webview.loadUrl("file:///android_asset/html/about.html");

		// Show the Up button in the action bar.
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}
	
	@Override
	public boolean onNavigateUp() {
		onBackPressed();
		return super.onNavigateUp();
	}

}

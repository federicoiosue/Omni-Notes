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
package it.feio.android.omninotes.async;

import it.feio.android.omninotes.utils.Constants;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class UpdaterTask extends AsyncTask<String, Void, Void> {

	private Context mContext;
	String url; 

	public UpdaterTask(Context mContext) {
		this.mContext = mContext;
	}
	
	
	@Override
	protected void onPreExecute() {
		String packageName = mContext.getApplicationContext().getPackageName();
		url = Constants.PS_METADATA_FETCHER_URL + Constants.PLAY_STORE_URL
				+ packageName;
		super.onPreExecute();
	}
	

	@Override
	protected Void doInBackground(String... params) {

		String appData = getAppData();
		try {
			JSONArray jsonArray = new JSONArray(appData);
			Log.i(Constants.TAG, "Number of entries " + jsonArray.length());
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				Log.i(Constants.TAG, jsonObject.getString("text"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	@Override
	protected void onPostExecute(Void result) {

	}

//	public String getAppData() {
//		StringBuilder sb = new StringBuilder();
//		HttpClient client = new DefaultHttpClient();
//		String packageName = mContext.getApplicationContext().getPackageName();
//		URL url;
//
//		try {
//			// get URL content
//			url = new URL(Constants.PS_METADATA_FETCHER_URL
//					+ Constants.PLAY_STORE_URL + packageName);
//			URLConnection conn = url.openConnection();
//
//			// open the stream and put it into BufferedReader
//			BufferedReader br = new BufferedReader(new InputStreamReader(
//					conn.getInputStream()));
//
//			String inputLine;
//
//			while ((inputLine = br.readLine()) != null) {
//				sb.append(inputLine);
//			}
//
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		return sb.toString();
//	}
	
	private String getAppData() {
		Document doc;
		try {
	 
			// need http protocol
			doc = Jsoup.connect("http://google.com").get();
	 
			// get page title
			String title = doc.title();
			System.out.println("title : " + title);
	 
			// get all links
			Elements links = doc.select("a[href]");
			for (Element link : links) {
	 
				// get the value from href attribute
				System.out.println("\nlink : " + link.attr("href"));
				System.out.println("text : " + link.text());
	 
			}
	 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

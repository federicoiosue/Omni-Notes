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
package it.feio.android.omninotes.models;

import it.feio.android.omninotes.R;
import it.feio.android.omninotes.utils.Constants;

import java.util.Arrays;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.neopixl.pixlui.components.textview.TextView;

public class NavigationDrawerAdapter extends BaseAdapter {

	private Context mContext;
	private Object[] mTitle;
	private TypedArray mIcon;
	private LayoutInflater inflater;

	public NavigationDrawerAdapter(Context context, Object[] title, TypedArray icon) {
		this.mContext = context;
		this.mTitle = title;
		this.mIcon = icon;
	}

	@Override
	public int getCount() {
		return mTitle.length;
	}

	@Override
	public Object getItem(int position) {
		return mTitle[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// Declare Variables
		TextView txtTitle;
		ImageView imgIcon;

		inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.drawer_list_item, parent, false);

		// Locate the TextViews in drawer_list_item.xml
		txtTitle = (TextView) itemView.findViewById(R.id.title);

		// Locate the ImageView in drawer_list_item.xml
		imgIcon = (ImageView) itemView.findViewById(R.id.icon);

		// Set the results into TextViews	
		txtTitle.setText(mTitle[position].toString());
		
		if (isSelected(parent, position)) {
			txtTitle.setTextColor(mContext.getResources().getColor(
					R.color.drawer_text_selected));
		}

		// Set the results into ImageView checking if an icon is present before
		if (mIcon != null && mIcon.length() >= position) {
			int imgRes = mIcon.getResourceId(position, 0);
			imgIcon.setImageResource(imgRes);
		}

		return itemView;
	}

	
	private boolean isSelected(ViewGroup parent, int position) {
		
		// Getting actual navigation selection
		String[] navigationListCodes = mContext.getResources().getStringArray(R.array.navigation_list_codes);
		String navigation = PreferenceManager.getDefaultSharedPreferences(mContext).getString(Constants.PREF_NAVIGATION, navigationListCodes[0]);
		
		// Finding selected item from standard navigation items or tags
		int index = Arrays.asList(navigationListCodes).indexOf(navigation);
		
		if (index == -1) 
			return false;
		
		String navigationLocalized = mContext.getResources().getStringArray(R.array.navigation_list)[index];
		
		// Check the selected one
		Object itemSelected = mTitle[position];
		String title= itemSelected.toString();			
		
		if (navigationLocalized.equals(title)) {
			return true;
		} else {
			return false;
		}			
	}

}

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

import it.feio.android.checklistview.utils.DensityUtil;
import it.feio.android.omninotes.ListActivity;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.utils.Constants;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.neopixl.pixlui.components.textview.TextView;

public class NavDrawerTagAdapter extends BaseAdapter {

	private Activity mActivity;
	private int layout;
	private ArrayList<Tag> tags;
	private LayoutInflater inflater;

	public NavDrawerTagAdapter(Activity mActivity, ArrayList<Tag> tags) {
		this(mActivity, tags, null);		
	}

	public NavDrawerTagAdapter(Activity mActivity, ArrayList<Tag> tags, String navigationTmp) {
		this.mActivity = mActivity;
		this.layout = R.layout.drawer_list_item;		
		this.tags = tags;	
		inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);		
	}

//	public NavDrawerTagAdapter(Context context, int layout, ArrayList<Tag> tags) {
//		this.mActivity = context;
//		this.layout = layout;
//		this.tags = tags;
//		inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//	}

	@Override
	public int getCount() {
		return tags.size();
	}

	@Override
	public Object getItem(int position) {
		return tags.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		
		// Finds elements
		Tag tag = tags.get(position);
		
		// Declare Variables
		TextView txtTitle;
		ImageView imgIcon;

		View itemView = inflater.inflate(layout, parent, false);

		// Locate the TextViews in drawer_list_item.xml
		txtTitle = (TextView) itemView.findViewById(R.id.title);

		// Locate the ImageView in drawer_list_item.xml
		imgIcon = (ImageView) itemView.findViewById(R.id.icon);

		// Set the results into TextViews	
		txtTitle.setText(tag.getName());
		
		if (isSelected(parent, position)) {
			txtTitle.setTextColor(mActivity.getResources().getColor(
					R.color.drawer_text_selected));
		}

		// Set the results into ImageView checking if an icon is present before
		if (tag.getColor() != null && tag.getColor().length() > 0) {
			Drawable img = mActivity.getResources().getDrawable(R.drawable.square);
			ColorFilter cf = new LightingColorFilter(Color.parseColor("#000000"), Integer.parseInt(tag.getColor()));
			// Before API 16 the object is mutable yet
			if (Build.VERSION.SDK_INT >= 16) {
				img.mutate().setColorFilter(cf);
			} else {
				img.setColorFilter(cf);				
			}
			imgIcon.setImageDrawable(img);
			imgIcon.setPadding(	DensityUtil.convertDpToPixel(22, mActivity), //10
								DensityUtil.convertDpToPixel(7, mActivity),//25
								DensityUtil.convertDpToPixel(1, mActivity),//-30
								DensityUtil.convertDpToPixel(7, mActivity));//25
		}

		return itemView;
	}

	
	
	private boolean isSelected(ViewGroup parent, int position) {	
		
		// Getting actual navigation selection
		String[] navigationListCodes = mActivity.getResources().getStringArray(
				R.array.navigation_list_codes);
		
		// Managing temporary navigation indicator when coming from a widget
		String navigationTmp = ListActivity.class.isAssignableFrom(mActivity
				.getClass()) ? ((ListActivity) mActivity).getNavigationTmp()
				: null;
				
		String navigation = navigationTmp != null ? navigationTmp
				: PreferenceManager.getDefaultSharedPreferences(mActivity)
						.getString(Constants.PREF_NAVIGATION,
								navigationListCodes[0]);
		
		if (navigation.equals(String.valueOf(tags.get(position).getId()))) {
			return true;
		} else {
			return false;
		}			
	}

}

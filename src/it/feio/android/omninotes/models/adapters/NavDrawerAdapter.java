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
package it.feio.android.omninotes.models.adapters;

import it.feio.android.omninotes.BaseActivity;
import it.feio.android.omninotes.ListFragment;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.Fonts;

import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.neopixl.pixlui.components.textview.TextView;

public class NavDrawerAdapter extends BaseAdapter {

	private Activity mActivity;
	private Object[] mTitle;
	private TypedArray mIcon;
	private LayoutInflater inflater;

	public NavDrawerAdapter(Activity mActivity, Object[] title, TypedArray icon) {
		this.mActivity = mActivity;
		this.mTitle = title;
		this.mIcon = icon;
		inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		NoteDrawerAdapterViewHolder holder;
	    if (convertView == null) {
	    	convertView = inflater.inflate(R.layout.drawer_list_item, parent, false);

			// Overrides font sizes with the one selected from user
			Fonts.overrideTextSize(mActivity, mActivity.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS), convertView);
	    	
	    	holder = new NoteDrawerAdapterViewHolder();
    		    	
	    	holder.imgIcon = (ImageView) convertView.findViewById(R.id.icon);
	    	holder.txtTitle = (TextView) convertView.findViewById(R.id.title);
	    	convertView.setTag(holder);
	    } else {
	        holder = (NoteDrawerAdapterViewHolder) convertView.getTag();
	    }

		// Set the results into TextViews	
	    holder.txtTitle.setText(mTitle[position].toString());
		
		if (isSelected(parent, position)) {
//			holder.txtTitle.setTextColor(mActivity.getResources().getColor(
//					R.color.drawer_text_selected));
			holder.txtTitle.setTypeface(null,Typeface.BOLD);
		} else {
//			holder.txtTitle.setTextColor(mActivity.getResources().getColor(
//					R.color.actionbar_title_text));
			holder.txtTitle.setTypeface(null,Typeface.NORMAL);
		}

		// Set the results into ImageView checking if an icon is present before
		if (mIcon != null && mIcon.length() >= position) {
			int imgRes = mIcon.getResourceId(position, 0);
			holder.imgIcon.setImageResource(imgRes);
		}

		return convertView;
	}

	
	private boolean isSelected(ViewGroup parent, int position) {
		
		// Getting actual navigation selection
		String[] navigationListCodes =mActivity.getResources().getStringArray(
				R.array.navigation_list_codes);
		
		// Managing temporary navigation indicator when coming from a widget
		String navigationTmp = ListFragment.class.isAssignableFrom(mActivity
				.getClass()) ? ((BaseActivity) mActivity).getNavigationTmp()
				: null;
				
		String navigation = navigationTmp != null ? navigationTmp
				: mActivity.getSharedPreferences(Constants.PREFS_NAME, Activity.MODE_MULTI_PROCESS)
						.getString(Constants.PREF_NAVIGATION,
								navigationListCodes[0]);

		// Finding selected item from standard navigation items or tags
		int index = Arrays.asList(navigationListCodes).indexOf(navigation);
		
		if (index == -1) 
			return false;
		
		String navigationLocalized = mActivity.getResources().getStringArray(R.array.navigation_list)[index];
		
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



/**
 * Holder object
 * @author fede
 *
 */
class NoteDrawerAdapterViewHolder {	
	ImageView imgIcon;
	TextView txtTitle;
}

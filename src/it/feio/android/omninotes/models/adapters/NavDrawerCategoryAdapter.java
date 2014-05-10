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
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.Fonts;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.neopixl.pixlui.components.textview.TextView;

public class NavDrawerCategoryAdapter extends BaseAdapter {

	private Activity mActivity;
	private int layout;
	private ArrayList<Category> tags;
	private LayoutInflater inflater;

	public NavDrawerCategoryAdapter(Activity mActivity, ArrayList<Category> tags) {
		this(mActivity, tags, null);		
	}

	public NavDrawerCategoryAdapter(Activity mActivity, ArrayList<Category> tags, String navigationTmp) {
		this.mActivity = mActivity;
		this.layout = R.layout.drawer_list_item;		
		this.tags = tags;	
		inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);		
	}

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
		Category tag = tags.get(position);
		
		NoteDrawerCategoryAdapterViewHolder holder;
	    if (convertView == null) {
	    	convertView = inflater.inflate(layout, parent, false);

			// Overrides font sizes with the one selected from user
			Fonts.overrideTextSize(mActivity, mActivity.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS), convertView);
	    	
	    	holder = new NoteDrawerCategoryAdapterViewHolder();
    		    	
	    	holder.imgIcon = (ImageView) convertView.findViewById(R.id.icon);
	    	holder.txtTitle = (TextView) convertView.findViewById(R.id.title);
	    	convertView.setTag(holder);
	    } else {
	        holder = (NoteDrawerCategoryAdapterViewHolder) convertView.getTag();
	    }
	
		// Set the results into TextViews	
	    holder.txtTitle.setText(tag.getName());
		
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
		if (tag.getColor() != null && tag.getColor().length() > 0) {
			Drawable img = mActivity.getResources().getDrawable(R.drawable.square);
			ColorFilter cf = new LightingColorFilter(Color.parseColor("#000000"), Integer.parseInt(tag.getColor()));
			// Before API 16 the object is mutable yet
			if (Build.VERSION.SDK_INT >= 16) {
				img.mutate().setColorFilter(cf);
			} else {
				img.setColorFilter(cf);				
			}
			holder.imgIcon.setImageDrawable(img);
			holder.imgIcon.setPadding(8,8,8,8);
		}

		return convertView;
	}

	
	
	private boolean isSelected(ViewGroup parent, int position) {	
		
		// Getting actual navigation selection
		String[] navigationListCodes = mActivity.getResources().getStringArray(
				R.array.navigation_list_codes);
		
		// Managing temporary navigation indicator when coming from a widget
		String navigationTmp = ListFragment.class.isAssignableFrom(mActivity
				.getClass()) ? ((BaseActivity) mActivity).getNavigationTmp()
				: null;
				
		String navigation = navigationTmp != null ? navigationTmp
				: mActivity.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS)
						.getString(Constants.PREF_NAVIGATION,
								navigationListCodes[0]);
		
		if (navigation.equals(String.valueOf(tags.get(position).getId()))) {
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
class NoteDrawerCategoryAdapterViewHolder {	
	ImageView imgIcon;
	TextView txtTitle;
}

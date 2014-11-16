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

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.neopixl.pixlui.components.textview.TextView;

import java.util.ArrayList;

import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.ImageAndTextItem;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.Fonts;

public class ImageAndTextAdapter extends BaseAdapter {

	private Activity mActivity;
	ArrayList<ImageAndTextItem> items;
	private LayoutInflater inflater;

	public ImageAndTextAdapter(Activity mActivity,
			ArrayList<ImageAndTextItem> items) {
		this.mActivity = mActivity;
		this.items = items;
		inflater = (LayoutInflater) mActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ImageAndTextViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.image_and_text_item,
					parent, false);

			// Overrides font sizes with the one selected from user
			Fonts.overrideTextSize(mActivity, mActivity.getSharedPreferences(
					Constants.PREFS_NAME, mActivity.MODE_MULTI_PROCESS),
					convertView);

			holder = new ImageAndTextViewHolder();

			holder.image = (ImageView) convertView.findViewById(R.id.image);
			holder.text = (TextView) convertView.findViewById(R.id.text);
			convertView.setTag(holder);
		} else {
			holder = (ImageAndTextViewHolder) convertView.getTag();
		}

		// Set the results into TextViews
		holder.text.setText(items.get(position).getText());

		// Set the results into ImageView checking if an icon is present before
		if (items.get(position).getImage() != 0) {
			holder.image.setImageResource(items.get(position).getImage());
		}

		return convertView;
	}

}

/**
 * Holder object
 * 
 * @author fede
 * 
 */
class ImageAndTextViewHolder {
	ImageView image;
	TextView text;
}

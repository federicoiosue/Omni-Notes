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
import it.feio.android.omninotes.models.holders.ImageAndTextItem;
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
                            Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS),
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
 */
class ImageAndTextViewHolder {

    ImageView image;
    TextView text;
}

/*
 * Copyright (C) 2013-2020 Federico Iosue (federico@iosue.it)
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

import static it.feio.android.omninotes.utils.Constants.PREFS_NAME;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_NAVIGATION;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.neopixl.pixlui.components.textview.TextView;
import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.NavigationItem;
import java.util.Arrays;
import java.util.List;


public class NavDrawerAdapter extends BaseAdapter {

  private Activity mActivity;
  private List<NavigationItem> items;
  private LayoutInflater inflater;


  public NavDrawerAdapter (Activity mActivity, List<NavigationItem> items) {
    this.mActivity = mActivity;
    this.items = items;
    inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }


  @Override
  public int getCount () {
    return items.size();
  }


  @Override
  public Object getItem (int position) {
    return items.get(position);
  }


  @Override
  public long getItemId (int position) {
    return position;
  }


  public View getView (int position, View convertView, ViewGroup parent) {
    NoteDrawerAdapterViewHolder holder;
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.drawer_list_item, parent, false);

      holder = new NoteDrawerAdapterViewHolder();

      holder.imgIcon = convertView.findViewById(R.id.icon);
      holder.txtTitle = convertView.findViewById(R.id.title);
      convertView.setTag(holder);
    } else {
      holder = (NoteDrawerAdapterViewHolder) convertView.getTag();
    }

    // Set the results into TextViews
    holder.txtTitle.setText(items.get(position).getText());

    if (isSelected(position)) {
      holder.imgIcon.setImageResource(items.get(position).getIconSelected());
      holder.txtTitle.setTypeface(null, Typeface.BOLD);
      int color = mActivity.getResources().getColor(R.color.colorPrimaryDark);
      holder.txtTitle.setTextColor(color);
      holder.imgIcon.getDrawable().mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    } else {
      holder.imgIcon.setImageResource(items.get(position).getIcon());
      holder.txtTitle.setTypeface(null, Typeface.NORMAL);
      holder.txtTitle.setTextColor(mActivity.getResources().getColor(R.color.drawer_text));
    }

    return convertView;
  }


  private boolean isSelected (int position) {

    // Getting actual navigation selection
    String[] navigationListCodes = mActivity.getResources().getStringArray(R.array.navigation_list_codes);

    // Managing temporary navigation indicator when coming from a widget
    String navigationTmp = MainActivity.class.isAssignableFrom(mActivity.getClass()) ? ((MainActivity) mActivity)
        .getNavigationTmp() : null;

    String navigation = navigationTmp != null ? navigationTmp
        : mActivity.getSharedPreferences(PREFS_NAME, Activity.MODE_MULTI_PROCESS)
                   .getString(PREF_NAVIGATION, navigationListCodes[0]);

    // Finding selected item from standard navigation items or tags
    int index = Arrays.asList(navigationListCodes).indexOf(navigation);

    if (index == -1) {
      return false;
    }

    String navigationLocalized = mActivity.getResources().getStringArray(R.array.navigation_list)[index];
    return navigationLocalized.equals(items.get(position).getText());
  }

}


/**
 * Holder object
 *
 * @author fede
 */
class NoteDrawerAdapterViewHolder {

  ImageView imgIcon;
  TextView txtTitle;
}

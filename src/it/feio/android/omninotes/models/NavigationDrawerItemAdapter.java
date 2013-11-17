package it.feio.android.omninotes.models;

import it.feio.android.omninotes.R;
import com.neopixl.pixlui.components.textview.TextView;
import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

public class NavigationDrawerItemAdapter extends BaseAdapter {

	Context context;
	String[] mTitle;
	TypedArray mIcon;
	LayoutInflater inflater;

	public NavigationDrawerItemAdapter(Context context, String[] title, TypedArray icon) {
		this.context = context;
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

		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.drawer_list_item, parent, false);
		boolean checked = ((ListView)parent).isItemChecked(position);

		// Locate the TextViews in drawer_list_item.xml
		txtTitle = (TextView) itemView.findViewById(R.id.title);

		// Locate the ImageView in drawer_list_item.xml
		imgIcon = (ImageView) itemView.findViewById(R.id.icon);

		// Set the results into TextViews
		txtTitle.setText(mTitle[position]);
		if (convertView != null && checked)
			txtTitle.setTextAppearance(context, R.style.Drawer_Selected);

		// Set the results into ImageView
		imgIcon.setImageResource(mIcon.getResourceId(position, 0));

		return itemView;
	}

}
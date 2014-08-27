package it.feio.android.omninotes.models.adapters;

import it.feio.android.omninotes.R;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class PaletteColorPickerAdapter extends BaseAdapter {

	private Context context;
	// list which holds the colors to be displayed
	private List<Integer> colorList = new ArrayList<Integer>();
	// width of grid column
	int colorGridColumnWidth;

	public PaletteColorPickerAdapter(Context context, String colors[][]) {
		this.context = context;

		// defines the width of each color square
		colorGridColumnWidth = context.getResources().getInteger(R.integer.color_grid_column_width);

		colorList = new ArrayList<Integer>();

		// add the color array to the list
		for (int i = 0; i < colors.length; i++) {
			for (int j = 0; j < colors[i].length; j++) {
				colorList.add(Color.parseColor("#" + colors[i][j]));
			}
		}
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;

		if (convertView == null) {
			imageView = new ImageView(context);
			imageView.setLayoutParams(new GridView.LayoutParams(colorGridColumnWidth, colorGridColumnWidth));
		} else {
			imageView = (ImageView) convertView;
		}

		imageView.setBackgroundColor(colorList.get(position));
		imageView.setId(position);

		return imageView;
	}

	public int getCount() {
		return colorList.size();
	}

	public Object getItem(int position) {
		return colorList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
}

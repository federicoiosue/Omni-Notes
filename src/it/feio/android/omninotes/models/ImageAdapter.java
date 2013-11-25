package it.feio.android.omninotes.models;

import java.util.ArrayList;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
	private Context mContext;

	// references to our images
//	private Integer[] mThumbIds = { R.drawable.sample_2, R.drawable.sample_3,
//			R.drawable.sample_4, R.drawable.sample_5, R.drawable.sample_6,
//			R.drawable.sample_7, R.drawable.sample_0, R.drawable.sample_1,
//			R.drawable.sample_2, R.drawable.sample_3, R.drawable.sample_4,
//			R.drawable.sample_5, R.drawable.sample_6, R.drawable.sample_7,
//			R.drawable.sample_0, R.drawable.sample_1, R.drawable.sample_2,
//			R.drawable.sample_3, R.drawable.sample_4, R.drawable.sample_5,
//			R.drawable.sample_6, R.drawable.sample_7 };
	private String[] urls = { R.drawable.sample_2, R.drawable.sample_3,
			R.drawable.sample_4, R.drawable.sample_5, R.drawable.sample_6,
			R.drawable.sample_7, R.drawable.sample_0, R.drawable.sample_1,
			R.drawable.sample_2, R.drawable.sample_3, R.drawable.sample_4,
			R.drawable.sample_5, R.drawable.sample_6, R.drawable.sample_7,
			R.drawable.sample_0, R.drawable.sample_1, R.drawable.sample_2,
			R.drawable.sample_3, R.drawable.sample_4, R.drawable.sample_5,
			R.drawable.sample_6, R.drawable.sample_7 };

	public ImageAdapter(Context mContext, ArrayList<Uri>) {
		this.mContext = mContext;
	}

	public int getCount() {
		return urls.length;
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	// create a new ImageView for each item referenced by the Adapter
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) { // if it's not recycled, initialize some
									// attributes
			imageView = new ImageView(mContext);
			imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(8, 8, 8, 8);
		} else {
			imageView = (ImageView) convertView;
		}

//		imageView.setImageResource(mThumbIds[position]);
		imageView.setImageBitmap(bm)(mThumbIds[position]);
		return imageView;
	}
}

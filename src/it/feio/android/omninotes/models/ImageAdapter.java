package it.feio.android.omninotes.models;

import it.feio.android.omninotes.utils.BitmapDecoder;
import it.feio.android.omninotes.utils.Constants;

import java.io.FileNotFoundException;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
	private Context mContext;
	private List<Uri> imageList;

	public ImageAdapter(Context mContext, List<Uri> imageList) {
		this.mContext = mContext;
		this.imageList = imageList;
	}

	public int getCount() {
		return imageList.size();
	}

	public Uri getItem(int position) {
		return imageList.get(position);
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
//			imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		} else {
			imageView = (ImageView) convertView;
		}

		try {
			imageView.setImageBitmap(BitmapDecoder.decodeSampledFromUri(mContext, imageList.get(position), Constants.THUMBNAIL_SIZE, Constants.THUMBNAIL_SIZE));
		} catch (FileNotFoundException e) {
			Log.e(Constants.TAG, "Image not found");
		}
		return imageView;
	}
}

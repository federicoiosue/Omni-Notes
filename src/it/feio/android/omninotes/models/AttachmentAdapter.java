package it.feio.android.omninotes.models;

import it.feio.android.omninotes.utils.BitmapDecoder;
import it.feio.android.omninotes.utils.Constants;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class AttachmentAdapter extends BaseAdapter {
	private Context mContext;
	private List<Attachment> attachmentsList = new ArrayList<Attachment>();

	public AttachmentAdapter(Context mContext, List<Attachment> attachmentsList) {
		this.mContext = mContext;
		this.attachmentsList = attachmentsList;
	}

	public int getCount() {
		return attachmentsList.size();
	}

	public Attachment getItem(int position) {
		return attachmentsList.get(position);
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
			imageView.setImageBitmap(BitmapDecoder.decodeSampledFromUri(mContext, attachmentsList.get(position).getUri(), Constants.THUMBNAIL_SIZE, Constants.THUMBNAIL_SIZE));
		} catch (FileNotFoundException e) {
			Log.e(Constants.TAG, "Image not found");
		}
		return imageView;
	}
}

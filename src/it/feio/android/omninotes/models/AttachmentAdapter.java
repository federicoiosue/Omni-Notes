/*******************************************************************************
 * Copyright 2013 Federico Iosue (federico.iosue@gmail.com)
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
package it.feio.android.omninotes.models;

import it.feio.android.omninotes.R;
import it.feio.android.omninotes.async.BitmapLoaderTask;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		} else {
			imageView = (ImageView) convertView;
		}

		// try {
		// imageView.setImageBitmap(BitmapDecoder.decodeSampledFromUri(mContext,
		// attachmentsList.get(position).getUri(), Constants.THUMBNAIL_SIZE,
		// Constants.THUMBNAIL_SIZE));
		// } catch (FileNotFoundException e) {
		// Log.e(Constants.TAG, "Image not found");
		// }

//		Bitmap b = BitmapFactory.decodeResource(mContext.getResources(),
//				R.drawable.white);
//		imageView.setImageBitmap(b);
		BitmapLoaderTask task = new BitmapLoaderTask(mContext, imageView);
		task.execute(attachmentsList.get(position).getUri());
		return imageView;
	}
}

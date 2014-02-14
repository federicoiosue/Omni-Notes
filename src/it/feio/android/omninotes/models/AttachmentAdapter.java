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
package it.feio.android.omninotes.models;

import it.feio.android.omninotes.R;
import it.feio.android.omninotes.async.ListThumbnailLoaderTask;
import it.feio.android.omninotes.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class AttachmentAdapter extends BaseAdapter {
	
	private final int THUMBNAIL_SIZE;
	
	private Activity mActivity;
	private List<Attachment> attachmentsList = new ArrayList<Attachment>();

	public AttachmentAdapter(Activity mActivity, List<Attachment> attachmentsList, int thumbnailSize) {
		this.mActivity = mActivity;
		this.attachmentsList = attachmentsList;
		THUMBNAIL_SIZE = thumbnailSize;
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
		
		Log.v(Constants.TAG, "GridView called for position " + position);
		
		ImageView imageView;
		if (convertView == null) { // if it's not recycled, initialize some
									// attributes
			imageView = new ImageView(mActivity);
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			// A placeholder is set here
			imageView.setImageBitmap(ThumbnailUtils.extractThumbnail(
					BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.image_placeholder),
					THUMBNAIL_SIZE, THUMBNAIL_SIZE));

		} else {
			imageView = (ImageView) convertView;
		}
	
		Attachment attachment = attachmentsList.get(position);
			
//		BitmapLoaderTask task = new BitmapLoaderTask(mActivity, imageView);
		ListThumbnailLoaderTask task = new ListThumbnailLoaderTask(mActivity, imageView, THUMBNAIL_SIZE);
		task.execute(attachment);
		
		return imageView;
	}
}

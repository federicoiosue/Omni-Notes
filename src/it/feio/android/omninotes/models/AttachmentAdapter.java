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
import it.feio.android.omninotes.async.ThumbnailLoaderTask;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.date.DateHelper;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AttachmentAdapter extends BaseAdapter {
		
	private Activity mActivity;
	private List<Attachment> attachmentsList = new ArrayList<Attachment>();
	private ExpandableHeightGridView mGridView;
	private LayoutInflater inflater;

	public AttachmentAdapter(Activity mActivity, List<Attachment> attachmentsList, ExpandableHeightGridView mGridView) {
		this.mActivity = mActivity;
		this.attachmentsList = attachmentsList;
		this.mGridView = mGridView;
		this.inflater = (LayoutInflater) mActivity.getSystemService(mActivity.LAYOUT_INFLATER_SERVICE);
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

	
//	// create a new ImageView for each item referenced by the Adapter
//	@SuppressLint("NewApi")
//	public View getView(int position, View convertView, ViewGroup parent) {
//		
//		Log.v(Constants.TAG, "GridView called for position " + position);
//		
//		SquareImageView imageView;
//		if (convertView == null) {// not recylcled
//			imageView = new SquareImageView(mActivity);
//		} else {
//			imageView = (SquareImageView) convertView;
//		}	
//		
////		ThumbnailLoaderTask task = new ThumbnailLoaderTask(mActivity, imageView, mGridView.getItemHeight(), mGridView.getItemHeight());
//		ThumbnailLoaderTask task = new ThumbnailLoaderTask(mActivity, imageView, Constants.THUMBNAIL_SIZE, Constants.THUMBNAIL_SIZE);
//		if (Build.VERSION.SDK_INT >= 11) {
//			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, attachmentsList.get(position));
//		} else {
//			task.execute(attachmentsList.get(position));
//		}
//		
//		return imageView;
//	}
	// create a new ImageView for each item referenced by the Adapter
	
	
	@SuppressLint("NewApi")
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Log.v(Constants.TAG, "GridView called for position " + position);
		
		Attachment mAttachment = attachmentsList.get(position);
		
		AttachmentHolder holder;
		if (convertView == null) {
			holder = new AttachmentHolder();
	    	convertView = inflater.inflate(R.layout.gridview_item, parent, false);
			holder.image = (SquareImageView) convertView.findViewById(R.id.gridview_item_picture);
			holder.text = (TextView) convertView.findViewById(R.id.gridview_item_text);
			convertView.setTag(holder);
		} else {
	        holder = (AttachmentHolder) convertView.getTag();
		}	
		
		// Draw name in case the type is an audio recording (or file in the future)
		if (mAttachment.getMime_type().equals(Constants.MIME_TYPE_AUDIO)) {
			String text = "";
			text = DateHelper.getLocalizedDateTime(mActivity, mAttachment
					.getUri().getLastPathSegment().split("\\.")[0],
					Constants.DATE_FORMAT_SORTABLE);
		
			if (text == null) {
				text = mActivity.getString(R.string.attachment);
			}
			holder.text.setText(text);
			holder.text.setVisibility(View.VISIBLE);
		}
		
		// Starts the AsyncTask to draw bitmap into ImageView
		ThumbnailLoaderTask task = new ThumbnailLoaderTask(mActivity, holder.image, Constants.THUMBNAIL_SIZE, Constants.THUMBNAIL_SIZE);
		if (Build.VERSION.SDK_INT >= 11) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mAttachment);
		} else {
			task.execute(mAttachment);
		}
		
		return convertView;
	}
 
    public class AttachmentHolder
    {
        TextView text;
        SquareImageView image;
    }
}

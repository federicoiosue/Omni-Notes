/*******************************************************************************
tas * Copyright 2014 Federico Iosue (federico.iosue@gmail.com)
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
package it.feio.android.omninotes.models.adapters;

import it.feio.android.omninotes.R;
import it.feio.android.omninotes.async.BitmapWorkerTask;
import it.feio.android.omninotes.async.TextWorkerTask;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.views.SquareImageView;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.Fonts;
import it.feio.android.omninotes.utils.TextHelper;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Spanned;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class NoteAdapter extends ArrayAdapter<Note> {

	private final Activity mActivity;
	private final List<Note> notes;
	private SparseBooleanArray selectedItems = new SparseBooleanArray();
	private boolean expandedView;
	private int layout;
	private LayoutInflater inflater;
	

	public NoteAdapter(Activity activity, int layout, List<Note> notes) {
		super(activity, R.layout.note_layout_expanded, notes);
		this.mActivity = activity;
		this.notes = notes;		
		this.layout = layout;
		
		expandedView = layout == R.layout.note_layout_expanded;
		inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@SuppressLint("NewApi")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Note note = notes.get(position);
		
		NoteAdapterViewHolder holder;
	    if (convertView == null) {
	    	convertView = inflater.inflate(layout, parent, false);

			// Overrides font sizes with the one selected from user
			Fonts.overrideTextSize(mActivity, mActivity.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS), convertView);
	    	
	    	holder = new NoteAdapterViewHolder();
    		    	
	    	holder.root = convertView.findViewById(R.id.root);
	    	holder.cardLayout = convertView.findViewById(R.id.card_layout);
	    	holder.categoryMarker = convertView.findViewById(R.id.category_marker);

	    	holder.title = (TextView) convertView.findViewById(R.id.note_title);
	    	holder.content = (TextView) convertView.findViewById(R.id.note_content);
	    	holder.date = (TextView) convertView.findViewById(R.id.note_date);

	    	holder.archiveIcon = (ImageView) convertView.findViewById(R.id.archivedIcon);
//	    	holder.trashIcon = (ImageView) convertView.findViewById(R.id.trashedIcon);
	    	holder.locationIcon = (ImageView) convertView.findViewById(R.id.locationIcon);
	    	holder.alarmIcon = (ImageView) convertView.findViewById(R.id.alarmIcon);
	    	holder.lockedIcon = (ImageView) convertView.findViewById(R.id.lockedIcon);
	    	if (!expandedView)
	    		holder.attachmentIcon = (ImageView) convertView.findViewById(R.id.attachmentIcon);

	    	holder.attachmentThumbnail = (SquareImageView) convertView.findViewById(R.id.attachmentThumbnail);	    	
	    	
	    	convertView.setTag(holder);
			
	    } else {
	        holder = (NoteAdapterViewHolder) convertView.getTag();
	    }
	    
		try {
//			if (note.isChecklist()) {
				TextWorkerTask task = new TextWorkerTask(mActivity, holder.title, holder.content, expandedView);
				if (Build.VERSION.SDK_INT >= 11) {
					task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, note);
				} else {
					task.execute(note);
				}
//			} else {
//				Spanned[] titleAndContent = TextHelper.parseTitleAndContent(mActivity, note);
//				holder.title.setText(titleAndContent[0]);
//				holder.content.setText(titleAndContent[1]);
//			}
		} catch (RejectedExecutionException e) {
			Log.w(Constants.TAG, "Oversized tasks pool to load texts!");
		}


		// Evaluates the archived state...
		holder.archiveIcon.setVisibility(note.isArchived() ? View.VISIBLE : View.GONE);
		// .. the trashed state
//		holder.trashIcon.setVisibility(note.isTrashed() ? View.VISIBLE : View.GONE);
		// ...the location
		holder.locationIcon.setVisibility(note.getLongitude() != null && note.getLongitude() != 0 ? View.VISIBLE : View.GONE);
		// ...the presence of an alarm
		holder.alarmIcon.setVisibility(note.getAlarm() != null ? View.VISIBLE : View.GONE);
		// ...the locked with password state	
		holder.lockedIcon.setVisibility(note.isLocked() ? View.VISIBLE : View.GONE);
		// ...the attachment icon for contracted view
		if (!expandedView) {
			holder.attachmentIcon.setVisibility(note.getAttachmentsList().size() > 0 ? View.VISIBLE : View.GONE);
		}
				
		
		String dateText = getDateText(mActivity, note);
		holder.date.setText(dateText);
		
		
		// Highlighted if is part of multiselection of notes. Remember to search for child with card ui
		if (selectedItems.get(position)) {
			holder.cardLayout.setBackgroundColor(mActivity.getResources().getColor(
					R.color.list_bg_selected));
		} else {
			restoreDrawable(note, holder.cardLayout, holder);
		}
		

		// Attachment thumbnail
		if (expandedView) {
			// If note is locked or without attachments nothing is shown
			if ((note.isLocked() && !mActivity.getSharedPreferences(Constants.PREFS_NAME, mActivity.MODE_MULTI_PROCESS).getBoolean("settings_password_access", false))
					|| note.getAttachmentsList().size() == 0) {
				holder.attachmentThumbnail.setImageResource(0);
				holder.attachmentThumbnail.setVisibility(View.GONE);
			}
			// Otherwise...
			else {
				Attachment mAttachment = note.getAttachmentsList().get(0);
				loadThumbnail(holder, mAttachment);
			}
		}

//		Animation animation = AnimationUtils.loadAnimation(mActivity, R.animator.fade_in);
//		animation.setDuration(60);
//		convertView.startAnimation(animation);
		
		return convertView;
	}

	
	
	/**
	 * Choosing which date must be shown depending on sorting criteria
	 * @return String ith formatted date
	 */
	public static String getDateText(Context mContext, Note note) {
		String dateText;
		SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFS_NAME, mContext.MODE_MULTI_PROCESS);
		String sort_column = prefs.getString(Constants.PREF_SORTING_COLUMN, "");
		
		// Creation
		if (sort_column.equals(DbHelper.KEY_CREATION)) {
			dateText = mContext.getString(R.string.creation) + " " + note.getCreationShort(mContext);
		}
		// Reminder
		else if (sort_column.equals(DbHelper.KEY_ALARM)) {
			String alarmShort = note.getAlarmShort(mContext);
			
			if (alarmShort.length() == 0) {
				dateText =  mContext.getString(R.string.no_reminder_set);
			} else {
				dateText = mContext.getString(R.string.alarm_set_on) + " "
					+ note.getAlarmShort(mContext);
			}
		}
		// Others
		else {
			dateText = mContext.getString(R.string.last_update) + " "
					+ note.getLastModificationShort(mContext);
		}
		return dateText;
	}

	
	
	
	

	public SparseBooleanArray getSelectedItems() {
		return selectedItems;
	}

	public void addSelectedItem(Integer selectedItem) {
		this.selectedItems.put(selectedItem, true);
	}

	public void removeSelectedItem(Integer selectedItem) {
		this.selectedItems.delete(selectedItem);
	}

	public void clearSelectedItems() {
		this.selectedItems.clear();
	}	

	
	public void restoreDrawable(Note note, View v) {
		restoreDrawable(note, v, null);
	}
	
	
	public void restoreDrawable(Note note, View v, NoteAdapterViewHolder holder) {
		final int paddingBottom = v.getPaddingBottom(), paddingLeft = v.getPaddingLeft();
	    final int paddingRight = v.getPaddingRight(), paddingTop = v.getPaddingTop();
	    v.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
	    colorNote(note, v, holder);
	}
	

	@SuppressWarnings("unused")
	private void colorNote(Note note, View v) {
		colorNote(note, v, null);
	}

	/**
	 * Color of category marker if note is categorized a function is active in preferences
	 * @param note
	 * @param rowView
	 */
	private void colorNote(Note note, View v, NoteAdapterViewHolder holder) {

		String colorsPref = mActivity.getSharedPreferences(Constants.PREFS_NAME, mActivity.MODE_MULTI_PROCESS)
									.getString("settings_colors_app", Constants.PREF_COLORS_APP_DEFAULT);
		
		// Checking preference
		if (!colorsPref.equals("disabled")) {

			// Resetting transparent color to the view
			v.setBackgroundColor(Color.parseColor("#00000000"));

			// If category is set the color will be applied on the appropriate target
			if (note.getCategory() != null && note.getCategory().getColor() != null) {
				if (colorsPref.equals("complete") || colorsPref.equals("list")) {
					v.setBackgroundColor(Integer.parseInt(note.getCategory().getColor()));
				} else {
					if (holder != null) {
						holder.categoryMarker.setBackgroundColor(Integer.parseInt(note.getCategory().getColor()));
					} else {
						v.findViewById(R.id.category_marker).setBackgroundColor(Integer.parseInt(note.getCategory().getColor()));
					}
				}
			} else {
				v.findViewById(R.id.category_marker).setBackgroundColor(0);
			}
		}
	}
	 
	
	
	@SuppressLint("NewApi")
	private void loadThumbnail(NoteAdapterViewHolder holder, Attachment mAttachment) {
//		if (isNewWork(mAttachment.getUri(), holder.attachmentThumbnail)) {
			BitmapWorkerTask task = new BitmapWorkerTask(mActivity, holder.attachmentThumbnail,
					Constants.THUMBNAIL_SIZE, Constants.THUMBNAIL_SIZE);
			holder.attachmentThumbnail.setAsyncTask(task);
			try {
				if (Build.VERSION.SDK_INT >= 11) {
					task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mAttachment);
				} else {
					task.execute(mAttachment);
				}
			} catch (RejectedExecutionException e) {
				Log.w(Constants.TAG, "Oversized tasks pool to load thumbnails!");
			}
			holder.attachmentThumbnail.setVisibility(View.VISIBLE);
//		}
	}

	
	public static boolean isNewWork(Uri uri, SquareImageView imageView) {
		final BitmapWorkerTask bitmapWorkerTask = (BitmapWorkerTask)imageView.getAsyncTask();

		if (bitmapWorkerTask != null && bitmapWorkerTask.getAttachment() != null) {
			final Uri bitmapData = bitmapWorkerTask.getAttachment().getUri();
			// If bitmapData is not yet set or it differs from the new data
			if (bitmapData == null || bitmapData != uri) {
				// Cancel previous task
				bitmapWorkerTask.cancel(true);
			} else {
				// The same work is already in progress
				return false;
			}
		}
		// No task associated with the ImageView, or an existing task was
		// cancelled
		return true;
	}
	
	
	/**
	 * Replaces notes
	 */
	public void replace(Note note, int index) {
        if (notes.indexOf(note) != -1) {
            notes.remove(index);
        }  else {
            index = notes.size();
        }
		notes.add(index, note);
	}
}



class NoteAdapterViewHolder {
	
	View root;
	View cardLayout;
	View categoryMarker;
	
	TextView title;
	TextView content;
	TextView date;

	ImageView archiveIcon;
//	ImageView trashIcon;
	ImageView locationIcon;
	ImageView alarmIcon;
	ImageView lockedIcon;
	ImageView attachmentIcon;
	
	SquareImageView attachmentThumbnail;
}
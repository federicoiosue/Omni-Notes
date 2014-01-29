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
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.db.DbHelper;
import java.util.HashMap;
import java.util.List;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class NoteAdapter extends ArrayAdapter<Note> {

	private final Context context;
	private final List<Note> values;
	private HashMap<Integer, Boolean> selectedItems = new HashMap<Integer, Boolean>();

	public NoteAdapter(Context context, List<Note> values) {
		this(context, R.layout.note_layout, values);
	}

	public NoteAdapter(Context context, int layout, List<Note> values) {
		super(context, layout, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Note note = values.get(position);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.note_layout, parent, false);

		TextView title = (TextView) rowView.findViewById(R.id.note_title);
		TextView content = (TextView) rowView.findViewById(R.id.note_content);
		TextView date = (TextView) rowView.findViewById(R.id.note_date);

		// Setting note title		
		title.setText(note.getTitle());
		

		// Setting note content
		String contentText = note.getContent();
		if (contentText.length() > 0) {
			int maxContentTextLength = 40;
			// Long content it cutted after maxContentTextLength chars and three dots are appended as suffix
			String[] noteContent = contentText.split(System.getProperty("line.separator"));
			String suffix = (noteContent[0].length() > maxContentTextLength || noteContent.length > 1) ? " ..."
					: "";
			String contentReducedText = suffix.length() > 0 ? (noteContent[0].length() > maxContentTextLength ? noteContent[0]
					.substring(0, maxContentTextLength) : noteContent[0])
					+ suffix
					: noteContent[0];
	
			content.setText(contentReducedText);
			content.setVisibility(View.VISIBLE);
		}

		// Evaluates the archived state...
		ImageView archiveIcon = (ImageView)rowView.findViewById(R.id.archivedIcon);
		archiveIcon.setVisibility(note.isArchived() ? View.VISIBLE : View.GONE);
		// ... the presence of an alarm
		ImageView alarmIcon = (ImageView)rowView.findViewById(R.id.alarmIcon);
		alarmIcon.setVisibility(note.getAlarm() != null ? View.VISIBLE : View.GONE);
		// ... the locked with password state	
		ImageView lockedIcon = (ImageView)rowView.findViewById(R.id.lockedIcon);
		lockedIcon.setVisibility(note.isLocked() ? View.VISIBLE : View.GONE);
		// ... or attachments to show relative icon indicators	
		ImageView attachmentIcon = (ImageView)rowView.findViewById(R.id.attachmentIcon);
		attachmentIcon.setVisibility(note.getAttachmentsList().size() > 0 ? View.VISIBLE : View.GONE);
		
		// Color of tag marker if note is tagged a function is active in preferences
		if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
				"settings_enable_tag_marker", true)){
			if (note.getTag() != null && note.getTag().getColor() != null) {
				View tagMarker = rowView.findViewById(R.id.tag_marker);
				tagMarker.setBackgroundColor(Integer.parseInt(note.getTag().getColor()));
			}
		}
		
		// Choosing if it must be shown creation date or last modification depending on sorting criteria
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String sort_column = prefs.getString(Constants.PREF_SORTING_COLUMN, "");
		String time_format = prefs.getBoolean("settings_hours_format", true) ? Constants.DATE_FORMAT_SHORT
				: Constants.DATE_FORMAT_SHORT_12;
		if (sort_column.equals(DbHelper.KEY_CREATION))
			date.setText(context.getString(R.string.creation) + " " + note.getCreationShort(time_format));
		else
			date.setText(context.getString(R.string.last_update) + " "
					+ note.getLastModificationShort(time_format));

		// Highlighted if is part of multiselection of notes. Remember to search for child with card ui
		View v = rowView.findViewById(R.id.card_layout);
		if (selectedItems.get(position) != null) {
			v.setBackgroundColor(context.getResources().getColor(
					R.color.list_bg_selected));
		} else {
//			v.setBackgroundColor(context.getResources().getColor(
//					R.color.list_bg));
			restoreDrawable(v);
		}
		
		return rowView;
	}

	public HashMap<Integer, Boolean> getSelectedItems() {
		return selectedItems;
	}

	public void addSelectedItem(Integer selectedItem) {
		this.selectedItems.put(selectedItem, true);
	}

	public void removeSelectedItem(Integer selectedItem) {
		this.selectedItems.remove(selectedItem);
	}

	public void clearSelectedItems() {
		this.selectedItems.clear();
	}
	

	
	public void restoreDrawable(View v) {
		final int paddingBottom = v.getPaddingBottom(), paddingLeft = v.getPaddingLeft();
	    final int paddingRight = v.getPaddingRight(), paddingTop = v.getPaddingTop();
		v.setBackgroundResource(R.drawable.bg_card);
	    v.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
	}


}

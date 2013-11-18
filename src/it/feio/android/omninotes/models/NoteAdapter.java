package it.feio.android.omninotes.models;

import it.feio.android.omninotes.R;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.DbHelper;
import java.util.HashMap;
import java.util.List;
import android.content.Context;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
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
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.note_layout, parent, false);

		TextView title = (TextView) rowView.findViewById(R.id.note_title);
		TextView content = (TextView) rowView.findViewById(R.id.note_content);
		TextView date = (TextView) rowView.findViewById(R.id.note_date);

		// Get text for title and content views
		title.setText(values.get(position).getTitle());
		int maxContentTextLength = 40;
		// Long content it cutted after maxContentTextLength chars and three dots are appended as suffix
		String[] noteContent = values.get(position).getContent().split(System.getProperty("line.separator"));
		String suffix = (noteContent[0].length() > maxContentTextLength || noteContent.length > 1) ? " ..."
				: "";
		String contentText = suffix.length() > 0 ? (noteContent[0].length() > maxContentTextLength ? noteContent[0]
				.substring(0, maxContentTextLength) : noteContent[0])
				+ suffix
				: noteContent[0];

		content.setText(contentText);

		// Choosing if it must be shown creation date or last modification depending on sorting criteria
		String sort_column = PreferenceManager.getDefaultSharedPreferences(context).getString(
				Constants.PREF_SORTING_COLUMN, "");
		if (sort_column.equals(DbHelper.KEY_CREATION))
			date.setText(context.getString(R.string.creation) + " " + values.get(position).getCreationShort());
		else
			date.setText(context.getString(R.string.last_update) + " "
					+ values.get(position).getLastModificationShort());

		// Highlighted if is part of multiselection of notes
		if (selectedItems.get(position) != null) {
			rowView.setBackgroundColor(context.getResources().getColor(
					R.color.list_bg_selected));
		} else {
			rowView.setBackgroundColor(context.getResources().getColor(
					R.color.list_bg));
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


}

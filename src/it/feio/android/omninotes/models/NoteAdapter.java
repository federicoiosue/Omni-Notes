package it.feio.android.omninotes.models;

import it.feio.android.omninotes.R;
import it.feio.android.omninotes.R.color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import android.content.Context;
import android.graphics.Color;
import android.provider.CalendarContract.Colors;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
//		View rowView = inflater.inflate(android.R.layout.simple_list_item_activated_1, parent, false);
		
		TextView title = (TextView) rowView.findViewById(R.id.note_title);
		TextView lastModification = (TextView) rowView.findViewById(R.id.note_last_modification);
		title.setText(values.get(position).getTitle());
		lastModification.setText(values.get(position).getlastModification());
		
		if (selectedItems.get(position) != null) {
			rowView.setBackgroundColor(context.getResources().getColor(R.color.list_bg_selected));
		} else {
			rowView.setBackgroundColor(context.getResources().getColor(R.color.list_bg));
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

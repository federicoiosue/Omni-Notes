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
	public View getView(int position, View v, ViewGroup parent) {     
		
		// Keeps reference to avoid future findViewById()
        NotesViewHolder viewHolder;
	
        if (v == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.note_layout, parent, false);
			
        	viewHolder = new NotesViewHolder();
            viewHolder.title = (TextView) v.findViewById(R.id.note_title);
            viewHolder.content = (TextView) v.findViewById(R.id.note_content);
            viewHolder.date = (TextView) v.findViewById(R.id.note_date);
            
            v.setTag(viewHolder);
        } else {
        	viewHolder = (NotesViewHolder) v.getTag();
        }

		// Get text for title and content views
        Note note = values.get(position);
        if (note != null) {
        	viewHolder.title.setText(note.getTitle());
        	
        	int maxContentTextLength = 40;
    		// Long content it cutted after maxContentTextLength chars and three dots are appended as suffix
    		String[] noteContent = values.get(position).getContent().split(System.getProperty("line.separator"));
    		String suffix = (noteContent[0].length() > maxContentTextLength || noteContent.length > 1) ? " ..."
    				: "";
    		String contentText = suffix.length() > 0 ? (noteContent[0].length() > maxContentTextLength ? noteContent[0]
    				.substring(0, maxContentTextLength) : noteContent[0])
    				+ suffix
    				: noteContent[0];

    		viewHolder.content.setText(contentText);

    		// Choosing if it must be shown creation date or last modification depending on sorting criteria
    		String sort_column = PreferenceManager.getDefaultSharedPreferences(context).getString(
    				Constants.PREF_SORTING_COLUMN, "");
    		if (sort_column.equals(DbHelper.KEY_CREATION))
    			viewHolder.date.setText(context.getString(R.string.creation) + " " + values.get(position).getCreationShort());
    		else
    			viewHolder.date.setText(context.getString(R.string.last_update) + " "
    					+ values.get(position).getlastModificationShort());
        }
        
		return v;
	}
	
	
	static class NotesViewHolder {
	    TextView title;
	    TextView content;
	    TextView date;
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

package it.feio.android.omninotes.models;

import it.feio.android.omninotes.R;
import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class NoteAdapter extends ArrayAdapter<Note> {

	private final Context context;
	private final List<Note> values;

	public NoteAdapter(Context context, List<Note> values) {
		super(context, R.layout.note_layout, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.note_layout, parent, false);
		TextView title = (TextView) rowView.findViewById(R.id.note_title);
		TextView timestamp = (TextView) rowView.findViewById(R.id.note_timestamp);
		title.setText(values.get(position).getTitle());
		timestamp.setText(values.get(position).getTimestamp());
		return rowView;
	}
}

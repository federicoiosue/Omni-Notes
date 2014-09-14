package it.feio.android.omninotes.utils.sync.drive;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.widget.DataBufferAdapter;

/**
* A DataBufferAdapter to display the results of file listing/querying requests.
*/
public class DriveResultsAdapter extends DataBufferAdapter<Metadata> {

    public DriveResultsAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(getContext(),
                    android.R.layout.simple_list_item_1, null);
        }
        Metadata metadata = getItem(position);
        TextView titleTextView =
                (TextView) convertView.findViewById(android.R.id.text1);
        titleTextView.setText(metadata.getTitle());
        return convertView;
    }
    
    
    
    public void setData(MetadataBuffer results) {
		clear();
		append(results);
    }
}
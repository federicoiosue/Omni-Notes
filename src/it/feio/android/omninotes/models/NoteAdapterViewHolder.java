package it.feio.android.omninotes.models;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class NoteAdapterViewHolder {
	
	View cardLayout;
	View tagMarker;
	
	TextView title;
	TextView content;
	TextView date;
	
	ImageView archiveIcon;
	ImageView locationIcon;
	ImageView alarmIcon;
	ImageView lockedIcon;
	ImageView attachmentIcon;
	
	ImageView attachmentThumbnail;
}

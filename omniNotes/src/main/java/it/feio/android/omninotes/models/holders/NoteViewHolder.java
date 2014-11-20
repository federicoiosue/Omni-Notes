package it.feio.android.omninotes.models.holders;

import android.view.View;
import android.widget.ImageView;

import com.neopixl.pixlui.components.textview.TextView;

import it.feio.android.omninotes.models.views.SquareImageView;

public class NoteViewHolder {

    public View root;
    public View cardLayout;
    public View categoryMarker;

    public TextView title;
    public TextView content;
    public TextView date;

    public ImageView archiveIcon;
    public ImageView locationIcon;
    public ImageView alarmIcon;
    public ImageView lockedIcon;
    public ImageView attachmentIcon;

    public SquareImageView attachmentThumbnail;
}
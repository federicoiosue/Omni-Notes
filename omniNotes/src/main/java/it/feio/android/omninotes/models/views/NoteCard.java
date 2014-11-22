package it.feio.android.omninotes.models.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.neopixl.pixlui.components.textview.TextView;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.async.BitmapWorkerTask;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.holders.NoteViewHolder;
import it.feio.android.omninotes.utils.BitmapHelper;
import it.feio.android.omninotes.utils.Constants;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import roboguice.util.Ln;

public class NoteCard extends Card {

    private final Context context;
    protected TextView titleView;
    protected TextView contentView;
    protected SquareImageView thumbnail;
    protected int resourceIdThumbnail;
    private Note note;

    public NoteCard(Context context, Note note) {
        this(context, note, R.layout.note_layout_expanded);
    }

    public NoteCard(Context context, Note note, int innerLayout) {
        super(context, innerLayout);
        this.context = context;
        this.note = note;
        init();
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public void init() {
//        buildTitle();
//        buildThumbnail();
        setSwipe();
    }

    private void setSwipe() {
        setSwipeable(true);
        setOnSwipeListener(new OnSwipeListener() {
            @Override
            public void onSwipe(Card card) {
                Toast.makeText(getContext(), "Removed card=" + note.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        setOnUndoSwipeListListener(new OnUndoSwipeListListener() {
            @Override
            public void onUndoSwipe(Card card) {
                Toast.makeText(getContext(), "Undo card=" + note.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        setOnUndoHideSwipeListListener(new OnUndoHideSwipeListListener() {
            @Override
            public void onUndoHideSwipe(Card card) {
                Toast.makeText(getContext(), "Hide undo card=" + note.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        //Retrieve elements
        titleView = (TextView) parent.findViewById(R.id.note_title);
        contentView = (TextView) parent.findViewById(R.id.note_content);
        thumbnail = (SquareImageView) parent.findViewById(R.id.attachmentThumbnail);


        if (titleView != null)
            titleView.setText(note.getTitle());

        if (contentView != null)
            contentView.setText(note.getContent());

        if (thumbnail != null) {
            List<Attachment> attachments = note.getAttachmentsList();
            if (attachments.size() > 0) {
                Uri thumbnailUri = BitmapHelper.getThumbnailUri(context, attachments.get(0));
                Glide.with(context)
                        .load(thumbnailUri)
                        .centerCrop()
                        .placeholder(R.drawable.abc_spinner_mtrl_am_alpha)
                        .crossFade()
                        .into(thumbnail);
            } else {
                thumbnail.setImageResource(0);
                thumbnail.setVisibility(View.GONE);
            }
        }

    }


}
package it.feio.android.omninotes.models.views;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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

import it.feio.android.omninotes.R;
import it.feio.android.omninotes.async.TextWorkerTask;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.holders.NoteViewHolder;
import it.feio.android.omninotes.utils.BitmapHelper;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.Fonts;
import it.gmariotti.cardslib.library.internal.Card;
import roboguice.util.Ln;

public class NoteCard extends Card {

    private final Activity activity;
    protected int resourceIdThumbnail;
    private Note note;

    public NoteCard(Activity context, Note note) {
        this(context, note, R.layout.note_layout_expanded);
    }

    public NoteCard(Activity activity, Note note, int innerLayout) {
        super(activity, innerLayout);
        this.activity = activity;
        this.note = note;
        init();
    }

    /**
     * Choosing which date must be shown depending on sorting criteria
     *
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
                dateText = mContext.getString(R.string.no_reminder_set);
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

        boolean expandedView = true;

        NoteViewHolder holder;


        // Overrides font sizes with the one selected from user
        Fonts.overrideTextSize(mContext, mContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS), parent);

        holder = new NoteViewHolder();

        holder.root = parent.findViewById(R.id.root);
        holder.cardLayout = parent.findViewById(R.id.card_layout);
        holder.categoryMarker = parent.findViewById(R.id.category_marker);

        holder.title = (TextView) parent.findViewById(R.id.note_title);
        holder.content = (TextView) parent.findViewById(R.id.note_content);
        holder.date = (TextView) parent.findViewById(R.id.note_date);

        holder.archiveIcon = (ImageView) parent.findViewById(R.id.archivedIcon);
        holder.locationIcon = (ImageView) parent.findViewById(R.id.locationIcon);
        holder.alarmIcon = (ImageView) parent.findViewById(R.id.alarmIcon);
        holder.lockedIcon = (ImageView) parent.findViewById(R.id.lockedIcon);
        if (!expandedView)
            holder.attachmentIcon = (ImageView) parent.findViewById(R.id.attachmentIcon);

        holder.attachmentThumbnail = (SquareImageView) parent.findViewById(R.id.attachmentThumbnail);

        parent.setTag(holder);


        try {
            TextWorkerTask task = new TextWorkerTask(activity, holder.title, holder.content, expandedView);
            if (Build.VERSION.SDK_INT >= 11) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, note);
            } else {
                task.execute(note);
            }
        } catch (RejectedExecutionException e) {
            Ln.w(e, "Oversized tasks pool to load texts!");
        }

        // Evaluates the archived state...
        holder.archiveIcon.setVisibility(note.isArchived() ? View.VISIBLE : View.GONE);
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

        String dateText = getDateText(mContext, note);
        holder.date.setText(dateText);

        if (holder.attachmentThumbnail != null) {
            List<Attachment> attachments = note.getAttachmentsList();
            if (attachments.size() > 0) {
                Uri thumbnailUri = BitmapHelper.getThumbnailUri(activity, attachments.get(0));
                Glide.with(activity)
                        .load(thumbnailUri)
                        .centerCrop()
                        .crossFade()
                        .into(holder.attachmentThumbnail);
            } else {
                holder.attachmentThumbnail.setImageResource(0);
                holder.attachmentThumbnail.setVisibility(View.GONE);
            }
        }

    }


}
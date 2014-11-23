package it.feio.android.omninotes.models.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.neopixl.pixlui.components.textview.TextView;
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

import java.util.List;
import java.util.concurrent.RejectedExecutionException;


public class NoteCard extends Card {

    private final Activity activity;
    private Note note;
    private boolean expandedView;


    public NoteCard(Activity activity, Note note, int layout) {
        super(activity, layout);
        this.activity = activity;
        this.note = note;
        expandedView = layout == R.layout.note_layout_expanded;
        setId(String.valueOf(note.get_id()));
    }


    public Note getNote() {
        return note;
    }


    public void setNote(Note note) {
        this.note = note;
    }


    @SuppressLint("WrongViewCast")
    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        NoteViewHolder holder;

        // Overrides font sizes with the one selected from user
        Fonts.overrideTextSize(activity, activity.getSharedPreferences(Constants.PREFS_NAME,
                Context.MODE_MULTI_PROCESS), parent);

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

//        } else {
//            holder = (NoteViewHolder) parent.getTag();
//        }

        try {
            TextWorkerTask task = new TextWorkerTask(activity, holder.title, holder.content, expandedView);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, note);
        } catch (RejectedExecutionException e) {
            Ln.w(e, "Oversized tasks pool to load texts!");
        }


        // Evaluates the archived state...
        holder.archiveIcon.setVisibility(note.isArchived() ? View.VISIBLE : View.GONE);
        // ...the location
        holder.locationIcon.setVisibility(note.getLongitude() != null && note.getLongitude() != 0 ? View.VISIBLE :
                View.GONE);
        // ...the presence of an alarm
        holder.alarmIcon.setVisibility(note.getAlarm() != null ? View.VISIBLE : View.GONE);
        // ...the locked with password state
        holder.lockedIcon.setVisibility(note.isLocked() ? View.VISIBLE : View.GONE);
        // ...the attachment icon for contracted view
        if (!expandedView) {
            holder.attachmentIcon.setVisibility(note.getAttachmentsList().size() > 0 ? View.VISIBLE : View.GONE);
        }


        String dateText = getDateText(activity, note);
        holder.date.setText(dateText);


        buildThumbnail(holder);

    }


    private void buildThumbnail(NoteViewHolder holder) {
        if (holder.attachmentThumbnail != null) {
            List<Attachment> attachments = note.getAttachmentsList();
            if (attachments.size() > 0) {
                holder.attachmentThumbnail.setVisibility(View.VISIBLE);
                Uri thumbnailUri = BitmapHelper.getThumbnailUri(activity, attachments.get(0));
                Glide.with(activity)
                        .load(thumbnailUri)
                        .centerCrop()
                        .crossFade()
                        .into(holder.attachmentThumbnail);
            } else {
                holder.attachmentThumbnail.setVisibility(View.GONE);
            }
        }
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


}
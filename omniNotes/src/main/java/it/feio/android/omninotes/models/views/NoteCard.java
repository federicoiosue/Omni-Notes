//package it.feio.android.omninotes.models.views;
//
//import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.graphics.Color;
//import android.net.Uri;
//import android.text.Spanned;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import com.bumptech.glide.Glide;
//import com.neopixl.pixlui.components.textview.TextView;
//import it.feio.android.omninotes.R;
//import it.feio.android.omninotes.db.DbHelper;
//import it.feio.android.omninotes.models.Attachment;
//import it.feio.android.omninotes.models.Note;
//import it.feio.android.omninotes.models.holders.NoteViewHolder;
//import it.feio.android.omninotes.utils.BitmapHelper;
//import it.feio.android.omninotes.utils.Constants;
//import it.feio.android.omninotes.utils.Fonts;
//import it.feio.android.omninotes.utils.TextHelper;
//import it.gmariotti.cardslib.library.internal.Card;
//
//import java.util.List;
//
//
//public class NoteCard extends Card {
//
//    private final Activity activity;
//    private Note note;
//    private boolean expandedView;
//
//
//    public NoteCard(Activity activity, Note note, int layout) {
//        super(activity, layout);
//        this.activity = activity;
//        this.note = note;
//        expandedView = layout == R.layout.note_layout_expanded;
//        setId(String.valueOf(note.get_id()));
//    }
//
//
//    public Note getNote() {
//        return note;
//    }
//
//
//    public void setNote(Note note) {
//        this.note = note;
//    }
//
//
//    @SuppressLint("WrongViewCast")
//    @Override
//    public void setupInnerViewElements(ViewGroup parent, View view) {
//        // Overrides font sizes with the one selected from user
//        Fonts.overrideTextSize(activity, activity.getSharedPreferences(Constants.PREFS_NAME,
//                Context.MODE_MULTI_PROCESS), parent);
//        NoteViewHolder holder = buildHolder(parent);
//        initColors(view, holder);
//        initTitleAndContent(holder);
//        initIcons(holder);
//        initDates(holder);
//        initThumbnail(holder);
//    }
//
//
//    private void initDates(NoteViewHolder holder) {
//        String dateText = getDateText(activity, note);
//        holder.date.setText(dateText);
//    }
//
//
//    private void initIcons(NoteViewHolder holder) {
//        // Evaluates the archived state...
//        holder.archiveIcon.setVisibility(note.isArchived() ? View.VISIBLE : View.GONE);
//        // ...the location
//        holder.locationIcon.setVisibility(note.getLongitude() != null && note.getLongitude() != 0 ? View.VISIBLE :
//                View.GONE);
//        // ...the presence of an alarm
//        holder.alarmIcon.setVisibility(note.getAlarm() != null ? View.VISIBLE : View.GONE);
//        // ...the locked with password state
//        holder.lockedIcon.setVisibility(note.isLocked() ? View.VISIBLE : View.GONE);
//        // ...the attachment icon for contracted view
//        if (!expandedView) {
//            holder.attachmentIcon.setVisibility(note.getAttachmentsList().size() > 0 ? View.VISIBLE : View.GONE);
//        }
//    }
//
//
//    private void initTitleAndContent(NoteViewHolder holder) {
////        try {
////            TextWorkerTask task = new TextWorkerTask(activity, holder.title, holder.content, expandedView);
////            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, note);
////        } catch (RejectedExecutionException e) {
////            Ln.w(e, "Oversized tasks pool to load texts!");
////        }
//        Spanned[] titleAndContent = TextHelper.parseTitleAndContent(activity, note);
//        holder.title.setText(titleAndContent[0]);
//        if (titleAndContent[1].length() > 0) {
//            holder.content.setText(titleAndContent[1]);
//            holder.content.setVisibility(View.VISIBLE);
//        } else {
//            if (expandedView) {
//                holder.content.setVisibility(View.INVISIBLE);
//            } else {
//                holder.content.setVisibility(View.GONE);
//            }
//        }
//
//    }
//
//
//    private NoteViewHolder buildHolder(ViewGroup parent) {
//        NoteViewHolder holder;
//        holder = new NoteViewHolder();
//
//        holder.root = parent.findViewById(R.id.root);
//        holder.cardLayout = parent.findViewById(R.id.card_layout);
//        holder.categoryMarker = parent.findViewById(R.id.category_marker);
//
//        holder.title = (TextView) parent.findViewById(R.id.note_title);
//        holder.content = (TextView) parent.findViewById(R.id.note_content);
//        holder.date = (TextView) parent.findViewById(R.id.note_date);
//
//        holder.archiveIcon = (ImageView) parent.findViewById(R.id.archivedIcon);
//        holder.locationIcon = (ImageView) parent.findViewById(R.id.locationIcon);
//        holder.alarmIcon = (ImageView) parent.findViewById(R.id.alarmIcon);
//        holder.lockedIcon = (ImageView) parent.findViewById(R.id.lockedIcon);
//        if (!expandedView)
//            holder.attachmentIcon = (ImageView) parent.findViewById(R.id.attachmentIcon);
//        holder.attachmentThumbnail = (SquareImageView) parent.findViewById(R.id.attachmentThumbnail);
//        parent.setTag(holder);
//        return holder;
//    }
//
//
//    private void initThumbnail(NoteViewHolder holder) {
//        if (holder.attachmentThumbnail != null) {
//            List<Attachment> attachments = note.getAttachmentsList();
//            if (attachments.size() > 0) {
//                holder.attachmentThumbnail.setVisibility(View.VISIBLE);
//                Uri thumbnailUri = BitmapHelper.getThumbnailUri(activity, attachments.get(0));
//                Glide.with(activity)
//                        .load(thumbnailUri)
//                        .centerCrop()
//                        .crossFade()
//                        .into(holder.attachmentThumbnail);
//            } else {
//                holder.attachmentThumbnail.setVisibility(View.GONE);
//            }
//        }
//    }
//
//
//    /**
//     * Choosing which date must be shown depending on sorting criteria
//     *
//     * @return String ith formatted date
//     */
//    public static String getDateText(Context mContext, Note note) {
//        String dateText;
//        SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFS_NAME, mContext.MODE_MULTI_PROCESS);
//        String sort_column = prefs.getString(Constants.PREF_SORTING_COLUMN, "");
//
//        // Creation
//        if (sort_column.equals(DbHelper.KEY_CREATION)) {
//            dateText = mContext.getString(R.string.creation) + " " + note.getCreationShort(mContext);
//        }
//        // Reminder
//        else if (sort_column.equals(DbHelper.KEY_REMINDER)) {
//            String alarmShort = note.getAlarmShort(mContext);
//
//            if (alarmShort.length() == 0) {
//                dateText = "";
//            } else {
//                dateText = mContext.getString(R.string.alarm_set_on) + " " + note.getAlarmShort(mContext);
//            }
//        }
//        // Others
//        else {
//            dateText = mContext.getString(R.string.last_update) + " "
//                    + note.getLastModificationShort(mContext);
//        }
//        return dateText;
//    }
//
//
//    private void initColors(View v, NoteViewHolder holder) {
//
//        String colorsPref = mContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS)
//                .getString("settings_colors_app", Constants.PREF_COLORS_APP_DEFAULT);
//
//        // Checking preference
//        if (!colorsPref.equals("disabled")) {
//
//            // Resetting transparent color to the view
//            v.setBackgroundColor(Color.parseColor("#00000000"));
//
//            // If category is set the color will be applied on the appropriate target
//            if (note.getCategory() != null && note.getCategory().getColor() != null) {
//                if (colorsPref.equals("complete") || colorsPref.equals("list")) {
//                    v.setBackgroundColor(Integer.parseInt(note.getCategory().getColor()));
//                } else {
//                    if (holder != null) {
//                        holder.categoryMarker.setBackgroundColor(Integer.parseInt(note.getCategory().getColor()));
//                    } else {
//                        v.findViewById(R.id.category_marker).setBackgroundColor(Integer.parseInt(note.getCategory()
//                                .getColor()));
//                    }
//                }
//            } else {
//                v.findViewById(R.id.category_marker).setBackgroundColor(0);
//            }
//        }
//    }
//
//
//}
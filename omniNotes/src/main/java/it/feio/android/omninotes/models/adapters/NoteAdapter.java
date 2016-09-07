/*
 * Copyright (C) 2015 Federico Iosue (federico.iosue@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.feio.android.omninotes.models.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Spanned;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.bumptech.glide.Glide;
import com.nhaarman.listviewanimations.util.Insertable;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.async.TextWorkerTask;
import it.feio.android.omninotes.helpers.NotesHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.holders.EmptyHolder;
import it.feio.android.omninotes.models.holders.NoteViewHolder;
import it.feio.android.omninotes.utils.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.RejectedExecutionException;


public class NoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Insertable {

    private final Activity mActivity;
    private final int navigation;
    private List<Note> notes = new ArrayList<>();
    private SparseBooleanArray selectedItems = new SparseBooleanArray();
    private boolean expandedView;
    private boolean gridView;
    private int layout;
    private LayoutInflater inflater;
    private long closestNoteReminder = Long.parseLong(Constants.TIMESTAMP_UNIX_EPOCH_FAR);
    private int closestNotePosition;

    private static final int TYPE_HEADER_VIEW = Integer.MIN_VALUE;
    private static final int TYPE_FOOTER_VIEW = Integer.MIN_VALUE + 1;

    private ArrayList<View> mHeaderViews = new ArrayList<>();
    private ArrayList<View> mFooterViews = new ArrayList<>();
    private int layoutID;

    public NoteAdapter(Activity activity, int layout, List<Note> notes) {
        super();

        this.mActivity = activity;

        inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        navigation = Navigation.getNavigation();

        this.notes = notes;
        manageCloserNote(notes, navigation);
        setLayout(layout);

        this.setHasStableIds(true);
    }


    /**
     * Highlighted if is part of multiselection of notes. Remember to search for child with card ui
     */
    private void manageSelectionColor(int position, Note note, NoteViewHolder holder) {
        if (selectedItems.get(position)) {
            holder.cardLayout.setBackgroundColor(mActivity.getResources().getColor(
                    R.color.list_bg_selected));
        } else {
            restoreDrawable(note, holder.cardLayout, holder);
        }
    }


    private void initThumbnail(Note note, NoteViewHolder holder) {
        // Attachment thumbnail
        if (expandedView || gridView) {
            // If note is locked or without attachments nothing is shown
            if ((note.isLocked() && !mActivity.getSharedPreferences(Constants.PREFS_NAME,
                    Context.MODE_MULTI_PROCESS).getBoolean("settings_password_access", false))
                    || note.getAttachmentsList().size() == 0) {
                holder.attachmentThumbnail.setVisibility(View.GONE);
            }
            // Otherwise...
            else {
                holder.attachmentThumbnail.setVisibility(View.VISIBLE);
                Attachment mAttachment = note.getAttachmentsList().get(0);
                Uri thumbnailUri = BitmapHelper.getThumbnailUri(mActivity, mAttachment);
                Glide.with(mActivity)
                        .load(thumbnailUri)
                        .centerCrop()
                        .crossFade()
                        .into(holder.attachmentThumbnail);
            }
        }
    }


    public List<Note> getNotes() {
        return notes;
    }


    private void initDates(Note note, NoteViewHolder holder) {
        String dateText = TextHelper.getDateText(mActivity, note, navigation);
        holder.date.setText(dateText);
    }


    private void initIcons(Note note, NoteViewHolder holder) {
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
        if (!expandedView && !gridView) {
            holder.attachmentIcon.setVisibility(note.getAttachmentsList().size() > 0 ? View.VISIBLE : View.GONE);
        }
    }


    private void initText(Note note, NoteViewHolder holder) {
        try {
            if (note.isChecklist()) {
                TextWorkerTask task = new TextWorkerTask(mActivity, holder.title, holder.content, expandedView);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, note);
            } else {
                Spanned[] titleAndContent = TextHelper.parseTitleAndContent(mActivity, note);
                holder.title.setText(titleAndContent[0]);
                holder.content.setText(titleAndContent[1]);
                holder.title.setText(titleAndContent[0]);
                if (titleAndContent[1].length() > 0) {
                    holder.content.setText(titleAndContent[1]);
                    holder.content.setVisibility(View.VISIBLE);
                } else {
                    holder.content.setVisibility(View.INVISIBLE);
                }
            }
        } catch (RejectedExecutionException e) {
            Log.w(Constants.TAG, "Oversized tasks pool to load texts!", e);
        }
    }


    /**
     * Saves the position of the closest note to align list scrolling with it on start
     */
    private void manageCloserNote(List<Note> notes, int navigation) {
        if (navigation == Navigation.REMINDERS) {
            for (int i = 0; i < notes.size(); i++) {
                long now = Calendar.getInstance().getTimeInMillis();
                long reminder = Long.parseLong(notes.get(i).getAlarm());
                if (now < reminder && reminder < closestNoteReminder) {
                    closestNotePosition = i;
                    closestNoteReminder = reminder;
                }
            }
        }

    }


    /**
     * Returns the note with the nearest reminder in the future
     */
    public int getClosestNotePosition() {
        return closestNotePosition;
    }


    public SparseBooleanArray getSelectedItems() {
        return selectedItems;
    }


    public void addSelectedItem(Integer selectedItem) {
        this.selectedItems.put(selectedItem, true);
    }


    public void removeSelectedItem(Integer selectedItem) {
        this.selectedItems.delete(selectedItem);
    }


    public void clearSelectedItems() {
        this.selectedItems.clear();
    }


    public void restoreDrawable(Note note, View v) {
        restoreDrawable(note, v, null);
    }


    public void restoreDrawable(Note note, View v, NoteViewHolder holder) {
        final int paddingBottom = v.getPaddingBottom(), paddingLeft = v.getPaddingLeft();
        final int paddingRight = v.getPaddingRight(), paddingTop = v.getPaddingTop();
        v.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        colorNote(note, v, holder);
    }


    @SuppressWarnings("unused")
    private void colorNote(Note note, View v) {
        colorNote(note, v, null);
    }


    /**
     * Color of category marker if note is categorized a function is active in preferences
     */
    private void colorNote(Note note, View v, NoteViewHolder holder) {

        String colorsPref = mActivity.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS)
                .getString("settings_colors_app", Constants.PREF_COLORS_APP_DEFAULT);

        // Checking preference
        if (!colorsPref.equals("disabled")) {

            // Resetting transparent color to the view
            v.setBackgroundColor(Color.parseColor("#00000000"));

            // If category is set the color will be applied on the appropriate target
            if (note.getCategory() != null && note.getCategory().getColor() != null) {
                if (colorsPref.equals("complete") || colorsPref.equals("list")) {
                    v.setBackgroundColor(Integer.parseInt(note.getCategory().getColor()));
                } else {
                    if (holder != null) {
                        holder.categoryMarker.setBackgroundColor(Integer.parseInt(note.getCategory().getColor()));
                    } else {
                        v.findViewById(R.id.category_marker).setBackgroundColor(Integer.parseInt(note.getCategory().getColor()));
                    }
                }
            } else {
                v.findViewById(R.id.category_marker).setBackgroundColor(0);
            }
        }
    }


    /**
     * Replaces notes
     */
    public void replace(@NonNull Note note, int index) {
        if (notes.indexOf(note) != -1) {
            remove(note);
        } else {
            index = notes.size();
        }
        add(index, note);
    }


    @Override
    public void add(int index, @NonNull Object o) {
        notes.add(index, (Note) o);
        notifyItemInserted(index);
    }


    public void remove(List<Note> notes) {
        for (Note note : notes) {
            remove(note);
        }
    }

    public void remove(@NonNull Note note) {
        int pos = getPosition(note);
        if (pos >= 0) {
            notes.remove(note);
            notifyItemRemoved(pos);
        }
    }

    public int getPosition(@NonNull Note note) {
        return notes.indexOf(note);
    }

    public Note getItem(int index) {
        return notes.get(index);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        int innerCount = getItemCount() - getHeaderViewsCount() - getFooterViewsCount();
        int headerViewsCountCount = getHeaderViewsCount();
        if (position < headerViewsCountCount) {
            return TYPE_HEADER_VIEW + position;
        } else if (headerViewsCountCount <= position && position < headerViewsCountCount + innerCount) {

            int innerItemViewType = layoutID; // to re-inflate views when layout changed (@see setLayout)
            if(innerItemViewType >= Integer.MAX_VALUE / 2) {
                throw new IllegalArgumentException("your adapter's return value of getViewTypeCount() must < Integer.MAX_VALUE / 2");
            }
            return innerItemViewType + Integer.MAX_VALUE / 2;
        } else {
            return TYPE_FOOTER_VIEW + position - headerViewsCountCount - innerCount;
        }
    }


    private NoteViewHolder buildHolder(View convertView, ViewGroup parent) {
        // Overrides font sizes with the one selected from user
        Fonts.overrideTextSize(mActivity, mActivity.getSharedPreferences(Constants.PREFS_NAME,
                Context.MODE_MULTI_PROCESS), convertView);
        return new NoteViewHolder(convertView);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int headerViewsCountCount = getHeaderViewsCount();
        if (viewType < TYPE_HEADER_VIEW + headerViewsCountCount) {
            return new EmptyHolder(mHeaderViews.get(viewType - TYPE_HEADER_VIEW));
        } else if (viewType >= TYPE_FOOTER_VIEW && viewType < Integer.MAX_VALUE / 2) {
            return new EmptyHolder(mFooterViews.get(viewType - TYPE_FOOTER_VIEW));
        } else {
            NoteViewHolder holder;
            View newview = inflater.inflate(layout, parent, false);
            holder = buildHolder(newview, parent);
            newview.setTag(holder);

            return holder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int headerViewsCountCount = getHeaderViewsCount();
        if (position >= headerViewsCountCount && position < headerViewsCountCount + (getItemCount() - getHeaderViewsCount() - getFooterViewsCount())) {
            Note note = notes.get(position);
            NoteViewHolder holderT = (NoteViewHolder) holder;

            initText(note, holderT);
            initIcons(note, holderT);
            initDates(note, holderT);
            initThumbnail(note, holderT);
            manageSelectionColor(position, note, holderT);
        } else {
            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            if(layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
                ((StaggeredGridLayoutManager.LayoutParams) layoutParams).setFullSpan(true);
            }
        }

    }

    @Override
    public int getItemCount() {
        return notes.size() + getHeaderViewsCount() + getFooterViewsCount();
    }

    public int getInnerItemCount() {
        return notes.size();
    }


    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public void setNotes(ArrayList<Note> newNotes) {
        int oldSize = getItemCount();

        this.notes = newNotes;
        manageCloserNote(notes, navigation);

        notifyItemRangeRemoved(0, oldSize);
        notifyItemRangeChanged(0, getItemCount());
    }

    public void setLayout(int layout) {
        this.layout = layout;

        expandedView = gridView = false;
        switch (layout) {
            case R.layout.note_layout_expanded:
                expandedView = true;
                layoutID = 1;
                break;
            case R.layout.note_layout_grid:
                gridView = true;
                layoutID = 2;
                break;
            default:
                layoutID = 0;
                break;
        }
    }

    /* Header / footer */
    public void addHeaderView(View header) {

        if (header == null) {
            throw new RuntimeException("header is null");
        }

        mHeaderViews.add(header);
        this.notifyDataSetChanged();
    }

    public void addFooterView(View footer) {

        if (footer == null) {
            throw new RuntimeException("footer is null");
        }

        mFooterViews.add(footer);
        this.notifyDataSetChanged();
    }

    public View getFooterView() {
        return  getFooterViewsCount()>0 ? mFooterViews.get(0) : null;
    }

    public View getHeaderView() {
        return  getHeaderViewsCount()>0 ? mHeaderViews.get(0) : null;
    }

    public void removeHeaderView(View view) {
        mHeaderViews.remove(view);
        this.notifyDataSetChanged();
    }

    public void removeFooterView(View view) {
        mFooterViews.remove(view);
        this.notifyDataSetChanged();
    }

    public int getHeaderViewsCount() {
        return mHeaderViews.size();
    }

    public int getFooterViewsCount() {
        return mFooterViews.size();
    }

    public boolean isHeader(int position) {
        return getHeaderViewsCount() > 0 && position == 0;
    }

    public boolean isFooter(int position) {
        int lastPosition = getItemCount() - 1;
        return getFooterViewsCount() > 0 && position == lastPosition;
    }
}

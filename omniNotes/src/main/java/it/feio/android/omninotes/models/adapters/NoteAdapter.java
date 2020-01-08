/*
 * Copyright (C) 2013-2020 Federico Iosue (federico@iosue.it)
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

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static it.feio.android.omninotes.utils.Constants.PREFS_NAME;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_COLORS_APP_DEFAULT;
import static it.feio.android.omninotes.utils.ConstantsBase.TIMESTAMP_UNIX_EPOCH_FAR;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import android.text.Spanned;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.nhaarman.listviewanimations.util.Insertable;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.async.TextWorkerTask;
import it.feio.android.omninotes.helpers.LogDelegate;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.holders.NoteViewHolder;
import it.feio.android.omninotes.utils.BitmapHelper;
import it.feio.android.omninotes.utils.Navigation;
import it.feio.android.omninotes.utils.TextHelper;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;


public class NoteAdapter extends ArrayAdapter<Note> implements Insertable {

  private final Activity mActivity;
  private final int navigation;
  private List<Note> notes;
  private SparseBooleanArray selectedItems = new SparseBooleanArray();
  private boolean expandedView;
  private int layout;
  private LayoutInflater inflater;
  private long closestNoteReminder = Long.parseLong(TIMESTAMP_UNIX_EPOCH_FAR);
  private int closestNotePosition;


  public NoteAdapter (Activity activity, int layout, List<Note> notes) {
    super(activity, R.layout.note_layout_expanded, notes);
    mActivity = activity;
    this.notes = notes;
    this.layout = layout;

    expandedView = layout == R.layout.note_layout_expanded;
    inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    navigation = Navigation.getNavigation();
    manageCloserNote(notes, navigation);
  }


  @Override
  public View getView (int position, View convertView, ViewGroup parent) {
    Note note = notes.get(position);
    NoteViewHolder holder;
    if (convertView == null) {
      convertView = inflater.inflate(layout, parent, false);
      holder = new NoteViewHolder(convertView);
      convertView.setTag(holder);
    } else {
      holder = (NoteViewHolder) convertView.getTag();
    }
    initText(note, holder);
    initIcons(note, holder);
    initDates(note, holder);
    initThumbnail(note, holder);
    manageSelectionColor(position, note, holder);
    return convertView;
  }


  /**
   * Highlighted if is part of multiselection of notes. Remember to search for child with card ui
   */
  private void manageSelectionColor (int position, Note note, NoteViewHolder holder) {
    if (selectedItems.get(position)) {
      holder.cardLayout.setBackgroundColor(mActivity.getResources().getColor(
          R.color.list_bg_selected));
    } else {
      restoreDrawable(note, holder.cardLayout, holder);
    }
  }


  private void initThumbnail (Note note, NoteViewHolder holder) {

    if (expandedView && holder.attachmentThumbnail != null) {
      // If note is locked or without attachments nothing is shown
      if ((note.isLocked() && !mActivity.getSharedPreferences(PREFS_NAME,
          Context.MODE_MULTI_PROCESS).getBoolean("settings_password_access", false))
          || note.getAttachmentsList().isEmpty()) {
        holder.attachmentThumbnail.setVisibility(View.GONE);
      } else {
        holder.attachmentThumbnail.setVisibility(View.VISIBLE);
        Attachment mAttachment = note.getAttachmentsList().get(0);
        Uri thumbnailUri = BitmapHelper.getThumbnailUri(mActivity, mAttachment);

        Glide.with(mActivity)
             .load(thumbnailUri)
             .apply(new RequestOptions().centerCrop())
             .transition(withCrossFade())
             .into(holder.attachmentThumbnail);
      }
    }
  }


  public List<Note> getNotes () {
    return notes;
  }


  private void initDates (Note note, NoteViewHolder holder) {
    String dateText = TextHelper.getDateText(mActivity, note, navigation);
    holder.date.setText(dateText);
  }


  private void initIcons (Note note, NoteViewHolder holder) {
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
      holder.attachmentIcon.setVisibility(!note.getAttachmentsList().isEmpty() ? View.VISIBLE : View.GONE);
    }
  }


  private void initText (Note note, NoteViewHolder holder) {
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
      LogDelegate.w("Oversized tasks pool to load texts!", e);
    }
  }


  /**
   * Saves the position of the closest note to align list scrolling with it on start
   */
  private void manageCloserNote (List<Note> notes, int navigation) {
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
  public int getClosestNotePosition () {
    return closestNotePosition;
  }


  public SparseBooleanArray getSelectedItems () {
    return selectedItems;
  }


  public void addSelectedItem (Integer selectedItem) {
    selectedItems.put(selectedItem, true);
  }


  public void removeSelectedItem (Integer selectedItem) {
    selectedItems.delete(selectedItem);
  }


  public void clearSelectedItems () {
    selectedItems.clear();
  }


  public void restoreDrawable (Note note, View v) {
    restoreDrawable(note, v, null);
  }


  public void restoreDrawable (Note note, View v, NoteViewHolder holder) {
    final int paddingBottom = v.getPaddingBottom();
    final int paddingLeft = v.getPaddingLeft();
    final int paddingRight = v.getPaddingRight();
    final int paddingTop = v.getPaddingTop();
    v.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    colorNote(note, v, holder);
  }


  @SuppressWarnings("unused")
  private void colorNote (Note note, View v) {
    colorNote(note, v, null);
  }


  /**
   * Color of category marker if note is categorized a function is active in preferences
   */
  private void colorNote (Note note, View v, NoteViewHolder holder) {

    String colorsPref = mActivity.getSharedPreferences(PREFS_NAME, Context.MODE_MULTI_PROCESS)
                                 .getString("settings_colors_app", PREF_COLORS_APP_DEFAULT);

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
  public void replace (Note note, int index) {
    if (notes.indexOf(note) != -1) {
      notes.remove(index);
    } else {
      index = notes.size();
    }
    notes.add(index, note);
  }


  @Override
  public void add (int i, @NonNull Object o) {
    insert((Note) o, i);
  }


  public void remove (List<Note> notes) {
    for (Note note : notes) {
      remove(note);
    }
  }

}




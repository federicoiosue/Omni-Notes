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

package it.feio.android.omninotes.async;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;
import it.feio.android.omninotes.DetailFragment;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.listeners.OnNoteSaved;
import it.feio.android.omninotes.receiver.AlarmReceiver;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.StorageManager;
import roboguice.util.Ln;

import java.util.Calendar;
import java.util.List;


public class SaveNoteTask extends AsyncTask<Note, Void, Note> {

    private final Activity mActivity;
    private boolean error = false;
    private boolean updateLastModification = true;
    private OnNoteSaved mOnNoteSaved;


    public SaveNoteTask(DetailFragment activity, boolean updateLastModification) {
        this(activity, null, updateLastModification);
    }


    public SaveNoteTask(DetailFragment activity, OnNoteSaved mOnNoteSaved, boolean updateLastModification) {
        super();
        mActivity = activity.getActivity();
        this.mOnNoteSaved = mOnNoteSaved;
        this.updateLastModification = updateLastModification;
    }


    @Override
    protected Note doInBackground(Note... params) {
        Note note = params[0];
        purgeRemovedAttachments(note);

        if (!error) {
            DbHelper db = DbHelper.getInstance(mActivity);
            // Note updating on database
            note = db.updateNote(note, updateLastModification);
        } else {
            Toast.makeText(mActivity, mActivity.getString(R.string.error_saving_attachments), 
                    Toast.LENGTH_SHORT).show();
        }

        return note;
    }


    private void purgeRemovedAttachments(Note note) {
        List<Attachment> deletedAttachments = note.getAttachmentsListOld();
        for (Attachment attachment : note.getAttachmentsList()) {
            if (attachment.getId() != 0) {
                // Workaround to prevent deleting attachments if instance is changed (app restart)
                if (deletedAttachments.indexOf(attachment) == -1) {
                    attachment = getFixedAttachmentInstance(deletedAttachments, attachment);
                }
                deletedAttachments.remove(attachment);
            }
        }
        // Remove from database deleted attachments
        for (Attachment deletedAttachment : deletedAttachments) {
            StorageManager.delete(mActivity, deletedAttachment.getUri().getPath());
            Ln.d("Removed attachment " + deletedAttachment.getUri());
        }
    }


    private Attachment getFixedAttachmentInstance(List<Attachment> deletedAttachments, Attachment attachment) {
        for (Attachment deletedAttachment : deletedAttachments) {
            if (deletedAttachment.getId() == attachment.getId()) return deletedAttachment;
        }
        return attachment;
    }


    @Override
    protected void onPostExecute(Note note) {
        super.onPostExecute(note);

        // Set reminder if is not passed yet
        long now = Calendar.getInstance().getTimeInMillis();
        if (note.getAlarm() != null && Long.parseLong(note.getAlarm()) >= now) {
            setAlarm(note);
        }

        if (this.mOnNoteSaved != null) {
            mOnNoteSaved.onNoteSaved(note);
        }
    }


    private void setAlarm(Note note) {
        Intent intent = new Intent(mActivity, AlarmReceiver.class);
        intent.putExtra(Constants.INTENT_NOTE, (android.os.Parcelable) note);
        PendingIntent sender = PendingIntent.getBroadcast(mActivity, note.getCreation().intValue(), intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) mActivity.getSystemService(Activity.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, Long.parseLong(note.getAlarm()), sender);
    }


}

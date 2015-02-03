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
package it.feio.android.omninotes.async.notes;

import android.content.Context;
import android.os.AsyncTask;

import it.feio.android.omninotes.BaseActivity;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.StorageHelper;


public class DeleteNoteTask extends AsyncTask<Note, Void, Integer> {

    private final Context mContext;


    public DeleteNoteTask(Context mContext) {
        this.mContext = mContext;
    }


    @Override
    protected Integer doInBackground(Note... params) {
        Integer result = null;
        Note note = params[0];

        // Deleting note using DbHelper
        DbHelper db = DbHelper.getInstance(mContext);
        boolean deleted = db.deleteNote(note);

        if (deleted) {
            // Attachment deletion from storage
            boolean attachmentsDeleted = false;
            for (Attachment mAttachment : note.getAttachmentsList()) {
                StorageHelper.deleteExternalStoragePrivateFile(mContext, mAttachment.getUri().getLastPathSegment());
            }
            result = deleted && attachmentsDeleted ? note.get_id() : null;
        }
        return result;
    }


    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        BaseActivity.notifyAppWidgets(mContext);
    }
}

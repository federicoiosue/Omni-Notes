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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import it.feio.android.omninotes.BaseActivity;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.ReminderHelper;
import roboguice.util.Ln;

import java.util.List;


public class AlarmRestoreOnRebootService extends Service {

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Ln.i("System rebooted: service refreshing reminders");
        Context mContext = getApplicationContext();
        // Refresh widgets data
        BaseActivity.notifyAppWidgets(mContext);

        // Retrieves all notes with reminder set
        DbHelper db = DbHelper.getInstance(mContext);
        List<Note> notes = db.getNotesWithReminder(true);
        Ln.d("Found " + notes.size() + " reminders");
        for (Note note : notes) {
            ReminderHelper.addReminder(OmniNotes.getAppContext(), note);
        }
        return Service.START_NOT_STICKY;
    }

}

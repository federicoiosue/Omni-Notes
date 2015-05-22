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

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.TextHelper;
import it.feio.android.omninotes.utils.date.DateHelper;

import java.util.*;


public class ONDashClockExtension extends DashClockExtension {

    private enum Counters {ACTIVE, REMINDERS, TODAY};


    private DashClockUpdateReceiver mDashClockReceiver;


    @Override
    protected void onInitialize(boolean isReconnect) {
        super.onInitialize(isReconnect);
        LocalBroadcastManager broadcastMgr = LocalBroadcastManager.getInstance(this);
        if (mDashClockReceiver != null) {
            broadcastMgr.unregisterReceiver(mDashClockReceiver);
        }
        mDashClockReceiver = new DashClockUpdateReceiver();
        broadcastMgr.registerReceiver(mDashClockReceiver, new IntentFilter(Constants.INTENT_UPDATE_DASHCLOCK));
    }


    @SuppressLint("DefaultLocale")
    @Override
    protected void onUpdateData(int reason) {

        Map<String, List<Note>> notesCounters = getNotesCounters();
        int reminders = notesCounters.get(Counters.REMINDERS).size();

        StringBuilder expandedTitle = new StringBuilder();
        expandedTitle.append(notesCounters.get(Counters.ACTIVE).size()).append(" ").append(getString(R.string.notes).toLowerCase());
        if (reminders > 0) {
            expandedTitle.append(", ").append(reminders).append(" ").append(getString(R.string.reminders));
        }

        StringBuilder expandedBody = new StringBuilder();
        if (notesCounters.get(Counters.TODAY).size() > 0) {
            expandedBody.append(notesCounters.get(Counters.TODAY).size()).append(" ").append(getString(R.string.today)).append(":");
            for (Note todayReminder : notesCounters.get(Counters.TODAY)) {
                expandedBody.append(System.getProperty("line.separator")).append(("☆ ")).append(TextHelper
                        .parseTitleAndContent(this, todayReminder)[0]);
            }
        }

        // Publish the extension data update.
        publishUpdate(new ExtensionData()
                .visible(true)
                .icon(R.drawable.ic_stat_notification_icon)
                .status(String.valueOf(notesCounters.get(Counters.ACTIVE)))
                .expandedTitle(expandedTitle.toString())
                .expandedBody(expandedBody.toString())
                .clickIntent(new Intent(this, MainActivity.class)));
    }


    private Map<String, List<Note>> getNotesCounters() {
        Map noteCounters = new HashMap<>();
        List<Note> activeNotes = new ArrayList<>();
        List<Note> reminders = new ArrayList<>();
        List<Note> today = new ArrayList<>();
        for (Note note : DbHelper.getInstance().getNotesActive()) {
            activeNotes.add(note);
            if (note.getAlarm() != null) {
                reminders.add(note);
                if (DateHelper.isSameDay(Long.valueOf(note.getAlarm()), Calendar.getInstance().getTimeInMillis())) {
                    today.add(note);
                }
            }
        }
        noteCounters.put(Counters.ACTIVE, activeNotes);
        noteCounters.put(Counters.REMINDERS, reminders);
        noteCounters.put(Counters.TODAY, today);
        return noteCounters;
    }


    public class DashClockUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            onUpdateData(UPDATE_REASON_MANUAL);
        }

    }
}

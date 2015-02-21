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
package it.feio.android.omninotes.receiver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spanned;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.SnoozeActivity;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.NotificationsHelper;
import it.feio.android.omninotes.utils.TextHelper;
import roboguice.util.Ln;


public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context mContext, Intent intent) {
        try {
            Note note = intent.getExtras().getParcelable(Constants.INTENT_NOTE);
            createNotification(mContext, note);
        } catch (Exception e) {
            Ln.e("Error on receiving reminder", e);
        }
    }


    private void createNotification(Context mContext, Note note) {

        // Retrieving preferences
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS);

        // Prepare text contents
        Spanned[] titleAndContent = TextHelper.parseTitleAndContent(mContext, note);
        String title = titleAndContent[0].toString();
        String text = titleAndContent[1].toString();

        Intent snoozeIntent = new Intent(mContext, SnoozeActivity.class);
        snoozeIntent.setAction(Constants.ACTION_SNOOZE);
        snoozeIntent.putExtra(Constants.INTENT_NOTE, (android.os.Parcelable) note);
        snoozeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent piSnooze = PendingIntent.getActivity(mContext, 0, snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent postponeIntent = new Intent(mContext, SnoozeActivity.class);
        postponeIntent.setAction(Constants.ACTION_POSTPONE);
        postponeIntent.putExtra(Constants.INTENT_NOTE, (android.os.Parcelable) note);
        snoozeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent piPostpone = PendingIntent.getActivity(mContext, 0, postponeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        String snoozeDelay = mContext.getSharedPreferences(Constants.PREFS_NAME,
                Context.MODE_MULTI_PROCESS).getString("settings_notification_snooze_delay", "10");

        // Next create the bundle and initialize it
        Intent intent = new Intent(mContext, SnoozeActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.INTENT_NOTE, note);
        intent.putExtras(bundle);

        // Sets the Activity to start in a new, empty task
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Workaround to fix problems with multiple notifications
        intent.setAction(Constants.ACTION_NOTIFICATION_CLICK + Long.toString(System.currentTimeMillis()));

        // Creates the PendingIntent
        PendingIntent notifyIntent = PendingIntent.getActivity(mContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationsHelper notificationsHelper = new NotificationsHelper(mContext);
        notificationsHelper.createNotification(R.drawable.ic_stat_notification_icon, title, notifyIntent);
        notificationsHelper.setMessage(text);

        notificationsHelper.getBuilder()
                .addAction(R.drawable.ic_material_reminder_time_light, it.feio.android.omninotes.utils.TextHelper
                        .capitalize(mContext.getString(R.string.snooze)) + ": " + snoozeDelay, piSnooze)
                .addAction(R.drawable.ic_remind_later_light,
                        it.feio.android.omninotes.utils.TextHelper.capitalize(mContext.getString(R.string
                                .add_reminder)), piPostpone);

        // Ringtone options
        String ringtone = prefs.getString("settings_notification_ringtone", null);
        if (ringtone != null) {
            notificationsHelper.setRingtone(ringtone);
        }

        // Vibration options
        long[] pattern = {500, 500};
        if (prefs.getBoolean("settings_notification_vibration", true)) {
            notificationsHelper.setVibration(pattern);
        }

        notificationsHelper.show(note.get_id());
    }
}

/*******************************************************************************
 * Copyright 2014 Federico Iosue (federico.iosue@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package it.feio.android.omninotes.receiver;

import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.date.DateHelper;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context mContext, Intent intent) {
		try {

//			PowerManager pm = (PowerManager) mContext
//					.getSystemService(Context.POWER_SERVICE);
//			PowerManager.WakeLock wl = pm.newWakeLock(
//					PowerManager.PARTIAL_WAKE_LOCK, Constants.TAG);
//			// Acquire the lock
//			wl.acquire();

			try {			
				Note note = (Note) intent.getExtras().getParcelable(Constants.INTENT_NOTE);

				createNotification(mContext, note);
			}

			// Release the lock
			finally {
//				wl.release();
			}

		} catch (Exception e) {
			Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
		}

	}

	private void createNotification(Context mContext, Note note) {
		
		// Retrieving preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		// Prepare text contents
		String title = note.getTitle().length() > 0 ? note.getTitle() : note
				.getContent();
		String alarmText = DateHelper.getString(
				Long.parseLong(note.getAlarm()),
				Constants.DATE_FORMAT_SHORT_DATE)
				+ ", "
				+ DateHelper.getDateTimeShort(mContext, Long.parseLong(note.getAlarm()));
		String text = note.getTitle().length() > 0 && note.getContent().length() > 0 ? note.getContent() : alarmText;
		
		// Notification building
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				mContext).setSmallIcon(R.drawable.ic_stat_notification_icon)
				.setContentTitle(title).setContentText(text)
				.setAutoCancel(true);
		
		
		// Ringtone options
		String ringtone = prefs.getString("settings_notification_ringtone", null);
		if (ringtone != null) {
			mBuilder.setSound(Uri.parse(ringtone));
		}
		
		
		// Vibration options
		long[] pattern = {500,500};		
		if (prefs.getBoolean("settings_notification_vibration", true))
			mBuilder.setVibrate(pattern);
		
		
		Bundle bundle = new Bundle();
		// Add the parameters to bundle as
		bundle.putParcelable(Constants.INTENT_NOTE, note);
		
		
		// Sets up the Snooze and Dismiss action buttons that will appear in the
		// big view of the notification.
		Intent dismissIntent = new Intent(mContext, it.feio.android.omninotes.async.NotificationService.class);
		dismissIntent.setAction(Constants.ACTION_DISMISS);
		dismissIntent.putExtra(Constants.INTENT_NOTE, note);
		PendingIntent piDismiss = PendingIntent.getService(mContext, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Intent snoozeIntent = new Intent(mContext, it.feio.android.omninotes.async.NotificationService.class);
		snoozeIntent.setAction(Constants.ACTION_SNOOZE);
		snoozeIntent.putExtra(Constants.INTENT_NOTE, note);
		PendingIntent piSnooze = PendingIntent.getService(mContext, 0, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
        //Sets the big view "big text" style  
		mBuilder.addAction (R.drawable.ic_action_cancel_dark,
       		mContext.getString(R.string.cancel), piDismiss)
       .addAction (R.drawable.ic_action_alarms_dark,
       		mContext.getString(R.string.snooze), piSnooze);
		

		// Next create the bundle and initialize it
		Intent intent = new Intent(mContext, it.feio.android.omninotes.DetailActivity.class);		
		// Add this bundle to the intent
		intent.putExtras(bundle);
		// Sets the Activity to start in a new, empty task
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// Workaround to fix problems with multiple notifications
	    intent.setAction(Long.toString(System.currentTimeMillis()));

		// Creates the PendingIntent
		PendingIntent notifyIntent = PendingIntent.getActivity(mContext, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// Puts the PendingIntent into the notification builder
		mBuilder.setContentIntent(notifyIntent);
		
		
		
		
		// Notifications are issued by sending them to the
		// NotificationManager system service.
		NotificationManager mNotificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		// Builds an anonymous Notification object from the builder, and
		// passes it to the NotificationManager
		mNotificationManager.notify(note.get_id(), mBuilder.build());
	}
}

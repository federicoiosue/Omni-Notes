/*
 * Copyright (C) 2013-2026 Federico Iosue (omninotes@iosue.it)
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

import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_POSTPONE;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_SNOOZE;
import static it.feio.android.omninotes.utils.ConstantsBase.INTENT_NOTE;
import static it.feio.android.omninotes.utils.ConstantsBase.MIME_TYPE_FILES;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.Spanned;
import com.pixplicity.easyprefs.library.Prefs;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.SnoozeActivity;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.helpers.IntentHelper;
import it.feio.android.omninotes.helpers.LogDelegate;
import it.feio.android.omninotes.helpers.notifications.NotificationChannels.NotificationChannelNames;
import it.feio.android.omninotes.helpers.notifications.NotificationsHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.services.NotificationListener;
import it.feio.android.omninotes.utils.BitmapHelper;
import it.feio.android.omninotes.utils.ParcelableUtil;
import it.feio.android.omninotes.utils.TextHelper;
import java.util.List;
import java.util.Objects;


public class AlarmReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context mContext, Intent intent) {
    try {
      if (intent.hasExtra(INTENT_NOTE)) {
        Note note = ParcelableUtil.unmarshall(intent.getExtras().getByteArray(INTENT_NOTE), Note
            .CREATOR);
        Note currentNote = reloadNote(note);
        if (currentNote == null) {
          LogDelegate.d("Reminder fired for a note that no longer exists, skipping");
          return;
        }
        if (!Objects.equals(currentNote.getAlarm(), note.getAlarm())) {
          LogDelegate.d("Reminder fired for a reminder that was changed or removed, skipping");
          return;
        }
        createNotification(mContext, currentNote);
        SnoozeActivity.setNextRecurrentReminder(currentNote);
        updateNote(currentNote);
      }
    } catch (Exception e) {
      LogDelegate.e("Error on receiving reminder", e);
    }
  }

  /**
   * Reloads the note the alarm was scheduled for, so that a still queued alarm can't overwrite it
   * with the copy stored when the reminder was set
   */
  private Note reloadNote(Note note) {
    if (note.getCreation() == null) {
      return note;
    }
    return DbHelper.getInstance().getNote(note.getCreation());
  }

  private void updateNote(Note note) {
    note.setArchived(false);
    if (!NotificationListener.isRunning()) {
      note.setReminderFired(true);
    }
    DbHelper.getInstance().updateNote(note, false);
  }

  private void createNotification(Context mContext, Note note) {
    PendingIntent piSnooze = IntentHelper
        .getNotePendingIntent(mContext, SnoozeActivity.class, ACTION_SNOOZE, note);
    PendingIntent piPostpone = IntentHelper
        .getNotePendingIntent(mContext, SnoozeActivity.class, ACTION_POSTPONE, note);
    PendingIntent notifyIntent = IntentHelper
        .getNotePendingIntent(mContext, SnoozeActivity.class, null, note);

    Spanned[] titleAndContent = TextHelper.parseTitleAndContent(mContext, note);
    String title = TextHelper.getAlternativeTitle(mContext, note, titleAndContent[0]);
    String text = titleAndContent[1].toString();

    NotificationsHelper notificationsHelper = new NotificationsHelper(mContext);
    notificationsHelper.createStandardNotification(NotificationChannelNames.REMINDERS,
        R.drawable.ic_stat_notification,
        title, notifyIntent).setLedActive().setMessage(text);

    List<Attachment> attachments = note.getAttachmentsList();
    if (!attachments.isEmpty() && !attachments.get(0).getMime_type().equals(MIME_TYPE_FILES)) {
      Bitmap notificationIcon = BitmapHelper
          .getBitmapFromAttachment(mContext, note.getAttachmentsList().get(0), 128,
              128);
      notificationsHelper.setLargeIcon(notificationIcon);
    }

    String snoozeDelay = Prefs.getString("settings_notification_snooze_delay", "10");

    notificationsHelper.getBuilder()
        .addAction(R.drawable.ic_material_reminder_time_light,
            TextHelper.capitalize(mContext.getString(R.string.snooze)) + ": " + snoozeDelay,
            piSnooze)
        .addAction(R.drawable.ic_remind_later_light,
            TextHelper.capitalize(mContext.getString(R.string
                .add_reminder)), piPostpone);

    setRingtone(notificationsHelper);
    setVibrate(notificationsHelper);

    notificationsHelper.show(note.get_id());
  }


  private void setRingtone(NotificationsHelper notificationsHelper) {
    String ringtone = Prefs.getString("settings_notification_ringtone", null);
    notificationsHelper.setRingtone(ringtone);
  }


  private void setVibrate(NotificationsHelper notificationsHelper) {
    if (Prefs.getBoolean("settings_notification_vibration", true)) {
      notificationsHelper.setVibration();
    }
  }

}

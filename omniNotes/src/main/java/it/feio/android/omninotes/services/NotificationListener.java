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

package it.feio.android.omninotes.services;

import android.content.ContentResolver;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import de.greenrobot.event.EventBus;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.async.bus.NotificationRemovedEvent;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.helpers.LogDelegate;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.date.DateUtils;


public class NotificationListener extends NotificationListenerService {

  @Override
  public void onCreate () {
    super.onCreate();
    EventBus.getDefault().register(this);
  }

  @Override
  public void onDestroy () {
    super.onDestroy();
    EventBus.getDefault().unregister(this);
  }

  @Override
  public void onNotificationPosted (StatusBarNotification sbn) {
    LogDelegate.d("Notification posted for note: " + sbn.getId());
  }

  @Override
  public void onNotificationRemoved (StatusBarNotification sbn) {
    if (sbn.getPackageName().equals(getPackageName())) {
      EventBus.getDefault().post(new NotificationRemovedEvent(sbn));
      LogDelegate.d("Notification removed for note: " + sbn.getId());
    }
  }

  public void onEventAsync (NotificationRemovedEvent event) {
    long nodeId = Long.parseLong(event.statusBarNotification.getTag());
    Note note = DbHelper.getInstance().getNote(nodeId);
    if (!DateUtils.isFuture(note.getAlarm())) {
      DbHelper.getInstance().setReminderFired(nodeId, true);
    }
  }

  public static boolean isRunning () {
    ContentResolver contentResolver = OmniNotes.getAppContext().getContentResolver();
    String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
    return enabledNotificationListeners != null && enabledNotificationListeners.contains(NotificationListener
        .class.getSimpleName());
  }

}

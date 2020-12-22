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

package it.feio.android.omninotes.helpers.notifications;

import static it.feio.android.omninotes.utils.Constants.CHANNEL_BACKUPS_ID;
import static it.feio.android.omninotes.utils.Constants.CHANNEL_PINNED_ID;
import static it.feio.android.omninotes.utils.Constants.CHANNEL_REMINDERS_ID;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.os.Build;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;
import java.util.EnumMap;
import java.util.Map;

@TargetApi(Build.VERSION_CODES.O)
public class NotificationChannels {


  protected static final Map<NotificationChannelNames, NotificationChannel> channels;

  static {
    channels = new EnumMap<>(NotificationChannelNames.class);
    channels.put(NotificationChannelNames.BACKUPS, new NotificationChannel(
        NotificationManager.IMPORTANCE_DEFAULT,
        OmniNotes.getAppContext().getString(R.string.channel_backups_name),
        OmniNotes.getAppContext().getString(R.string.channel_backups_description),
        CHANNEL_BACKUPS_ID));
    channels.put(NotificationChannelNames.REMINDERS, new NotificationChannel(
        NotificationManager.IMPORTANCE_DEFAULT,
        OmniNotes.getAppContext().getString(R.string.channel_reminders_name),
        OmniNotes.getAppContext().getString(R.string.channel_reminders_description),
        CHANNEL_REMINDERS_ID));
    channels.put(NotificationChannelNames.PINNED, new NotificationChannel(
        NotificationManager.IMPORTANCE_DEFAULT,
        OmniNotes.getAppContext().getString(R.string.channel_pinned_name),
        OmniNotes.getAppContext().getString(R.string.channel_pinned_description),
        CHANNEL_PINNED_ID));
  }

  public enum NotificationChannelNames {
    BACKUPS, REMINDERS, PINNED
  }

}

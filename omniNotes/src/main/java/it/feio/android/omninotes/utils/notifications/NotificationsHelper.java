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

package it.feio.android.omninotes.utils.notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import it.feio.android.omninotes.R;


public class NotificationsHelper {

  private Context mContext;
  private Builder mBuilder;
  private NotificationManager mNotificationManager;

  public NotificationsHelper (Context mContext) {
    this.mContext = mContext.getApplicationContext();
    if (mNotificationManager == null) {
      mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }
  }

  /**
   * Creates the NotificationChannel, but only on API 26+ because the NotificationChannel class is new and not in the
   * support library
   */
  @TargetApi(Build.VERSION_CODES.O)
  public void initNotificationChannels () {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
      for (it.feio.android.omninotes.utils.notifications.NotificationChannel notificationChannel : NotificationChannels.getChannels()) {
        NotificationChannel channel = new NotificationChannel(notificationChannel.id, notificationChannel
            .name, notificationChannel.importance);
        channel.setDescription(notificationChannel.description);
        mNotificationManager.createNotificationChannel(channel);
      }
    }
  }

  /**
   * Creation of notification on operations completed
   */
  public NotificationsHelper createNotification (NotificationChannels.NotificationChannelNames channelId, int
      smallIcon, String title, PendingIntent notifyIntent) {
    mBuilder = new NotificationCompat.Builder(mContext, channelId.name()).setSmallIcon(smallIcon).setContentTitle(title)
                                                                         .setAutoCancel(true).setColor(
            mContext.getResources().getColor(R.color.colorAccent));
    mBuilder.setContentIntent(notifyIntent);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      setLargeIcon(R.drawable.logo_notification_lollipop);
    } else {
      setLargeIcon(R.mipmap.ic_launcher);
    }

    return this;
  }

  public Builder getBuilder () {
    return mBuilder;
  }

  public NotificationsHelper setLargeIcon (Bitmap largeIconBitmap) {
    mBuilder.setLargeIcon(largeIconBitmap);
    return this;
  }

  public NotificationsHelper setLargeIcon (int largeIconResource) {
    Bitmap largeIconBitmap = BitmapFactory.decodeResource(mContext.getResources(), largeIconResource);
    return setLargeIcon(largeIconBitmap);
  }

  public NotificationsHelper setRingtone (String ringtone) {
    if (ringtone != null) {
      mBuilder.setSound(Uri.parse(ringtone));
    }
    return this;
  }

  public NotificationsHelper setVibration () {
    return setVibration(null);
  }

  public NotificationsHelper setVibration (long[] pattern) {
    if (pattern == null || pattern.length == 0) {
      pattern = new long[]{500, 500};
    }
    mBuilder.setVibrate(pattern);
    return this;
  }

  public NotificationsHelper setLedActive () {
    mBuilder.setLights(Color.BLUE, 1000, 1000);
    return this;
  }

  public NotificationsHelper setIcon (int icon) {
    mBuilder.setSmallIcon(icon);
    return this;
  }

  public NotificationsHelper setMessage (String message) {
    mBuilder.setContentText(message);
    return this;
  }

  public NotificationsHelper setIndeterminate () {
    mBuilder.setProgress(0, 0, true);
    return this;
  }

  public NotificationsHelper setOngoing () {
    mBuilder.setOngoing(true);
    return this;
  }

  public NotificationsHelper show () {
    show(0);
    return this;
  }

  public NotificationsHelper show (long id) {
    Notification mNotification = mBuilder.build();
    if (mNotification.contentIntent == null) {
      // Creates a dummy PendingIntent
      mBuilder.setContentIntent(PendingIntent.getActivity(mContext, 0, new Intent(),
          PendingIntent.FLAG_UPDATE_CURRENT));
    }
    // Builds an anonymous Notification object from the builder, and passes it to the NotificationManager
    mNotificationManager.notify(String.valueOf(id), 0, mBuilder.build());
    return this;
  }

  public NotificationsHelper start (NotificationChannels.NotificationChannelNames channelId, int
      smallIcon, String title) {
    createNotification(channelId, smallIcon, title, null).setIndeterminate().setOngoing();
    mNotificationManager.notify(0, mBuilder.setOnlyAlertOnce(true).build());
    return this;
  }

  public void updateMessage (String message) {
    updateMessage(0, message);
  }

  public void updateMessage (int id, String message) {
    mNotificationManager.notify(id, mBuilder.setContentText(message).build());
  }

  public void finish (Intent intent, String message) {
    finish(0, intent, message);
  }

  public void finish (int id, Intent intent, String message) {
    mBuilder.setContentTitle(message).setProgress(0, 0, false).setOngoing(false);
    mNotificationManager.notify(id, mBuilder.build());
  }

  public void cancel () {
    mNotificationManager.cancel(0);
  }

  public void cancel (int id) {
    mNotificationManager.cancel(id);
  }

}

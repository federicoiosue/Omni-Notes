/*
 * Copyright (C) 2018 Federico Iosue (federico.iosue@gmail.com)
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

package it.feio.android.omninotes.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import it.feio.android.omninotes.R;


public class NotificationsHelper {

    private Context mContext;
    private Builder mBuilder;
    private NotificationManager mNotificationManager;


    public NotificationsHelper(Context mContext) {
        this.mContext = mContext.getApplicationContext();
    }


    /**
     * Creation of notification on operations completed
     */
    public NotificationsHelper createNotification(int smallIcon, String title, PendingIntent notifyIntent) {
        mBuilder = new NotificationCompat.Builder(mContext).setSmallIcon(smallIcon).setContentTitle(title)
                .setAutoCancel(true).setColor(mContext.getResources().getColor(R.color.colorAccent));
        mBuilder.setContentIntent(notifyIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setLargeIcon(R.drawable.logo_notification_lollipop);
        } else {
            setLargeIcon(R.mipmap.ic_launcher);
        }

        return this;
    }


    public Builder getBuilder() {
        return mBuilder;
    }


    public NotificationsHelper setLargeIcon(Bitmap largeIconBitmap) {
        mBuilder.setLargeIcon(largeIconBitmap);
        return this;
    }


    public NotificationsHelper setLargeIcon(int largeIconResource) {
        Bitmap largeIconBitmap = BitmapFactory.decodeResource(mContext.getResources(), largeIconResource);
        return setLargeIcon(largeIconBitmap);
    }


    public NotificationsHelper setRingtone(String ringtone) {
        // Ringtone options
        if (ringtone != null) {
            mBuilder.setSound(Uri.parse(ringtone));
        }
        return this;
    }


    public NotificationsHelper setVibration() {
        return setVibration(null);
    }


    public NotificationsHelper setVibration(long[] pattern) {
        if (pattern == null || pattern.length == 0) {
            pattern = new long[]{500, 500};
        }
        mBuilder.setVibrate(pattern);
        return this;
    }


    public NotificationsHelper setLedActive() {
        mBuilder.setLights(Color.BLUE, 1000, 1000);
        return this;
    }


    public NotificationsHelper setIcon(int icon) {
        mBuilder.setSmallIcon(icon);
        return this;
    }


    public NotificationsHelper setMessage(String message) {
        mBuilder.setContentText(message);
        return this;
    }


    public NotificationsHelper setIndeterminate() {
        mBuilder.setProgress(0, 0, true);
        return this;
    }


    public NotificationsHelper setOngoing() {
        mBuilder.setOngoing(true);
        return this;
    }


    public NotificationsHelper show() {
        show(0);
        return this;
    }


    public NotificationsHelper show(long id) {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }
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

}

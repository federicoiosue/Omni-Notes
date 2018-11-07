package it.feio.android.omninotes.utils.notifications;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.R;

@TargetApi(Build.VERSION_CODES.O)
public class NotificationChannels {

    private static List<NotificationChannel> channels = new ArrayList<NotificationChannel>() {{
        add(new NotificationChannel(NotificationManager.IMPORTANCE_HIGH, OmniNotes.getAppContext().getString(R.string
                .channel_backups_name), OmniNotes.getAppContext().getString(R.string
                .channel_backups_description)));
        add(new NotificationChannel(NotificationManager.IMPORTANCE_DEFAULT, OmniNotes.getAppContext().getString(R.string
                .channel_reminders_name), OmniNotes.getAppContext().getString(R.string
                .channel_reminders_description)));
    }};

    public static List<NotificationChannel> getChannels() {
        return channels;
    }

    public enum NotificationChannelNames {
        Backups, Reminders
    }
}

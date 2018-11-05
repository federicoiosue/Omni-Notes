package it.feio.android.omninotes.utils.notifications;

public class NotificationChannel {

    int importance;
    String name;
    String description;

    NotificationChannel(int importance, String name, String description) {
        this.importance = importance;
        this.name = name;
        this.description = description;
    }
}
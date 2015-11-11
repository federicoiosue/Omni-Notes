package it.feio.android.omninotes.services;

import android.annotation.TargetApi;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import de.greenrobot.event.EventBus;
import it.feio.android.omninotes.async.bus.NotificationRemovedEvent;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.date.DateHelper;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationListener extends NotificationListenerService {


	@Override
	public void onCreate() {
		super.onCreate();
		EventBus.getDefault().register(this);
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}


	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		if (sbn.getPackageName().equals(getPackageName())) {
			EventBus.getDefault().post(new NotificationRemovedEvent(sbn));
			Log.d(Constants.TAG, "Notification removed for note: " + sbn.getId());
		}
	}


	public void onEventAsync(NotificationRemovedEvent event) {
		Long nodeId = Long.valueOf(event.statusBarNotification.getTag());
		Note note = DbHelper.getInstance().getNote(nodeId);
		if (!DateHelper.isFuture(note.getAlarm())) {
			DbHelper.getInstance().setReminderFired(nodeId, true);
		}
	}

}
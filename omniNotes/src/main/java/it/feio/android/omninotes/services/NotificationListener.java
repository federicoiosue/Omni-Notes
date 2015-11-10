package it.feio.android.omninotes.services;

import android.annotation.TargetApi;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.utils.Constants;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationListener extends NotificationListenerService {

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		if (sbn.getPackageName().equals(getPackageName())) {
			DbHelper.getInstance().setReminderFired(Long.valueOf(sbn.getTag()), true);
			Log.d(Constants.TAG, "Notification removed for note: " + sbn.getId());
		}
	}

}
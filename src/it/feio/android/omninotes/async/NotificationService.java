package it.feio.android.omninotes.async;

import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.receiver.AlarmReceiver;
import it.feio.android.omninotes.utils.Constants;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class NotificationService extends IntentService{

	public NotificationService() {
		super(NotificationService.class.getName());
	}
	

	@Override
	protected void onHandleIntent(Intent intent) {
		
		Note note = intent.getParcelableExtra(Constants.INTENT_NOTE);
		// If an alarm has been fired a notification must be generated
		if (Constants.ACTION_DISMISS.equals(intent.getAction())) {} 
		else if (Constants.ACTION_SNOOZE.equals(intent.getAction())) {
			String snoozeDelay = PreferenceManager.getDefaultSharedPreferences(this).getString("settings_notification_snooze_delay", "10");
			long newAlarm = Calendar.getInstance().getTimeInMillis() + Integer.parseInt(snoozeDelay) * 60 * 1000;
			setAlarm(note, newAlarm);
		}		
		removeNotification(note);
	}
	
	
	private void removeNotification(Note note) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(note.get_id());		
	}
	
	private void setAlarm(Note note, long newAlarm) {
		Intent intent = new Intent(this, AlarmReceiver.class);
		intent.putExtra(Constants.INTENT_NOTE, note);
		PendingIntent sender = PendingIntent.getBroadcast(this, Constants.INTENT_ALARM_CODE, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager) this.getSystemService(this.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, newAlarm, sender);
	}

}

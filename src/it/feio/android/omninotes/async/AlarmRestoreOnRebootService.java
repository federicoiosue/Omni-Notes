package it.feio.android.omninotes.async;

import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.receiver.AlarmReceiver;
import it.feio.android.omninotes.utils.Constants;

import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AlarmRestoreOnRebootService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(Constants.TAG, "System rebooted: service refreshing reminders");

		Context ctx = getApplicationContext();

//		PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
//		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Constants.TAG);
//		// Acquire the lock
//		wl.acquire();

		// Retrieves all notes with reminder set
		try {
			DbHelper db = new DbHelper(ctx);
			List<Note> notes = db.getNotesWithReminder(false);
			Log.d(Constants.TAG, "Found " + notes.size() + " reminders");
			for (Note note : notes) {
				setAlarm(ctx, note);
			}
		}

		// Release the lock
		finally {
//			wl.release();
		}

		return Service.START_NOT_STICKY;
	}

	private void setAlarm(Context ctx, Note note) {
		Intent intent = new Intent(ctx, AlarmReceiver.class);
		intent.putExtra(Constants.INTENT_NOTE, note);
		PendingIntent sender = PendingIntent.getBroadcast(ctx, Constants.INTENT_ALARM_CODE, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager) ctx.getSystemService(ctx.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, Long.parseLong(note.getAlarm()), sender);
	}

}

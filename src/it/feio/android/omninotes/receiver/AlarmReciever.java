package it.feio.android.omninotes.receiver;

import it.feio.android.omninotes.DetailActivity;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.ParcelableNote;
import it.feio.android.omninotes.utils.Constants;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class AlarmReciever extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent intent) {
		try {

			PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");
			// Acquire the lock
			wl.acquire();

			// You can do the processing here update the widget/remote views.
			Bundle extras = intent.getExtras();
			Note note = ((ParcelableNote)extras.getParcelable(Constants.INTENT_NOTE)).getNote();
			
			createNotification("boh", ctx, note);

			// Release the lock
			wl.release();

		} catch (Exception e) {
			Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_LONG).show();
		}

	}

//	private Calendar selectRepetitionDate(boolean[] weekRepetition) {
//		Calendar calendar = Calendar.getInstance();
//
//		// repeat message if is specified into weekRepetition
//		boolean mustRepeatAlarm = false;// , onlyOneTime = false;
//		int nextDay = (calendar.get(Calendar.DAY_OF_WEEK) + 1) % 8;
//		nextDay = nextDay == 0 ? 1 : nextDay;
//		int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
//
//		long dayToAdd = calendar.getTimeInMillis() + 86400000;
//		while (currentDay != nextDay && !mustRepeatAlarm) {
//			if (weekRepetition[nextDay]) {
//				mustRepeatAlarm = true;
//			} else {
//				nextDay = (nextDay + 1) % 8;
//				nextDay = nextDay == 0 ? 1 : nextDay;
//				dayToAdd += 86400000;
//			}
//		}
//
//		if (mustRepeatAlarm || weekRepetition[nextDay]) {
//			calendar.setTimeInMillis(dayToAdd);
//			return calendar;
//		}
//
//		return null;
//
//	}

//	private void sendRepetition(Context ctx, Calendar calendar, boolean[] confirmedSelection,
//			boolean[] weekRepetition) {
//
////		Toast.makeText(ctx, "next alarm is on date " + (new Date(calendar.getTimeInMillis())).toString(),
////				Toast.LENGTH_LONG).show();
//
//		Intent alarmIntent = new Intent(ctx, AlarmReciever.class);
//		alarmIntent.putExtra(Constants.SELECTION, confirmedSelection);
//		alarmIntent.putExtra(Constants.WEEK_SELECTION, weekRepetition);
//
//		PendingIntent sender = PendingIntent.getBroadcast(ctx, Constants.ALARM_CODE, alarmIntent,
//				PendingIntent.FLAG_CANCEL_CURRENT);
//		AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
//		am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
//	}

	// private void sendMessage(String message, Context ctx, int sectionIndex) {
	//
	// Intent intent = new Intent(ctx, AlarmActivity.class);
	//
	// // Next create the bundle and initialize it
	// Bundle bundle = new Bundle();
	//
	// // Add the parameters to bundle as
	// bundle.putString(MESSAGE, message);
	//
	// bundle.putInt(SECTION_INDEX, sectionIndex);
	//
	// // Add this bundle to the intent
	// intent.putExtras(bundle);
	// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	//
	// // Start next activity
	// ctx.startActivity(intent);
	// }

//	private String choosePhrase(Context ctx, int index) throws NoSuchFieldException {
//		Random r = new Random();
//		int limit = -1;
//		int phraseIndex = -1;
//		String phrase = "";
//		int resId = -1;
//		if (index == Constants.JOB) {
//			limit = Integer.parseInt(ctx.getString(R.string.job_limit));
//			phraseIndex = r.nextInt(limit);
//
//			resId = getResId("job_phrase" + phraseIndex, ctx, R.string.class);
//			phrase = ctx.getString(resId);
//		} else if (index == Constants.LOVE) {
//			limit = Integer.parseInt(ctx.getString(R.string.love_limit));
//			phraseIndex = r.nextInt(limit);
//
//			resId = getResId("love_phrase" + phraseIndex, ctx, R.string.class);
//			phrase = ctx.getString(resId);
//		} else if (index == Constants.HEALT) {
//			limit = Integer.parseInt(ctx.getString(R.string.health_limit));
//			phraseIndex = r.nextInt(limit);
//
//			resId = getResId("health_phrase" + phraseIndex, ctx, R.string.class);
//			phrase = ctx.getString(resId);
//		} else {
//			SharedPreferences settings = ctx.getSharedPreferences(Constants.MY_PREFERENCES,
//					Context.MODE_PRIVATE);
//			phrase = settings.getString(Constants.myString, "Cazzo");
//		}
//
//		return phrase;
//	}
//
//	private int chooseSection(boolean[] confirmedSelection) {
//		int randomIndex = -1;
//		List<Integer> activeSelection = new ArrayList<Integer>();
//		for (int i = 0; i < confirmedSelection.length; i++) {
//			if (confirmedSelection[i]) {
//				activeSelection.add(i);
//			}
//		}
//		Random r = new Random();
//		randomIndex = activeSelection.get(r.nextInt(activeSelection.size()));
//
//		return randomIndex;
//	}
//
//	private int getResId(String variableName, Context context, Class<?> c) {
//
//		try {
//			Field idField = R.string.class.getDeclaredField(variableName);
//			return idField.getInt(idField);
//		} catch (Exception e) {
//			e.printStackTrace();
//			return -1;
//		}
//	}


	private void createNotification(String message, Context ctx, Note note) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(ctx.getResources().getString(R.string.app_name))
//				.setContentText(message)
				.setAutoCancel(true);

		Intent intent = new Intent(ctx, DetailActivity.class);

		// Next create the bundle and initialize it
		Bundle bundle = new Bundle();

		// Add the parameters to bundle as
		bundle.putString(Constants.MESSAGE, message);
		bundle.putParcelable(Constants.INTENT_NOTE, new ParcelableNote(note));
		// Add this bundle to the intent
		intent.putExtras(bundle);
		// Sets the Activity to start in a new, empty task
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		// Creates the PendingIntent
		PendingIntent notifyIntent = PendingIntent.getActivity(ctx, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// Puts the PendingIntent into the notification builder
		mBuilder.setContentIntent(notifyIntent);
		// Notifications are issued by sending them to the
		// NotificationManager system service.
		NotificationManager mNotificationManager = (NotificationManager) ctx
				.getSystemService(Context.NOTIFICATION_SERVICE);
		// Builds an anonymous Notification object from the builder, and
		// passes it to the NotificationManager
		mNotificationManager.notify(0, mBuilder.build());
	}


}

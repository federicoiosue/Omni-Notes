package it.feio.android.omninotes.async;

import it.feio.android.omninotes.ListActivity;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.db.DbHelper;
import android.annotation.SuppressLint;
import android.content.Intent;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

public class ONDashClockExtension extends DashClockExtension {

	@SuppressLint("DefaultLocale")
	@Override
	protected void onUpdateData(int reason) {

		DbHelper db = new DbHelper(this);
		int notes = db.getAllNotes(false).size();
		int remindersTotal = db.getNotesWithReminder(true).size();
		int remindersNotExpired = db.getNotesWithReminder(false).size();
		int today = db.getTodayReminders().size();

		// Publish the extension data update.
		publishUpdate(new ExtensionData()
				.visible(true)
				.icon(R.drawable.ic_stat_notification_icon)
				.status(String.valueOf(notes))
				.expandedTitle(
						notes + " " + getString(R.string.notes).toLowerCase())
				.expandedBody(
						remindersNotExpired + " "
								+ getString(R.string.reminders) + ", "
								+ (remindersTotal - remindersNotExpired) + " "
								+ getString(R.string.expired)
								+ System.getProperty("line.separator") + today
								+ " " + getString(R.string.today))
				.clickIntent(new Intent(this, ListActivity.class)));
	}
}

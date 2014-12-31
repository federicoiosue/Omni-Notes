/*
 * Copyright (C) 2015 Federico Iosue (federico.iosue@gmail.com)
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

package it.feio.android.omninotes.async;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.utils.Constants;

public class ONDashClockExtension extends DashClockExtension {
	
	
	private DashClockUpdateReceiver mDashClockReceiver;



	@Override
	protected void onInitialize(boolean isReconnect) {
		super.onInitialize(isReconnect);

		LocalBroadcastManager broadcastMgr = LocalBroadcastManager.getInstance(this);
		if (mDashClockReceiver != null) {
			try {
				broadcastMgr.unregisterReceiver(mDashClockReceiver);
			} catch (Exception ignore) {
			}
		}
		mDashClockReceiver = new DashClockUpdateReceiver();
		broadcastMgr.registerReceiver(mDashClockReceiver, new IntentFilter(Constants.INTENT_UPDATE_DASHCLOCK));
	}
	
	

	@SuppressLint("DefaultLocale")
	@Override
	protected void onUpdateData(int reason) {

		DbHelper db = DbHelper.getInstance(this);
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
				.clickIntent(new Intent(this, MainActivity.class)));
	}

	
	
	public class DashClockUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			onUpdateData(UPDATE_REASON_MANUAL);
		}

	}
}

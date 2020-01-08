/*
 * Copyright (C) 2013-2020 Federico Iosue (federico@iosue.it)
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
package it.feio.android.omninotes.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import it.feio.android.omninotes.async.AlarmRestoreOnRebootService;
import it.feio.android.omninotes.helpers.LogDelegate;


public class BootCompleteReceiver extends BroadcastReceiver {

  @Override
  public void onReceive (Context ctx, Intent intent) {
    LogDelegate.i("System rebooted: refreshing reminders");
    if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
      AlarmRestoreOnRebootService.enqueueWork(ctx, intent);
    }
  }


}

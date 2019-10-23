/*
 * Copyright (C) 2013-2019 Federico Iosue (federico@iosue.it)
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

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import it.feio.android.omninotes.BaseActivity;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.helpers.LogDelegate;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.ReminderHelper;
import java.util.List;


public class AlarmRestoreOnRebootService extends JobIntentService {

  public static final int JOB_ID = 0x01;

  public static void enqueueWork (Context context, Intent work) {
    enqueueWork(context, AlarmRestoreOnRebootService.class, JOB_ID, work);
  }

  @Override
  protected void onHandleWork (@NonNull Intent intent) {
    LogDelegate.i("System rebooted: service refreshing reminders");
    Context mContext = getApplicationContext();

    BaseActivity.notifyAppWidgets(mContext);

    List<Note> notes = DbHelper.getInstance().getNotesWithReminderNotFired();
    LogDelegate.d("Found " + notes.size() + " reminders");
    for (Note note : notes) {
      ReminderHelper.addReminder(OmniNotes.getAppContext(), note);
    }
  }

}

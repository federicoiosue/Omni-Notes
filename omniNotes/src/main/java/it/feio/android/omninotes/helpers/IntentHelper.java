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

package it.feio.android.omninotes.helpers;

import static it.feio.android.omninotes.utils.ConstantsBase.INTENT_NOTE;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import it.feio.android.omninotes.models.Note;
import lombok.experimental.UtilityClass;

@UtilityClass
public class IntentHelper {

  public static Intent getNoteIntent(@NonNull Context context, @NonNull Class target, String action,
      Note note) {
    Intent intent = new Intent(context, target);
    intent.setAction(action);
    Bundle bundle = new Bundle();
    bundle.putParcelable(INTENT_NOTE, note);
    intent.putExtras(bundle);

//    // Sets the Activity to start in a new, empty task
//    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//    // Workaround to fix problems with multiple notifications
//    intent.setAction(ACTION_NOTIFICATION_CLICK + System.currentTimeMillis());

    return intent;
  }

  public static PendingIntent getNotePendingIntent(@NonNull Context context, @NonNull Class target,
      String action,
      Note note) {
    Intent intent = getNoteIntent(context, target, action, note);
    return PendingIntent.getActivity(context, getUniqueRequestCode(note), intent,
        PendingIntent.FLAG_UPDATE_CURRENT);
  }

  static int getUniqueRequestCode(Note note) {
    return note.get_id().intValue();
  }

}

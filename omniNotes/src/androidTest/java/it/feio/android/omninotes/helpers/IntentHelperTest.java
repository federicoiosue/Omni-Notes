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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Intent;
import it.feio.android.omninotes.BaseAndroidTestCase;
import it.feio.android.omninotes.SnoozeActivity;
import it.feio.android.omninotes.models.Note;
import java.util.Calendar;
import org.junit.Test;

public class IntentHelperTest extends BaseAndroidTestCase {

  private static final String ACTION = "DUMMY_ACTION";
  private static final String NOTE_TITLE = "Lorem ipsum is not that good";

  @Test
  public void checkUtilityClassWellDefined() throws Exception {
    assertUtilityClassWellDefined(IntentHelper.class);
  }

  @Test
  public void getNoteIntent() {
    Note note = new Note();
    note.setTitle(NOTE_TITLE);

    Intent intent = IntentHelper.getNoteIntent(testContext, SnoozeActivity.class, ACTION, note);

    assertEquals(ACTION, intent.getAction());
    assertTrue(intent.getParcelableExtra(INTENT_NOTE) instanceof Note);
    assertEquals(NOTE_TITLE, ((Note) intent.getParcelableExtra(INTENT_NOTE)).getTitle());
  }

  @Test
  public void getNotePendingIntent() {
    Note note = new Note();
    note.set_id(Calendar.getInstance().getTimeInMillis());
    note.setTitle(NOTE_TITLE);

    IntentHelper.getNotePendingIntent(testContext, SnoozeActivity.class, ACTION, note);
  }

  @Test
  public void getUniqueRequestCode() {
    final Long now = Calendar.getInstance().getTimeInMillis();
    Note note = new Note();
    note.set_id(now);

    int uniqueRequestCode = IntentHelper.getUniqueRequestCode(note);

    assertEquals(now.intValue(), uniqueRequestCode);
  }

}
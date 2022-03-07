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

package it.feio.android.omninotes;

import static org.junit.Assert.assertTrue;

import it.feio.android.omninotes.models.StatsSingleNote;
import org.junit.Test;

public class NoteInfosActivityTest {

  @Test
  public void getChecklistCompletionState() {
    StatsSingleNote infos = new StatsSingleNote();
    infos.setChecklistItemsNumber(42);
    infos.setChecklistCompletedItemsNumber(12);

    String completionState = NoteInfosActivity.getChecklistCompletionState(infos);

    assertTrue(completionState.contains(infos.getChecklistCompletedItemsNumber() + ""));
    assertTrue(completionState.contains("29%"));
  }
}
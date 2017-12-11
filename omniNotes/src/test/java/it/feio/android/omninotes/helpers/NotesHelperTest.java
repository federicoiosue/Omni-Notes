/*
 * Copyright (C) 2016 Federico Iosue (federico.iosue@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundatibehaon, either version 3 of the License, or
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

import org.junit.Test;

import it.feio.android.omninotes.models.Note;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class NotesHelperTest {

    @Test
    public void haveSameIdShouldFail() {
        Note note1 = getNote(1L, "test title", "test content");
        Note note2 = getNote(2L, "test title", "test content");
        assertFalse(NotesHelper.haveSameId(note1, note2));
    }

    @Test
    public void haveSameIdShouldSucceed() {
        Note note1 = getNote(3L, "test title", "test content");
        Note note2 = getNote(3L, "different test title", "different test content");
        assertTrue(NotesHelper.haveSameId(note1, note2));
    }

    private Note getNote(Long id, String title, String content) {
        Note note = new Note();
        note.set_id(id);
        note.setTitle(title);
        note.setContent(content);
        return note;
    }
}

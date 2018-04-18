/*
 * Copyright (C) 2018 Federico Iosue (federico.iosue@gmail.com)
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

package it.feio.android.omninotes.async.notes;

import java.util.List;

import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.Note;


public class NoteProcessorCategorize extends NoteProcessor {

    Category category;


    public NoteProcessorCategorize(List<Note> notes, Category category) {
        super(notes);
        this.category = category;
    }


    @Override
    protected void processNote(Note note) {
        note.setCategory(category);
        DbHelper.getInstance().updateNote(note, false);
    }
}

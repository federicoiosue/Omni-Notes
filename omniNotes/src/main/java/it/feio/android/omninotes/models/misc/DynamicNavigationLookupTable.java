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

package it.feio.android.omninotes.models.misc;


import android.content.Context;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Note;

import java.util.HashMap;
import java.util.List;


public class DynamicNavigationLookupTable {

    HashMap<String, Integer> hashMap = new HashMap<>();
    int archived;
    int trashed;
    int uncategorized;
    int reminders;


    public void init(Context context) {
        List<Note> notes = DbHelper.getInstance(context).getAllNotes(false);
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).isTrashed()) trashed++;
            else if (notes.get(i).isArchived()) archived++;
            else if (notes.get(i).getAlarm() != null) reminders++;
            if (notes.get(i).getCategory() != null) uncategorized++;
        }
    }


    public int getArchived() {
        return archived;
    }


    public int getTrashed() {
        return trashed;
    }


    public int getReminders() {
        return reminders;
    }


    public int getUncategorized() {
        return uncategorized;
    }

}
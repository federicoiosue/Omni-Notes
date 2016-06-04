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

package it.feio.android.omninotes.db;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import it.feio.android.omninotes.models.Note;


public class DbHelperTest extends AndroidTestCase {

	private RenamingDelegatingContext context;
	private DbHelper dbHelper;


	@Override
	public void setUp() throws Exception {
		super.setUp();
		context = new RenamingDelegatingContext(getContext(), "test_");
		dbHelper = DbHelper.getInstance(context);
	}


	@Override
	protected void tearDown() throws Exception {
		context.deleteDatabase(dbHelper.getDatabaseName());
		super.tearDown();
	}


	public void testGetNotesByTag() {
		Note note = new Note();
		note.setTitle("title with #tag inside");
		note.setContent("useless content");
		dbHelper.updateNote(note, true);
		Note note1 = new Note();
		note1.setTitle("simple title");
		note1.setContent("content with #tag");
		dbHelper.updateNote(note1, true);
		Note note2 = new Note();
		note2.setTitle("title without tags in it");
		note2.setContent("some \n #tagged content");
		dbHelper.updateNote(note2, true);
		assertEquals(dbHelper.getNotesByTag("#tag").size(), 2);
		assertEquals(dbHelper.getNotesByTag("#tagged").size(), 1);
	}
}

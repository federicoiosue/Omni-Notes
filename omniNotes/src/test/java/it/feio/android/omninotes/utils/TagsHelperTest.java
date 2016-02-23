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

package it.feio.android.omninotes.utils;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.TagsHelper;

import java.util.HashMap;


public class TagsHelperTest extends AndroidTestCase {

	private RenamingDelegatingContext context;


	@Override
	public void setUp() throws Exception {
		super.setUp();
		context = new RenamingDelegatingContext(getContext(), "test_");
	}


	@Override
	protected void tearDown() throws Exception {
		context.deleteDatabase(DbHelper.getInstance().getDatabaseName());
		super.tearDown();
	}


	public void testGetAllTripDeliveries() {
		Note note = new Note();
		note.setContent("Random content with #mixed #123numbered #tags");
		HashMap<String, Integer> tags = TagsHelper.retrieveTags(note);
		assertTrue(tags.containsKey("#mixed"));
		assertTrue(tags.containsKey("#123numbered"));
		assertTrue(tags.containsKey("#tags"));
		assertFalse(tags.containsKey("#nonExistingTag"));
	}
}

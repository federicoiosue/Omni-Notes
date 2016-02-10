/*
 * Copyright (C) 2016 Federico Iosue (federico.iosue@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundatibehaon, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package it.feio.android.omninotes.models;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;


public class NoteTest {

	private Note note1;
	private Note note2;


	@Before
	public void setUp() {
		note1 = new Note();
		note1.setTitle("new title");
		note1.setContent("some random content");
		note1.setCreation(Calendar.getInstance().getTimeInMillis() - 10000);
		note1.setLastModification(Calendar.getInstance().getTimeInMillis() - 10000);
		note1.setLocked(true);

		note2 = new Note();
		note2.setTitle("another title");
		note2.setContent("some more random different content");
		note2.setCreation(Calendar.getInstance().getTimeInMillis());
		note2.setLastModification(Calendar.getInstance().getTimeInMillis());
		note2.setCategory(new Category());
	}


	@Test
	public void equivalence() {
		Note note3 = new Note(note1);
		assertEquals(note1, note3);
		note3.setContent(note1.getContent());
		assertEquals(note1, note3);
	}


	@Test
	public void difference() {
		assertNotEquals(note1, note2);
	}


	@Test
	public void listContainsNote() {
		List<Note> notes = new ArrayList<>();
		notes.add(note1);
		notes.add(note2);
		assertTrue(notes.contains(note1));
		assertTrue(notes.contains(note2));
		assertFalse(notes.contains(new Note()));
	}
}

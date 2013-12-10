/*******************************************************************************
 * Copyright 2013 Federico Iosue (federico.iosue@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package it.feio.android.omninotes.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.joda.time.DateTime;

import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.AssetUtils;
import it.feio.android.omninotes.utils.Constants;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {

	// Database name
	private static final String DATABASE_NAME = "omni-notes";
	// Database version aligned if possible to software version
	private static final int DATABASE_VERSION = 391;
	// Sql query file directory
    private static final String SQL_DIR = "sql" ;
	// Notes table name
	private static final String TABLE_NOTES = "notes";
	// Notes table columns
	private static final String KEY_ID = "id";
	public static final String KEY_CREATION = "creation";
	public static final String KEY_LAST_MODIFICATION = "last_modification";
	public static final String KEY_TITLE = "title";
	private static final String KEY_CONTENT = "content";
	private static final String KEY_ARCHIVED = "archived";
	private static final String KEY_ALARM = "alarm";
	private static final String KEY_LATITUDE = "latitude";
	private static final String KEY_LONGITUDE = "longitude";
	// Attachments table name
	private static final String TABLE_ATTACHMENTS = "attachments";
	// Attachments table columns
	private static final String KEY_ATTACHMENT_ID = "id"; 
	private static final String KEY_ATTACHMENT_URI = "uri"; 
	private static final String KEY_ATTACHMENT_NOTE_ID = "note_id"; 
	// Queries    
    private static final String CREATE_QUERY = "create.sql";
    private static final String UPGRADE_QUERY_PREFIX = "upgrade-";    
    private static final String UPGRADE_QUERY_SUFFIX = ".sql";


	private final Context ctx;

	public DbHelper(Context ctx) {
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
		this.ctx = ctx;
	}

	
	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
            Log.i(Constants.TAG, "Database creation");
            execSqlFile(CREATE_QUERY, db);
        } catch( IOException exception ) {
            throw new RuntimeException("Database creation failed", exception);
        }
	}

	
	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(Constants.TAG, "Upgrading database version from " + oldVersion + " to " + newVersion );
        try {
            for( String sqlFile : AssetUtils.list(SQL_DIR, ctx.getAssets())) {
                if ( sqlFile.startsWith(UPGRADE_QUERY_PREFIX)) {
                    int fileVersion = Integer.parseInt(sqlFile.substring(UPGRADE_QUERY_PREFIX.length(),  sqlFile.length() - UPGRADE_QUERY_SUFFIX.length())); 
                    if ( fileVersion > oldVersion && fileVersion <= newVersion ) {
                        execSqlFile( sqlFile, db );
                    }
                }
            }
            Log.i(Constants.TAG, "Database upgrade successful");
        } catch( IOException exception ) {
            throw new RuntimeException("Database upgrade failed", exception );
        }
	}
	
	
	protected void execSqlFile(String sqlFile, SQLiteDatabase db ) throws SQLException, IOException {
        Log.i(Constants.TAG, "  exec sql file: {}" + sqlFile );
        for( String sqlInstruction : SqlParser.parseSqlFile( SQL_DIR + "/" + sqlFile, ctx.getAssets())) {
        	Log.v(Constants.TAG, "    sql: {}" + sqlInstruction );
        	try {
        		db.execSQL(sqlInstruction);
        	} catch (Exception e) {
        		Log.e(Constants.TAG, "Error creating table: " + sqlInstruction);
        	}
        }
    }

	
	// Inserting or updating single note
	public Note updateNote(Note note) {
		
		long resNote, resAttachment;
		SQLiteDatabase db = this.getWritableDatabase();
		
		// To ensure note and attachments insertions are atomical and boost performances transaction are used
		db.beginTransaction();

		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, note.getTitle());
		values.put(KEY_CONTENT, note.getContent());
		values.put(KEY_LAST_MODIFICATION, Calendar.getInstance().getTimeInMillis());
		boolean archive = note.isArchived() != null ? note.isArchived() : false;
		values.put(KEY_ARCHIVED, archive);
		values.put(KEY_ALARM, note.getAlarm());
		values.put(KEY_LATITUDE, note.getLatitude());
		values.put(KEY_LONGITUDE, note.getLongitude());

		// Updating row
		if (note.get_id() != 0) {
			values.put(KEY_ID, note.get_id());
			resNote = db.update(TABLE_NOTES, values, KEY_ID + " = ?",
					new String[] { String.valueOf(note.get_id()) });
			// Importing data from csv without existing note in db
			if (resNote == 0) {
				resNote = db.insert(TABLE_NOTES, null, values);
			}
			Log.d(Constants.TAG, "Updated note titled '" + note.getTitle()
					+ "'");
			// Inserting new note
		} else {
			values.put(KEY_CREATION, Calendar.getInstance().getTimeInMillis());
			resNote = db.insert(TABLE_NOTES, null, values);
			Log.d(Constants.TAG, "Saved new note titled '" + note.getTitle()
					+ "' with id: " + resNote);
		}
		
		// Updating attachments
		ContentValues valuesAttachments = new ContentValues();
		ArrayList<Attachment> deletedAttachments = note.getAttachmentsListOld();
		for (Attachment attachment : note.getAttachmentsList()) {
			// Updating attachment
			if (attachment.getId() == 0) {
				valuesAttachments.put(KEY_ATTACHMENT_URI, attachment.getUri().toString());
				valuesAttachments.put(KEY_ATTACHMENT_NOTE_ID, (note.get_id() != 0 ? note.get_id() : resNote) );
				resAttachment = db.insert(TABLE_ATTACHMENTS, null, valuesAttachments);
				Log.d(Constants.TAG, "Saved new attachment with uri '"
						+ attachment.getUri().toString() + "' with id: " + resAttachment);
			} else {
				deletedAttachments.remove(attachment);
			}
		}
		// Remove from database deleted attachments
		for (Attachment attachmentDeleted : deletedAttachments) {
			db.delete(TABLE_ATTACHMENTS, KEY_ATTACHMENT_ID + " = ?",
					new String[] { String.valueOf(attachmentDeleted.getId()) });
		}
		
		db.setTransactionSuccessful();
		db.endTransaction();
		
		db.close();

		// Fill the note with correct data before returning it
		note.set_id(note.get_id() != 0 ? note.get_id() : (int)resNote);
		note.setCreation(note.getCreation() != null ? note.getCreation() : values.getAsLong(KEY_CREATION));
		note.setLastModification(values.getAsLong(KEY_LAST_MODIFICATION));	
		
		return note;
		// new UpdateNoteAsync(this).execute(note);
	}

	
	
	
	/**
	 * Getting single note
	 * @param id
	 * @return
	 */
	public Note getNote(int id) {
		SQLiteDatabase db = getReadableDatabase();

		Cursor cursor = db.query(TABLE_NOTES, new String[] { KEY_ID,
				KEY_CREATION, KEY_LAST_MODIFICATION, KEY_TITLE, KEY_CONTENT,
				KEY_ARCHIVED, KEY_ALARM, KEY_LATITUDE, KEY_LONGITUDE }, KEY_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		Note note = new Note(Integer.parseInt(cursor.getString(0)),
				cursor.getLong(1), cursor.getLong(2), cursor.getString(3),
				cursor.getString(4), cursor.getInt(5), cursor.getString(6),
				cursor.getString(7), cursor.getString(8));
		
		// Add eventual attachments uri
		note.setAttachmentsList(getNoteAttachments(note));
		
		db.close();
		return note;
	}

	
	
	/**
	 * Getting All notes
	 * 
	 * @param checkNavigation
	 *            Tells if navigation status (notes, archived) must be kept in
	 *            consideration or if all notes have to be retrieved
	 * @return Notes list
	 */
	public List<Note> getAllNotes(boolean checkNavigation) {

		// Getting sorting criteria from preferences
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		String sort_column = prefs.getString(Constants.PREF_SORTING_COLUMN,
				KEY_TITLE);
		String sort_order = KEY_TITLE.equals(sort_column) ? " ASC " : " DESC ";

		// Checking if archived notes must be shown
		boolean archived = "1".equals(prefs.getString(
				Constants.PREF_NAVIGATION, "0"));
		String whereCondition = checkNavigation ? " WHERE " + KEY_ARCHIVED
				+ (archived ? " = 1 " : " = 0 ") : "";

		// Select All Query
		String selectQuery = "SELECT * FROM " + TABLE_NOTES + whereCondition
				+ " ORDER BY " + sort_column + sort_order;
		Log.d(Constants.TAG, "Select notes query: " + selectQuery);

		return getNotes(selectQuery);

		// new GetAllNotesAsync(ctx, this, mAdapter).execute(checkNavigation);
	}
	
	
	
	/**
	 * Common method for notes retrieval. It accepts a query to perform and returns matching records.
	 * @param query
	 * @return Notes list
	 */
	private List<Note> getNotes(String query) {
		List<Note> noteList = new ArrayList<Note>();
		Log.d(Constants.TAG, "Select notes query: " + query);

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		// Looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				Note note = new Note();
				note.set_id(Integer.parseInt(cursor.getString(0)));
				note.setCreation(cursor.getLong(1));
				note.setLastModification(cursor.getLong(2));
				note.setTitle(cursor.getString(3));
				note.setContent(cursor.getString(4));
				note.setArchived("1".equals(cursor.getString(5)));
				note.setAlarm(cursor.getString(6));		
				note.setLatitude(cursor.getString(7));
				note.setLongitude(cursor.getString(8));
				// Add eventual attachments uri
				note.setAttachmentsList(getNoteAttachments(note));
				// Adding note to list
				noteList.add(note);
			} while (cursor.moveToNext());
		}

		cursor.close();
		db.close();

		return noteList;
	}
	
	


	/**
	 * Getting notes count
	 * @return
	 */
	public int getNotesCount() {
		String countQuery = "SELECT * FROM " + TABLE_NOTES;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();
		db.close();
		return cursor.getCount();
	}

	
	
	/**
	 * Deleting single note
	 * @param note
	 */
	public void deleteNote(Note note) {
		SQLiteDatabase db = this.getWritableDatabase();
		// Delete notes
		db.delete(TABLE_NOTES, KEY_ID + " = ?",
				new String[] { String.valueOf(note.get_id()) });
		// Delete note's attachments
		db.delete(TABLE_ATTACHMENTS, KEY_ATTACHMENT_NOTE_ID + " = ?",
				new String[] { String.valueOf(note.get_id()) });
		db.close();
	}

	
	

	/**
	 * Clears completelly the database
	 */
	public void clear() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DELETE FROM " + TABLE_NOTES);
		db.close();
	}

	
	
	/**
	 * Getting notes matching pattern with title or content text
	 * 
	 * @param checkNavigation
	 *            Tells if navigation status (notes, archived) must be kept in
	 *            consideration or if all notes have to be retrieved
	 * @return Notes list
	 */
	public List<Note> getMatchingNotes(String pattern) {
		
		// Getting sorting criteria from preferences
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		String sort_column = prefs.getString(Constants.PREF_SORTING_COLUMN,
				KEY_TITLE);

		// Select All Query
		String selectQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE "
				+ KEY_TITLE + " LIKE '%" + pattern + "%' " + " OR "
				+ KEY_CONTENT + " LIKE '%" + pattern + "%' " + " ORDER BY "
				+ sort_column;
		
		return getNotes(selectQuery);
	}
	
	
	
	
	
	/**
	 * Search for notes with reminder
	 * @param passed Search also for fired yet reminders
	 * @return Notes list
	 */
	public List<Note> getNotesWithReminder(boolean passed) {

		// Select query
		String selectQuery = "SELECT * FROM " + TABLE_NOTES 
							+ " WHERE " + KEY_ALARM 
							+ (passed ? " IS NOT NULL" : " >= " + DateTime.now().getMillis());
				

		return getNotes(selectQuery);
	}


	
	/**
	 * Retrieves all attachments related to specifi note
	 * @param note
	 * @return List of attachments
	 */
	public ArrayList<Attachment> getNoteAttachments(Note note) {
		
		ArrayList<Attachment> attachmentsList = new ArrayList<Attachment>();
		String sql = "SELECT " + KEY_ATTACHMENT_ID + "," + KEY_ATTACHMENT_URI + " FROM " + TABLE_ATTACHMENTS + " WHERE " + KEY_ATTACHMENT_NOTE_ID + " = " + note.get_id();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(sql, null);

		// Looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				attachmentsList.add(new Attachment(Integer.valueOf(cursor.getInt(0)), Uri.parse(cursor.getString(1))));
			} while (cursor.moveToNext());
		}
		return attachmentsList;		
	}


}

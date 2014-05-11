/*******************************************************************************
 * Copyright 2014 Federico Iosue (federico.iosue@gmail.com)
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

import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.AssetUtils;
import it.feio.android.omninotes.utils.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {

	// Database name
	private static final String DATABASE_NAME = Constants.DATABASE_NAME;
	// Database version aligned if possible to software version
	private static final int DATABASE_VERSION = 452;
	// Sql query file directory
    private static final String SQL_DIR = "sql" ;
    
	// Notes table name
    public static final String TABLE_NOTES = "notes";
	// Notes table columns
	public static final String KEY_ID = "note_id";
	public static final String KEY_CREATION = "creation";
	public static final String KEY_LAST_MODIFICATION = "last_modification";
	public static final String KEY_TITLE = "title";
	public static final String KEY_CONTENT = "content";
	public static final String KEY_ARCHIVED = "archived";
	public static final String KEY_TRASHED = "trashed";
	public static final String KEY_ALARM = "alarm";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_CATEGORY = "category_id";
	public static final String KEY_LOCKED = "locked";
	public static final String KEY_CHECKLIST = "checklist";
	
	// Attachments table name
	public static final String TABLE_ATTACHMENTS = "attachments";
	// Attachments table columns
	public static final String KEY_ATTACHMENT_ID = "attachment_id"; 
	public static final String KEY_ATTACHMENT_URI = "uri"; 
	public static final String KEY_ATTACHMENT_NAME = "name"; 
	public static final String KEY_ATTACHMENT_SIZE = "size"; 
	public static final String KEY_ATTACHMENT_LENGTH = "length"; 
	public static final String KEY_ATTACHMENT_MIME_TYPE = "mime_type"; 
	public static final String KEY_ATTACHMENT_NOTE_ID = "note_id"; 	

	// Categories table name
	public static final String TABLE_CATEGORY = "categories";
	// Categories table columns
	public static final String KEY_CATEGORY_ID = "category_id"; 
	public static final String KEY_CATEGORY_NAME = "name"; 
	public static final String KEY_CATEGORY_DESCRIPTION = "description"; 
	public static final String KEY_CATEGORY_COLOR = "color"; 
	
	// Queries    
    private static final String CREATE_QUERY = "create.sql";
    private static final String UPGRADE_QUERY_PREFIX = "upgrade-";    
    private static final String UPGRADE_QUERY_SUFFIX = ".sql";


	private final Context ctx;
	private final SharedPreferences prefs;

	public DbHelper(Context ctx) {
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
		this.ctx = ctx;
		this.prefs = ctx.getSharedPreferences(Constants.PREFS_NAME, ctx.MODE_MULTI_PROCESS);
	}
	
	public String getDatabaseName() {
		return DATABASE_NAME;
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
        		Log.e(Constants.TAG, "Error executing command: " + sqlInstruction, e);
        	}
        }
    }

	
	// Inserting or updating single note
	public Note updateNote(Note note, boolean updateLastModification) {
		
		long resNote, resAttachment;
		SQLiteDatabase db = this.getWritableDatabase();
		
		// To ensure note and attachments insertions are atomical and boost performances transaction are used
		db.beginTransaction();

		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, note.getTitle());
		values.put(KEY_CONTENT, note.getContent());
		values.put(KEY_LAST_MODIFICATION, updateLastModification ? Calendar
				.getInstance().getTimeInMillis() : note.getLastModification());
//		boolean archive = note.isArchived() != null ? note.isArchived() : false;
//		values.put(KEY_ARCHIVED, archive);
		values.put(KEY_ARCHIVED, note.isArchived());
		values.put(KEY_TRASHED, note.isTrashed());
		values.put(KEY_ALARM, note.getAlarm());
		values.put(KEY_LATITUDE, note.getLatitude());
		values.put(KEY_LONGITUDE, note.getLongitude());
		values.put(KEY_CATEGORY, note.getCategory() != null ? note.getCategory().getId() : null);
		boolean locked = note.isLocked() != null ? note.isLocked() : false;
		values.put(KEY_LOCKED, locked);
		boolean checklist = note.isChecklist() != null ? note.isChecklist() : false;
		values.put(KEY_CHECKLIST, checklist);

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
				valuesAttachments.put(KEY_ATTACHMENT_MIME_TYPE, attachment.getMime_type());
				valuesAttachments.put(KEY_ATTACHMENT_NOTE_ID, (note.get_id() != 0 ? note.get_id() : resNote) );
				valuesAttachments.put(KEY_ATTACHMENT_NAME, attachment.getName());
				valuesAttachments.put(KEY_ATTACHMENT_SIZE, attachment.getSize());
				valuesAttachments.put(KEY_ATTACHMENT_LENGTH, attachment.getLength());
				attachment.setId((int) db.insert(TABLE_ATTACHMENTS, null, valuesAttachments));
//				Log.d(Constants.TAG, "Saved new attachment with uri '"
//						+ attachment.getUri().toString() + "' with id: " + attachment.getId());
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
	}

	
	
	
	/**
	 * Getting single note
	 * @param id
	 * @return
	 */
	public Note getNote(int id) {

		String whereCondition = " WHERE "
								+ KEY_ID + " = " + id;
		
		List<Note> notes = getNotes(whereCondition, true);
		Note note;
		if (notes.size() > 0) {
			note = notes.get(0);
		} else {
			note = null;
		}
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
	public List<Note> getAllNotes(Boolean checkNavigation) {

		// Checking if archived or reminders notes must be shown
		String[] navigationListCodes = ctx.getResources().getStringArray(R.array.navigation_list_codes);
		String navigation = prefs.getString(Constants.PREF_NAVIGATION, navigationListCodes[0]);
		String whereCondition = "";
		boolean notes = navigationListCodes[0].equals(navigation);
		boolean archived = navigationListCodes[1].equals(navigation);
		boolean reminders = navigationListCodes[2].equals(navigation);
		boolean trashed = navigationListCodes[3].equals(navigation);
		boolean category = !notes && ! archived && !reminders && !trashed;		
		if (checkNavigation) {
			whereCondition = notes ? " WHERE " + KEY_ARCHIVED + " IS NOT 1 AND " + KEY_TRASHED + " IS NOT 1 " : whereCondition;			
			whereCondition = archived ? " WHERE " + KEY_ARCHIVED + " = 1 " : whereCondition;
			whereCondition = reminders ? " WHERE " + KEY_ALARM + " != 0 " : whereCondition;	
			whereCondition = trashed ? " WHERE " + KEY_TRASHED + " = 1 " : whereCondition;
			whereCondition = category ? " WHERE " + TABLE_NOTES + "." + KEY_CATEGORY + " = " + navigation : whereCondition;
		}		
		return getNotes(whereCondition, true);
	}
	
	
	
	/**
	 * Common method for notes retrieval. It accepts a query to perform and returns matching records.
	 * @param query
	 * @return Notes list
	 */
	public List<Note> getNotes(String whereCondition, boolean order) {
		List<Note> noteList = new ArrayList<Note>();

		// Getting sorting criteria from preferences
		String sort_column = "", sort_order = "";
		sort_column = prefs.getString(Constants.PREF_SORTING_COLUMN,
				KEY_TITLE);
		if (order) {			
			sort_order = KEY_TITLE.equals(sort_column) || KEY_ALARM.equals(sort_column) ? " ASC " : " DESC ";
		}

		// In case of title sorting criteria it must be handled empty title by concatenating content
		sort_column = KEY_TITLE.equals(sort_column) ? KEY_TITLE + "||" + KEY_CONTENT : sort_column;
		
		// In case of reminder sorting criteria the empty reminder notes must be moved on bottom of results
		sort_column = KEY_ALARM.equals(sort_column) ? "IFNULL(" + KEY_ALARM + ", " + Constants.TIMESTAMP_NEVER + ")" : sort_column;
		
		// Generic query to be specialized with conditions passed as parameter
		String query = "SELECT " 
						+ KEY_ID + "," 
						+ KEY_CREATION + "," 
						+ KEY_LAST_MODIFICATION + "," 
						+ KEY_TITLE + "," 
						+ KEY_CONTENT + "," 
						+ KEY_ARCHIVED + "," 
						+ KEY_TRASHED + "," 
						+ KEY_ALARM + "," 
						+ KEY_LATITUDE + "," 
						+ KEY_LONGITUDE + "," 
						+ KEY_LOCKED + "," 
						+ KEY_CHECKLIST + "," 
						+ KEY_CATEGORY + "," 
						+ KEY_CATEGORY_NAME + "," 
						+ KEY_CATEGORY_DESCRIPTION + "," 
						+ KEY_CATEGORY_COLOR 
					+ " FROM " + TABLE_NOTES 
					+ " LEFT JOIN " + TABLE_CATEGORY + " USING( " + KEY_CATEGORY + ") "						
					+ whereCondition
					+ (order ? " ORDER BY " + sort_column + sort_order : "");

		Log.d(Constants.TAG, "Query: " + query);

		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = this.getReadableDatabase();
			cursor = db.rawQuery(query, null);
	
			// Looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				do {
					int i = 0;
					Note note = new Note();
					note.set_id(Integer.parseInt(cursor.getString(i++)));
					note.setCreation(cursor.getLong(i++));
					note.setLastModification(cursor.getLong(i++));
					note.setTitle(cursor.getString(i++));
					note.setContent(cursor.getString(i++));
					note.setArchived("1".equals(cursor.getString(i++)));
					note.setTrashed("1".equals(cursor.getString(i++)));
					note.setAlarm(cursor.getString(i++));		
					note.setLatitude(cursor.getString(i++));
					note.setLongitude(cursor.getString(i++));
					note.setLocked("1".equals(cursor.getString(i++)));
					note.setChecklist("1".equals(cursor.getString(i++)));
					
					// Set category
					Category category = new Category(cursor.getInt(i++), cursor.getString(i++), cursor.getString(i++), cursor.getString(i++));
					note.setCategory(category);
					
					// Add eventual attachments uri
					note.setAttachmentsList(getNoteAttachments(note));
					
					// Adding note to list
					noteList.add(note);
					
				} while (cursor.moveToNext());
			}
			
		} finally {
			if (cursor != null)
				cursor.close();
			if (db != null)
				db.close();	
		}

		return noteList;
	}
	
	


	/**
	 * Getting notes count
	 * @return
	 */
	public int getNotesCount() {
		int count = 0;
		String countQuery = "SELECT * FROM " + TABLE_NOTES;

		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = this.getReadableDatabase();
			cursor = db.rawQuery(countQuery, null);
			count = cursor.getCount();
		} finally {
			if (cursor != null)
				cursor.close();
			if (db != null)
				db.close();
		}
		return count;
	}

	
	
	/**
	 * Trashes single note
	 * @param note
	 */
	public void trashNote(Note note) {
		note.setTrashed(true);
		updateNote(note, false);
	}
	
	
	/**
	 * Trashes single note
	 * @param note
	 */
	public void untrashNote(Note note) {
		note.setTrashed(false);
		updateNote(note, false);
	}

	
	
	/**
	 * Deleting single note
	 * @param note
	 */
	public boolean deleteNote(Note note) {
		int deletedNotes, deletedAttachments;

		SQLiteDatabase db = null;
		boolean result;
		try {
			db = this.getWritableDatabase();

			// Delete notes
			deletedNotes = db.delete(TABLE_NOTES, KEY_ID + " = ?",
					new String[] { String.valueOf(note.get_id()) });

			// Delete note's attachments
			deletedAttachments = db.delete(TABLE_ATTACHMENTS,
					KEY_ATTACHMENT_NOTE_ID + " = ?",
					new String[] { String.valueOf(note.get_id()) });

			// Check on correct and complete deletion
			result = deletedNotes == 1
					&& deletedAttachments == note.getAttachmentsList().size();

		} finally {
			if (db != null)
				db.close();
		}

		return result;
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
		// Select All Query
		String whereCondition = " WHERE "
								+ KEY_TITLE + " LIKE '%" + pattern + "%' " + " OR "
								+ KEY_CONTENT + " LIKE '%" + pattern + "%' ";
		return getNotes(whereCondition, true);
	}
	
	
	
	/**
	 * Search for notes with reminder
	 * @param passed Search also for fired yet reminders
	 * @return Notes list
	 */
	public List<Note> getNotesWithReminder(boolean passed) {
		// Select query
		String whereCondition = " WHERE " + KEY_ALARM 
								+ (passed ? " IS NOT NULL" : " >= " + Calendar.getInstance().getTimeInMillis());
		return getNotes(whereCondition, false);
	}
	
	
	
	/**
	 * Search for notes with reminder expiring the current day
	 * @return Notes list
	 */
	public List<Note> getTodayReminders() {
		// Select query
		String whereCondition = " WHERE DATE(" + KEY_ALARM + "/1000, 'unixepoch') = DATE('now')";
		return getNotes(whereCondition, false);
	}


	
	/**
	 * Retrieves all attachments related to specific note
	 * @param note
	 * @return List of attachments
	 */
	public ArrayList<Attachment> getNoteAttachments(Note note) {		
		String whereCondition = " WHERE " + KEY_ATTACHMENT_NOTE_ID + " = " + note.get_id();
		return getAttachments(whereCondition);
	}


	
	/**
	 * Retrieves all notes related to Category it passed as parameter
	 * @param categoryId Category integer identifier
	 * @return List of notes with requested category
	 */
	public List<Note> getNotesByCategory(String categoryId) {	
		List<Note> notes;
		try {
			int id = Integer.parseInt(categoryId);
			String whereCondition = " WHERE " + KEY_CATEGORY_ID + " = " + id;
			notes = getNotes(whereCondition, true);
		} catch (NumberFormatException e) {
			notes = getAllNotes(true);
		}		
		return notes;
	}

	
	
	
	/**
	 * Retrieves all notes related to category it passed as parameter
	 * @param categoryId Category integer identifier
	 * @return List of notes with requested category
	 */
	public List<String> getTags() {	
		HashMap<String, Boolean> tagsMap = new HashMap<String, Boolean>();
		
		String whereCondition = " WHERE "
								+ KEY_CONTENT + " LIKE '%#%' ";
		List<Note> notes = getNotes(whereCondition, true);
		
		Pattern pattern = Pattern.compile("(#[a-zA-Z0-9_-]+)");		
		for (Note note : notes) {
			Matcher matcher = pattern.matcher(note.getContent());
		    while (matcher.find()) {
		    	tagsMap.put(matcher.group(), true);
		    }
		}
		List<String> tags = new ArrayList<String>();
		tags.addAll(tagsMap.keySet());
		Collections.sort(tags);
		return tags;
	}
	
	
	/**
	 * Retrieves all notes related to category it passed as parameter
	 * @param categoryId Category integer identifier
	 * @return List of notes with requested category
	 */
	public List<Note> getNotesByTag(String tag) {	
		ArrayList<String> tags = new ArrayList<String>();
		if (tag.indexOf(",") != -1) {
			return getNotesByTag(tag.split(","));
		} else {
			return getNotesByTag(new String[]{tag} );			
		}
	}
	
	/**
	 * Retrieves all notes related to category it passed as parameter
	 * @param categoryId Category integer identifier
	 * @return List of notes with requested category
	 */
	public List<Note> getNotesByTag(String[] tags) {	
		// Select All Query
		StringBuilder whereCondition  = new StringBuilder();
		whereCondition.append(" WHERE " + KEY_CONTENT);		
		for (int i =0;i<tags.length;i++) {
			if (i!=0) {
				whereCondition.append(" OR " + KEY_CONTENT);
			}
			whereCondition.append(" LIKE '%" + tags[i] + "%' ");
		}
		return getNotes(whereCondition.toString(), true);
	}
	
	


	
	/**
	 * Retrieves all attachments
	 * @param note
	 * @return List of attachments
	 */
	public ArrayList<Attachment> getAllAttachments() {		
		return getAttachments("");
	}


	
	/**
	 * Retrieves attachments using a condition passed as parameter
	 * @param note
	 * @return List of attachments
	 */
	public ArrayList<Attachment> getAttachments(String whereCondition) {
		
		ArrayList<Attachment> attachmentsList = new ArrayList<Attachment>();
		String sql = "SELECT " 
						+ KEY_ATTACHMENT_ID + "," 
						+ KEY_ATTACHMENT_URI + ","
						+ KEY_ATTACHMENT_NAME + ","
						+ KEY_ATTACHMENT_SIZE + ","
						+ KEY_ATTACHMENT_LENGTH + ","
						+ KEY_ATTACHMENT_MIME_TYPE
					+ " FROM " + TABLE_ATTACHMENTS
					+ whereCondition;
		SQLiteDatabase db = null;
		Cursor cursor = null;

		try {

			db = this.getReadableDatabase();
			cursor = db.rawQuery(sql, null);

			// Looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				Attachment mAttachment;
				do {
					mAttachment = new Attachment(Integer.valueOf(cursor.getInt(0)),
							Uri.parse(cursor.getString(1)), cursor.getString(2), Integer.valueOf(cursor.getInt(3)),
							Long.valueOf(cursor.getInt(4)), cursor.getString(5));
//					mAttachment.setMoveWhenNoteSaved(false);
					attachmentsList.add(mAttachment);
				} while (cursor.moveToNext());
			}

		} finally {
			if (cursor != null)
				cursor.close();
			if (db != null)
				db.close();
		}
		return attachmentsList;
	}
	

	/**
	 * Retrieves categories list from database
	 * @return List of categories
	 */
	public ArrayList<Category> getCategories() {		
		ArrayList<Category> categoriesList = new ArrayList<Category>();
		String sql = "SELECT " 
						+ KEY_CATEGORY_ID + "," 
						+ KEY_CATEGORY_NAME + ","
						+ KEY_CATEGORY_DESCRIPTION  + ","
						+ KEY_CATEGORY_COLOR
					+ " FROM " + TABLE_CATEGORY
					+ " ORDER BY IFNULL(NULLIF(" + KEY_CATEGORY_NAME + ", ''),'zzzzzzzz') ";
		
		SQLiteDatabase db = null;
		Cursor cursor = null;

		try {

			db = this.getReadableDatabase();
			cursor = db.rawQuery(sql, null);
			// Looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				do {
					categoriesList.add(new Category(Integer.valueOf(cursor.getInt(0)),
							cursor.getString(1), cursor.getString(2), cursor
									.getString(3)));
				} while (cursor.moveToNext());
			}

		} finally {
			if (cursor != null)
				cursor.close();
			if (db != null)
				db.close();
		}
		return categoriesList;
	}

	
	/**
	 * Updates or insert a new a category
	 * @param category Category to be updated or inserted
	 * @return Rows affected or new inserted category id
	 */
	public long updateCategory(Category category) {

		long res;
		SQLiteDatabase db = null;
		
		try {
			db = this.getWritableDatabase();

			ContentValues values = new ContentValues();
			values.put(KEY_CATEGORY_NAME, category.getName());
			values.put(KEY_CATEGORY_DESCRIPTION, category.getDescription());
			values.put(KEY_CATEGORY_COLOR, category.getColor());

			// Updating row
			if (category.getId() != null) {
				values.put(KEY_CATEGORY_ID, category.getId());
				res = db.update(TABLE_CATEGORY, values, KEY_CATEGORY_ID + " = ?",
						new String[] { String.valueOf(category.getId()) });
				Log.d(Constants.TAG, "Updated category titled '" + category.getName()
						+ "'");
				// Inserting new category
			} else {
				res = db.insert(TABLE_CATEGORY, null, values);
				Log.d(Constants.TAG, "Saved new category titled '" + category.getName()
						+ "' with id: " + res);
			}

		} finally {
			if (db != null)
				db.close();
		}

		// Returning result
		return res;
	}
	
	
	/**
	 * Deletion of  a category
	 * @param category Category to be deleted
	 * @return Number 1 if category's record has been deleted, 0 otherwise
	 */
	public long deleteCategory(Category category) {		
		long deleted;

		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			// Un-categorize notes associated with this category
			ContentValues values = new ContentValues();
			values.put(KEY_CATEGORY, "");

			// Updating row
			db.update(TABLE_NOTES, values, KEY_CATEGORY + " = ?",
					new String[] { String.valueOf(category.getId()) });

			// Delete category
			deleted = db.delete(TABLE_CATEGORY, KEY_CATEGORY_ID + " = ?",
					new String[] { String.valueOf(category.getId()) });

		} finally {
			if (db != null)
				db.close();
		}
		return deleted;
	}

	
	/**
	 * Get note Category
	 * @param id
	 * @return
	 */
	public Category getCategory(Integer id) {		
		Category category = null;
		String sql = "SELECT " 
							+ KEY_CATEGORY_ID + "," 
							+ KEY_CATEGORY_NAME + ","
							+ KEY_CATEGORY_DESCRIPTION  + ","
							+ KEY_CATEGORY_COLOR
						+ " FROM " + TABLE_CATEGORY
						+ " WHERE " + KEY_CATEGORY_ID + " = " + id;

		SQLiteDatabase db = null;
		Cursor cursor = null;

		try {
			db = this.getReadableDatabase();
			cursor = db.rawQuery(sql, null);

			// Looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				category = new Category(cursor.getInt(0), cursor.getString(1),
						cursor.getString(2), cursor.getString(3));
			}

		} finally {
			if (cursor != null)
				cursor.close();
			if (db != null)
				db.close();
		}
		return category;
	}
	

	
	public int getCategorizedCount(Category category) {		
		int count = 0;
		String sql = "SELECT COUNT(*)" 
					+ " FROM " + TABLE_NOTES
					+ " WHERE " + KEY_CATEGORY + " = " + category.getId();
		
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = this.getReadableDatabase();
			cursor = db.rawQuery(sql, null);

			// Looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				count = cursor.getInt(0);
			}

		} finally {
			if (cursor != null)
				cursor.close();
			if (db != null)
				db.close();
		}
		return count;		
	}
	

	
	/**
	 * Unlocks all notes after security password removal
	 * @return
	 */
	public int unlockAllNotes() {			
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(KEY_LOCKED, "0");
		
		// Updating row
		return db.update(TABLE_NOTES, values, null, new String[] {});		
	}
	
	
	
	 
}

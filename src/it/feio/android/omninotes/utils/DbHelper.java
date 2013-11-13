package it.feio.android.omninotes.utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.Note;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;

public class DbHelper extends SQLiteOpenHelper {

	// Database name
	private static final String DATABASE_NAME = "omni-notes";
	// Database version
	private static final int DATABASE_VERSION = 7;
	// Notes table name
	private static final String TABLE_NAME = "notes";
	// Notes table columns
	private static final String KEY_ID = "id";	
	public static final String KEY_CREATION = "creation";	
	public static final String KEY_LAST_MODIFICATION = "last_modification";	
	public static final String KEY_TITLE = "title";	
	private static final String KEY_CONTENT = "content";	
	private static final String KEY_ARCHIVED = "archived";	
	private static final String KEY_ATTACHMENT = "attachment";	// Actually not implemented
	// Creation query
	private static final String TABLE_CREATE = 	"CREATE TABLE " + TABLE_NAME + " (" + 
												KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
												KEY_CREATION + " LONG, " +
												KEY_LAST_MODIFICATION + " LONG, " +
												KEY_TITLE + " TEXT, " +
												KEY_CONTENT + " TEXT, " +
												KEY_ARCHIVED + " INTEGER);";
	
	
//	public static LinkedHashMap<String, String> getSortableColumns() {
//		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
//		map.put("KEY_TITLE", KEY_TITLE);
//        map.put("KEY_CREATION", KEY_CREATION);
//        map.put("KEY_LAST_MODIFICATION", KEY_LAST_MODIFICATION);
//        return map;
//    }
	
	private final Context ctx;

	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.ctx = context;
	}

	
	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME); 
        // Create tables again
        onCreate(db);
	}
	
	
	// Inserting or updating single note
	public long updateNote(Note note) {
		long res;
		SQLiteDatabase db = this.getWritableDatabase();
		 
	    ContentValues values = new ContentValues();
	    values.put(KEY_TITLE, note.getTitle());
	    values.put(KEY_CONTENT, note.getContent());
	    values.put(KEY_LAST_MODIFICATION, Calendar.getInstance().getTimeInMillis());
//	    boolean navigationArchived = PreferenceManager.getDefaultSharedPreferences(ctx).getString(Constants.PREF_NAVIGATION, "").equals(ctx.getResources().getStringArray(R.array.navigation_list)[1]);
	    boolean archive = note.isArchived() != null ? note.isArchived() : false;
	    values.put(KEY_ARCHIVED, archive);

		// Updating row
		if (note.get_id() != 0) {
			values.put(KEY_ID, note.get_id());
			res = db.update(TABLE_NAME, values, KEY_ID + " = ?",
					new String[] { String.valueOf(note.get_id()) });
			// Importing data from csv without existing note in db
			if (res == 0) {
				res = db.insert(TABLE_NAME, null, values);
			}			
			Log.d(Constants.TAG, "Updated note titled '" + note.getTitle() + "'");
			
		// Inserting new note
		} else {
			values.put(KEY_CREATION, Calendar.getInstance().getTimeInMillis());
		    res = db.insert(TABLE_NAME, null, values);
			Log.d(Constants.TAG, "Saved new note titled '" + note.getTitle() + "' with id: " + res);
		}
	    db.close();
		return res;
	}
	
	
//	// Archiving single note
//	public long archiveNote(Note note) {
//		long res;
//		SQLiteDatabase db = this.getWritableDatabase();
//		 
//	    ContentValues values = new ContentValues();
//	    values.put(KEY_TITLE, note.getTitle());
//	    values.put(KEY_CONTENT, note.getContent());
//	    values.put(KEY_LAST_MODIFICATION, Calendar.getInstance().getTimeInMillis());
//	    values.put(KEY_ARCHIVED, 1);
//
//		// Updating row
//		values.put(KEY_ID, note.get_id());
//		res = db.update(TABLE_NAME, values, KEY_ID + " = ?",
//				new String[] { String.valueOf(note.get_id()) });
//		Log.d(Constants.TAG, "Updated note titled '" + note.getTitle() + "'");
//			
//		
//		return res;
//	}
	 
	
	// Getting single note
	public Note getNote(int id) {
		SQLiteDatabase db = getReadableDatabase();
		 
	    Cursor cursor = db.query(TABLE_NAME, new String[] { KEY_ID, KEY_CREATION,
	    		KEY_LAST_MODIFICATION, KEY_TITLE, KEY_CONTENT, KEY_ARCHIVED }, KEY_ID + "=?",
	            new String[] { String.valueOf(id) }, null, null, null, null);
	    if (cursor != null)
	        cursor.moveToFirst();

	    Note note = new Note(Integer.parseInt(cursor.getString(0)), DateHelper.getDateString(cursor.getLong(1)), DateHelper.getDateString(cursor.getLong(2)), cursor.getString(3), cursor.getString(4), cursor.getInt(5));

	    db.close();
	    return note;
	}
	 
	
	/**
	 * Getting All notes
	 * @param checkNavigation Tells if navigation status (notes, archived) must
	 * 			be kept in consideration or if all notes have to be retrieved
	 * @return Notes list
	 */
	public List<Note> getAllNotes(boolean checkNavigation) {
		List<Note> noteList = new ArrayList<Note>();
		
		// Getting sorting criteria from preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		String sort_column = prefs.getString(Constants.PREF_SORTING_COLUMN, KEY_TITLE);
		
		// Checking if archived notes must be shown
		boolean archived = prefs.getString(Constants.PREF_NAVIGATION, "").equals(ctx.getResources().getStringArray(R.array.navigation_list)[1]) ? true : false;
		
		String whereCondition = checkNavigation ? " WHERE " + KEY_ARCHIVED + (archived ? " = 1 " : " = 0 ") : "";
				
	    // Select All Query
	    String selectQuery = "SELECT * FROM " + TABLE_NAME + 
	    					whereCondition +
	    					" ORDER BY " + sort_column;
	    Log.d(Constants.TAG, "Select notes query: " + selectQuery);
	    
	    SQLiteDatabase db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);
	 
	    // Looping through all rows and adding to list
	    if (cursor.moveToFirst()) {
	        do {
	            Note note = new Note();
	            note.set_id(Integer.parseInt(cursor.getString(0)));
	            note.setCreation(DateHelper.getDateString(cursor.getLong(1)));
	            note.setlastModification(DateHelper.getDateString(cursor.getLong(2)));
	            note.setTitle(cursor.getString(3));
	            note.setContent(cursor.getString(4));
	            note.setArchived("1".equals(cursor.getString(5)));
	            // Adding note to list
	            noteList.add(note);
	        } while (cursor.moveToNext());
	    }
	    
	    db.close();
	 
	    return noteList;
	}
	 
	
	// Getting notes count
	public int getNotesCount() {
		String countQuery = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
	    db.close();
        return cursor.getCount();
	}
	 
	
	// Deleting single note
	public void deleteNote(Note note) {
		SQLiteDatabase db = this.getWritableDatabase();
	    db.delete(TABLE_NAME, KEY_ID + " = ?",
	            new String[] { String.valueOf(note.get_id()) });
	    db.close();
	}
	

	
	// Clears completelly the database
	public void clear() {
	    SQLiteDatabase db = this.getWritableDatabase();
	    db.execSQL("DELETE FROM " + TABLE_NAME);
	    db.close();
	}
	
	
	/**
	 * Getting All notes
	 * @param checkNavigation Tells if navigation status (notes, archived) must
	 * 			be kept in consideration or if all notes have to be retrieved
	 * @return Notes list
	 */
	public List<Note> getMatchingNotes(String pattern) {
		List<Note> noteList = new ArrayList<Note>();
		
		// Getting sorting criteria from preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		String sort_column = prefs.getString(Constants.PREF_SORTING_COLUMN, KEY_TITLE);
				
	    // Select All Query
	    String selectQuery = "SELECT * FROM " + TABLE_NAME + 
	    					" WHERE " + KEY_TITLE + " LIKE '%" + pattern + "%' " +
	    					" OR " + KEY_CONTENT + " LIKE '%" + pattern + "%' " +
	    					" ORDER BY " + sort_column;
	    Log.d(Constants.TAG, "Select notes query: " + selectQuery);
	    
	    SQLiteDatabase db = this.getReadableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);
	 
	    // Looping through all rows and adding to list
	    if (cursor.moveToFirst()) {
	        do {
	            Note note = new Note();
	            note.set_id(Integer.parseInt(cursor.getString(0)));
	            note.setCreation(DateHelper.getDateString(cursor.getLong(1)));
	            note.setlastModification(DateHelper.getDateString(cursor.getLong(2)));
	            note.setTitle(cursor.getString(3));
	            note.setContent(cursor.getString(4));
	            note.setArchived("1".equals(cursor.getString(5)));
	            // Adding note to list
	            noteList.add(note);
	        } while (cursor.moveToNext());
	    }
	    
	    db.close();
	 
	    return noteList;
	} 
	
	
	
	
	
	
	
	
	
	
}
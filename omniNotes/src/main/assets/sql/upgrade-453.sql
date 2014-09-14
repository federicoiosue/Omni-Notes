/*
* Performs a column addition to keep location as string to avoid resolving everytime
*/
ALTER TABLE notes RENAME TO notes_tmp;
CREATE
	TABLE notes
	(
		note_id INTEGER PRIMARY KEY AUTOINCREMENT,
		creation INTEGER,
		last_modification INTEGER,
		title TEXT,
		content TEXT,
		archived INTEGER,
		trashed INTEGER,
		alarm INTEGER DEFAULT null,
		latitude REAL,
		longitude REAL,
		address TEXT,
		category_id INTEGER DEFAUL null,
		locked INTEGER,  
		checklist  INTEGER   
	);
INSERT INTO notes(note_id, creation, last_modification, title, content, archived, alarm, latitude, longitude, category_id, locked, checklist)
SELECT note_id, creation, last_modification, title, content, archived, alarm, latitude, longitude, category_id, locked, checklist
FROM notes_tmp;
DROP TABLE notes_tmp; 

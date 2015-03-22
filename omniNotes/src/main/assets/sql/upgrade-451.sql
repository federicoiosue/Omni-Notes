/*
* Renaming TAGS table to CATEGORIES
*/

ALTER TABLE tags RENAME TO tags_tmp;
CREATE
	TABLE categories
	(
		category_id INTEGER PRIMARY KEY AUTOINCREMENT,
		name TEXT,
		description TEXT,
		color TEXT
	);	
INSERT INTO categories(category_id, name, description, color)
SELECT tag_id, name, description, color
FROM tags_tmp;
DROP TABLE tags_tmp; 



/*
* Performing column name change to match category_id
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
		category_id INTEGER DEFAULT null,
		locked INTEGER,  
		checklist  INTEGER   
	);
INSERT INTO notes(note_id, creation, last_modification, title, content, archived, trashed, alarm, latitude, longitude,
category_id, locked, checklist)
SELECT note_id, creation, last_modification, title, content, archived, trashed, alarm, latitude, longitude, tag_id, locked, checklist
FROM notes_tmp;
DROP TABLE notes_tmp; 

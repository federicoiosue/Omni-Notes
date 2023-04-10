-- Create table for tags
CREATE
	TABLE tags
	(
		tag_id INTEGER PRIMARY KEY AUTOINCREMENT,
		name TEXT,
        	description TEXT,
        	color TEXT
	);

/* 
* Serie of commands to rename id columns of various tables to avoid ambiguity joining them 
*/

-- Backup table
ALTER TABLE notes RENAME TO notes_tmp;

-- Create new table for NOTES adding "locked" column too
CREATE
	TABLE notes
	(
		note_id INTEGER PRIMARY KEY AUTOINCREMENT,
		creation INTEGER,
		last_modification INTEGER,
		title TEXT,
		content TEXT,
		archived INTEGER,
		alarm INTEGER DEFAULT null,
		latitude REAL,
		longitude REAL,
        	tag_id INTEGER DEFAUL null,
		locked INTEGER
	);

-- Moving notes data	
INSERT INTO notes(note_id, creation, last_modification, title, content, archived, alarm, latitude, longitude)
SELECT id, creation, last_modification, title, content, archived, alarm, latitude, longitude
FROM notes_tmp;

-- Dropping old table
DROP TABLE notes_tmp; 



-- Backup table
ALTER TABLE attachments RENAME TO attachments_tmp;

-- Create new table for attachments
CREATE
	TABLE attachments
	(
		attachment_id INTEGER PRIMARY KEY AUTOINCREMENT,
		uri TEXT,
		mime_type TEXT,
		note_id INTEGER
	);

-- Moving attachments data	
INSERT INTO attachments(attachment_id, uri, mime_type, note_id)
SELECT id, uri, mime_type, note_id
FROM attachments_tmp;

-- Dropping old table
DROP TABLE attachments_tmp; 


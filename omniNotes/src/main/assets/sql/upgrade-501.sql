/*
 * Refactors 'notes' and 'attachments' tables to use note's creation as primary key
 */

-- Update for ATTACHMENTS
ALTER TABLE attachments RENAME TO attachments_tmp;
CREATE
	TABLE attachments
	(
		attachment_id INTEGER PRIMARY KEY AUTOINCREMENT,
		uri TEXT,
		name TEXT,
		size INTEGER,
		length INTEGER,
		mime_type TEXT,
		note_id INTEGER
	);
INSERT INTO attachments(attachment_id, uri, name, size, length, mime_type, note_id)
SELECT
	a.attachment_id,
	a.uri,
	a.name,
	a.size,
	a.length,
	a.mime_type,
	notes.creation
FROM attachments_tmp a JOIN notes ON notes.note_id=a.note_id;
DROP TABLE attachments_tmp;

-- Update for NOTES
ALTER TABLE notes RENAME TO notes_tmp;
CREATE
	TABLE notes
	(
		creation INTEGER PRIMARY KEY,
		last_modification INTEGER,
		title TEXT,
		content TEXT,
		archived INTEGER,
		trashed INTEGER,
		alarm INTEGER DEFAULT null,
        reminder_fired INTEGER,
		recurrence_rule TEXT,
		latitude REAL,
		longitude REAL,
		address TEXT,
		category_id INTEGER DEFAULT null,
		locked INTEGER,  
		checklist  INTEGER   
	);
INSERT INTO notes(creation, last_modification, title, content, archived, trashed, alarm, reminder_fired,
    recurrence_rule, latitude, longitude, address, category_id, locked, checklist)
SELECT creation, last_modification, title, content, archived, trashed, alarm, reminder_fired, recurrence_rule,
    latitude, longitude, address, category_id, locked, checklist
FROM notes_tmp;
DROP TABLE notes_tmp; 

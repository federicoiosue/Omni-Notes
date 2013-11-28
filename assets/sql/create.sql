-- Create table for NOTES
CREATE
	TABLE notes
	(
		id INTEGER PRIMARY KEY AUTOINCREMENT,
		creation LONG,
		last_modification LONG,
		title TEXT,
		content TEXT,
		archived INTEGER,
		alarm LONG DEFAULT null
	);
	


-- Create table for attachments
CREATE
	TABLE attachments
	(
		id INTEGER PRIMARY KEY AUTOINCREMENT,
		uri TEXT,
		note_id INTEGER
	);
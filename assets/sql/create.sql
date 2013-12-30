-- Create table for NOTES
CREATE
	TABLE notes
	(
		id INTEGER PRIMARY KEY AUTOINCREMENT,
		creation INTEGER,
		last_modification INTEGER,
		title TEXT,
		content TEXT,
		archived INTEGER,
		alarm INTEGER DEFAULT null,
		latitude REAL,
		longitude REAL,
        tag INTEGER DEFAUL null
	);
	


-- Create table for attachments
CREATE
	TABLE attachments
	(
		id INTEGER PRIMARY KEY AUTOINCREMENT,
		uri TEXT,
		mime_type TEXT,
		note_id INTEGER
	);
	


-- Create table for tags
CREATE
	TABLE tags
	(
		id INTEGER PRIMARY KEY AUTOINCREMENT,
		title TEXT,
        description TEXT,
        color TEXT
	);

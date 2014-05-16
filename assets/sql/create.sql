-- Create table for NOTES
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
	


-- Create table for ATTACHMENTS
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
	


-- Create table for CATEGORIES
CREATE
	TABLE categories
	(
		category_id INTEGER PRIMARY KEY AUTOINCREMENT,
		name TEXT,
		description TEXT,
		color TEXT
	);

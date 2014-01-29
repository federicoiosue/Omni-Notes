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
		alarm INTEGER DEFAULT null,
		latitude REAL,
		longitude REAL,
       		tag_id INTEGER DEFAUL null,
		locked INTEGER,  
		checklist  INTEGER   
	);
	


-- Create table for attachments
CREATE
	TABLE attachments
	(
		attachment_id INTEGER PRIMARY KEY AUTOINCREMENT,
		uri TEXT,
		mime_type TEXT,
		note_id INTEGER
	);
	


-- Create table for tags
CREATE
	TABLE tags
	(
		tag_id INTEGER PRIMARY KEY AUTOINCREMENT,
		name TEXT,
        	description TEXT,
        	color TEXT
	);

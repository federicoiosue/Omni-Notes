CREATE
	TABLE notes
	(
		id INTEGER PRIMARY KEY AUTOINCREMENT,
		creation LONG,
		last_modification LONG,
		title TEXT,
		content TEXT,
		archived INTEGER,
		alarm LONG
	);
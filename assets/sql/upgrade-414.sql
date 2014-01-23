-- Adding column to support checklists
ALTER TABLE notes ADD COLUMN checklist archived INTEGER default 0;

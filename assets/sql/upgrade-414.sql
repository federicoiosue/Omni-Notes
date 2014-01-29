-- Adding column to support checklists
ALTER TABLE notes ADD COLUMN checklist INTEGER default 0;

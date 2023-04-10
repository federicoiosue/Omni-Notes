-- Adding attachments meta data
ALTER TABLE attachments ADD COLUMN name TEXT;
ALTER TABLE attachments ADD COLUMN size INTEGER;
ALTER TABLE attachments ADD COLUMN length INTEGER;

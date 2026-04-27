-- Add first_name and last_name columns to users table
-- SQLite doesn't support adding NOT NULL columns without a default to existing tables,
-- so we add them with a default empty string and allow existing rows to have this value.
ALTER TABLE users ADD COLUMN first_name VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE users ADD COLUMN last_name VARCHAR(100) NOT NULL DEFAULT '';

-- V8: Remove description column from time_table_place_block

ALTER TABLE time_table_place_block
DROP COLUMN IF EXISTS description;

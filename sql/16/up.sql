USE master;

SET @migration := 16;

-- check migration number
SELECT CASE migration WHEN @migration - 1 THEN 'SELECT ''Performing update...''' ELSE CONCAT('KILL CONNECTION ', connection_id()) END
INTO @stmt FROM Migration;

PREPARE stmt FROM @stmt;
EXECUTE stmt;
-- ////////////////////////////////////////// --

USE directory;

ALTER TABLE Location_Category ADD CONSTRAINT unq_location_category UNIQUE (location_id, category_id);
ALTER TABLE Location_Ingest ADD CONSTRAINT unq_location_ingest UNIQUE (location_id, ingest_id);

USE master;

-- ////////////////////////////////////////// --
-- update migration
UPDATE Migration SET migration = migration + 1;

COMMIT;
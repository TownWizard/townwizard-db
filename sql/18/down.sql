USE master;

SET @migration := 18;

-- check migration number
SELECT CASE migration WHEN @migration THEN 'SELECT ''Performing update...''' ELSE CONCAT('KILL CONNECTION ', connection_id()) END
INTO @stmt FROM Migration;

PREPARE stmt FROM @stmt;
EXECUTE stmt;
-- ////////////////////////////////////////// --

ALTER TABLE User DROP COLUMN image_url;

-- ////////////////////////////////////////// --
-- update migration
UPDATE Migration SET migration = migration - 1;

COMMIT;
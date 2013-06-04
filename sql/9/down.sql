USE master;

SET @migration := 9;

-- check migration number
SELECT CASE migration WHEN @migration THEN 'SELECT ''Performing update...''' ELSE CONCAT('KILL CONNECTION ', connection_id()) END
INTO @stmt FROM Migration;

PREPARE stmt FROM @stmt;
EXECUTE stmt;
-- ////////////////////////////////////////// --

DROP TABLE Location_LocationIngest;
DROP TABLE Location_LocationCategory;
DROP TABLE Location;
DROP TABLE LocationCategory;
DROP TABLE LocationIngest;

-- ////////////////////////////////////////// --
-- update migration
UPDATE Migration SET migration = migration - 1;

COMMIT;
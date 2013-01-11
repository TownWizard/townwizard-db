USE master;

SET @migration := 6;

-- check migration number
SELECT CASE migration WHEN @migration - 1 THEN 'SELECT ''Performing update...''' ELSE CONCAT('KILL CONNECTION ', connection_id()) END
INTO @stmt FROM Migration;

PREPARE stmt FROM @stmt;
EXECUTE stmt;
-- ////////////////////////////////////////// --

ALTER TABLE LoginRequest
MODIFY id VARCHAR(50);

ALTER TABLE User
ADD COLUMN name VARCHAR(100);

-- ////////////////////////////////////////// --
-- update migration
UPDATE Migration SET migration = migration + 1;

COMMIT;
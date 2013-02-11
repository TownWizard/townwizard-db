USE master;

SET @migration := 7;

-- check migration number
SELECT CASE migration WHEN @migration THEN 'SELECT ''Performing update...''' ELSE CONCAT('KILL CONNECTION ', connection_id()) END
INTO @stmt FROM Migration;

PREPARE stmt FROM @stmt;
EXECUTE stmt;
-- ////////////////////////////////////////// --

ALTER TABLE User
DROP FOREIGN KEY fk_user_site;

ALTER TABLE User
DROP COLUMN site_id;

-- ////////////////////////////////////////// --
-- update migration
UPDATE Migration SET migration = migration - 1;

COMMIT;
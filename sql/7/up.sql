USE master;

SET @migration := 7;

-- check migration number
SELECT CASE migration WHEN @migration - 1 THEN 'SELECT ''Performing update...''' ELSE CONCAT('KILL CONNECTION ', connection_id()) END
INTO @stmt FROM Migration;

PREPARE stmt FROM @stmt;
EXECUTE stmt;
-- ////////////////////////////////////////// --

ALTER TABLE User
ADD COLUMN site_id INT;

ALTER TABLE User
ADD CONSTRAINT fk_user_site FOREIGN KEY(site_id) REFERENCES master(mid);

-- ////////////////////////////////////////// --
-- update migration
UPDATE Migration SET migration = migration + 1;

COMMIT;
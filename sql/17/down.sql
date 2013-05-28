USE master;

SET @migration := 17;

-- check migration number
SELECT CASE migration WHEN @migration THEN 'SELECT ''Performing update...''' ELSE CONCAT('KILL CONNECTION ', connection_id()) END
INTO @stmt FROM Migration;

PREPARE stmt FROM @stmt;
EXECUTE stmt;
-- ////////////////////////////////////////// --

USE directory;

CREATE TABLE `Lock` (
  `value` BIGINT NOT NULL
) ENGINE = InnoDB;

INSERT INTO `Lock` VALUES(1);

USE master;

-- ////////////////////////////////////////// --
-- update migration
UPDATE Migration SET migration = migration - 1;

COMMIT;
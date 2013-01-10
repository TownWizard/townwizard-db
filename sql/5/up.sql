USE master;

SET @migration := 5;

-- check migration number
SELECT CASE migration WHEN @migration - 1 THEN 'SELECT ''Performing update...''' ELSE CONCAT('KILL CONNECTION ', connection_id()) END
INTO @stmt FROM Migration;

PREPARE stmt FROM @stmt;
EXECUTE stmt;
-- ////////////////////////////////////////// --

CREATE TABLE LoginRequest (
  id CHAR(36),
  date DATETIME,
  location VARCHAR(250),
  CONSTRAINT pk_login_request PRIMARY KEY(id)  
) ENGINE = InnoDB;

-- ////////////////////////////////////////// --
-- update migration
UPDATE Migration SET migration = migration + 1;

COMMIT;
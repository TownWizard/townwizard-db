USE master;

SET @migration := 4;

-- check migration number
SELECT CASE migration WHEN @migration - 1 THEN 'SELECT ''Performing update...''' ELSE CONCAT('KILL CONNECTION ', connection_id()) END
INTO @stmt FROM Migration;

PREPARE stmt FROM @stmt;
EXECUTE stmt;
-- ////////////////////////////////////////// --

CREATE TABLE Event (
  id BIGINT NOT NULL,
  date DATETIME,
  CONSTRAINT pk_event PRIMARY KEY(id),
  CONSTRAINT fk_event_content FOREIGN KEY(id) REFERENCES Content(id)
) ENGINE = InnoDB;

CREATE INDEX idx_event_date ON Event(date);

CREATE TABLE EventResponse (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  event_id BIGINT NOT NULL,
  created DATETIME NOT NULL,
  updated DATETIME NOT NULL,
  active BIT NOT NULL,
  value CHAR(1) NOT NULL,
  CONSTRAINT pk_event_response PRIMARY KEY(id),
  CONSTRAINT fk_event_response_user FOREIGN KEY(user_id) REFERENCES User(id),
  CONSTRAINT fk_event_response_event FOREIGN KEY(event_id) REFERENCES Event(id)
) ENGINE = InnoDB;

-- ////////////////////////////////////////// --
-- update migration
UPDATE Migration SET migration = migration + 1;

COMMIT;
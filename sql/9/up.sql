USE master;

SET @migration := 9;

-- check migration number
SELECT CASE migration WHEN @migration - 1 THEN 'SELECT ''Performing update...''' ELSE CONCAT('KILL CONNECTION ', connection_id()) END
INTO @stmt FROM Migration;

PREPARE stmt FROM @stmt;
EXECUTE stmt;
-- ////////////////////////////////////////// --

CREATE TABLE LocationIngest (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created DATETIME NOT NULL,
  updated DATETIME NOT NULL,
  active BIT NOT NULL,
  zip VARCHAR(10),
  country_code CHAR(2),
  distance INTEGER,
  CONSTRAINT pk_location_ingest PRIMARY KEY (id),
  CONSTRAINT unq_location_ingest UNIQUE(zip, country_code)
) ENGINE = InnoDB;

ALTER TABLE LocationIngest ADD INDEX idx_location_ingest (zip, country_code, distance);

CREATE TABLE LocationCategory (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255),
  CONSTRAINT pk_location_category PRIMARY KEY (id),
  CONSTRAINT unq_location_category_name UNIQUE(name)
) ENGINE = InnoDB;

CREATE TABLE Location (
  id BIGINT NOT NULL AUTO_INCREMENT,
  active BIT NOT NULL,  
  external_id VARCHAR(255),
  name VARCHAR(255),
  category VARCHAR(255),
  street VARCHAR(255),
  city VARCHAR(50),
  state CHAR(2),
  zip VARCHAR(10),
  country_code CHAR(2) NOT NULL,
  phone VARCHAR(20),
  latitude DOUBLE,
  longitude DOUBLE,
  url VARCHAR(500),
  source INTEGER NOT NULL,
  CONSTRAINT pk_location PRIMARY KEY (id),
  CONSTRAINT unq_location UNIQUE(external_id, source)  
) ENGINE = InnoDB;

CREATE TABLE Location_LocationCategory (
  id BIGINT NOT NULL AUTO_INCREMENT,
  location_id BIGINT NOT NULL,
  location_category_id BIGINT NOT NULL,
  CONSTRAINT pk_loc_loccat PRIMARY KEY (id),
  CONSTRAINT fk_lc_location FOREIGN KEY(location_id) REFERENCES Location(id),
  CONSTRAINT fk_lc_location_category FOREIGN KEY(location_category_id) REFERENCES LocationCategory(id)
) ENGINE = InnoDB;

CREATE TABLE Location_LocationIngest (
  id BIGINT NOT NULL AUTO_INCREMENT,
  location_id BIGINT NOT NULL,
  location_ingest_id BIGINT NOT NULL,
  CONSTRAINT pk_loc_locingest PRIMARY KEY (id),
  CONSTRAINT fk_li_location FOREIGN KEY(location_id) REFERENCES Location(id),
  CONSTRAINT fk_li_location_ingest FOREIGN KEY(location_ingest_id) REFERENCES LocationIngest(id)
) ENGINE = InnoDB;

-- ////////////////////////////////////////// --
-- update migration
UPDATE Migration SET migration = migration + 1;

COMMIT;
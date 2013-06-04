USE master;

SET @migration := 11;

-- check migration number
SELECT CASE migration WHEN @migration - 1 THEN 'SELECT ''Performing update...''' ELSE CONCAT('KILL CONNECTION ', connection_id()) END
INTO @stmt FROM Migration;

PREPARE stmt FROM @stmt;
EXECUTE stmt;
-- ////////////////////////////////////////// --

USE geo;

CREATE TABLE CityBlocks (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  ip_start INT NOT NULL,
  ip_end INT NOT NULL,
  location_id INT NOT NULL
);

CREATE TABLE CityLocations (
  id INT NOT NULL PRIMARY KEY,
  country_code CHAR(2),
  region_code CHAR(2),
  city VARCHAR(100),
  postal_code VARCHAR(10),
  latitude DOUBLE,
  longitude DOUBLE,
  metro_code VARCHAR(10),
  area_code VARCHAR(10)
);

LOAD DATA CONCURRENT LOCAL INFILE '11/GeoLiteCity-Blocks.csv' INTO TABLE CityBlocks
COLUMNS TERMINATED BY ',' ENCLOSED BY '"'
IGNORE 2 LINES 
(ip_start, ip_end, location_id);

LOAD DATA CONCURRENT LOCAL INFILE '11/GeoLiteCity-Location.csv' INTO TABLE CityLocations
COLUMNS TERMINATED BY ',' ENCLOSED BY '"'
IGNORE 2 LINES;

ALTER TABLE CityBlocks ADD INDEX idx_cityblocks_ip_start (ip_start);
ALTER TABLE CityBlocks ADD INDEX idx_cityblocks_ip_end (ip_end);
ALTER TABLE CityBlocks ADD INDEX idx_cityblocks_loc_id (location_id);

USE master;

-- ////////////////////////////////////////// --
-- update migration
UPDATE Migration SET migration = migration + 1;

COMMIT;
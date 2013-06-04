USE master;

SET @migration := 16;

-- check migration number
SELECT CASE migration WHEN @migration THEN 'SELECT ''Performing update...''' ELSE CONCAT('KILL CONNECTION ', connection_id()) END
INTO @stmt FROM Migration;

PREPARE stmt FROM @stmt;
EXECUTE stmt;
-- ////////////////////////////////////////// --

USE directory;

ALTER TABLE Location_Category DROP FOREIGN KEY fk_lc_location;
ALTER TABLE Location_Category DROP FOREIGN KEY fk_lc_category;
ALTER TABLE Location_Category DROP INDEX unq_location_category;
ALTER TABLE Location_Category ADD CONSTRAINT fk_lc_location FOREIGN KEY(location_id) REFERENCES Location(id);
ALTER TABLE Location_Category ADD CONSTRAINT fk_lc_category FOREIGN KEY(category_id) REFERENCES Category(id);

ALTER TABLE Location_Ingest DROP FOREIGN KEY fk_li_location;
ALTER TABLE Location_Ingest DROP FOREIGN KEY fk_li_ingest;
ALTER TABLE Location_Ingest DROP INDEX unq_location_ingest;
ALTER TABLE Location_Ingest ADD CONSTRAINT fk_li_location FOREIGN KEY(location_id) REFERENCES Location(id);
ALTER TABLE Location_Ingest ADD CONSTRAINT fk_li_ingest FOREIGN KEY(ingest_id) REFERENCES Ingest(id);

USE master;

-- ////////////////////////////////////////// --
-- update migration
UPDATE Migration SET migration = migration - 1;

COMMIT;
package com.townwizard.globaldata.ingest.place;

import java.util.Date;
import java.util.List;

import com.townwizard.globaldata.model.directory.PlaceCategory;

public class JdbcIngester extends AbstractIngester {
    
    public JdbcIngester(String zipCode, String countryCode, List<PlaceCategory> categories) {
        super(zipCode, countryCode, categories);
    }


    
    @Override
    public void merge() {
        
        Date now = new Date();
        
        //1) category list
        //2) set of places
        //3) category to places map
        //4) new categories set
        
        //escape qoutes
        
        //merge ingests
        String s = "INSERT INTO Ingest (created, status, zip, country_code, category_id) " + 
                   "VALUES ('2013-05-07 17:26:20', 'R', '10001', 'US', 1) " + 
                   "ON DUPLICATE KEY UPDATE country_code = 'US'";
        
        
        //merge locations
        String s2 = "INSERT INTO Location (created, external_id .... " + 
                    "ON DUPLICATE KEY UPDATE external_id = 1234";
        
        
        //map locations to ingests
        String s3 = "INSERT INTO Location_Ingest (location_id, ingest_id) " +
                    "VALUES ((SELECT id FROM Location WHERE external_id = '123456' AND source = 'YP'), 1) " +
                     "ON DUPLICATE KEY UPDATE ingest_id = 1";
        
        
        // map locations to categories
        String s4 = "INSERT INTO Location_Category (location_id, category_id) " + 
                    "VALUES ((SELECT id FROM Location WHERE external_id = '123456' AND source = 'YP'), 1) " +
                    "ON DUPLICATE KEY UPDATE category_id = 1";
        
        //add new categories
        String s5 = "INSERT INTO Category (name) VALUES ('pizza') ON DUPLICATE KEY UPDATE name = 'pizza'";
        
        
    }

}

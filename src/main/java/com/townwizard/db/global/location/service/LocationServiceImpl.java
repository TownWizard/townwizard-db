package com.townwizard.db.global.location.service;

import java.io.InputStream;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.townwizard.db.global.model.Location;
import com.townwizard.db.logger.Log;
import com.townwizard.db.util.DataUtils;

@Component("LocationService")
public class LocationServiceImpl implements LocationService {
    
    // taken from http://federalgovernmentzipcodes.us/
    private static final String LOCATION_DATABASE = "free-zipcode-database-Primary.csv";    
    
    private static Map<String, Location> data;

    @Override
    public Location getLocationByZip(String zip) {
        if(data == null) {
            synchronized(this) {
                if(data == null) {
                    try {
                        Log.info("Loading location data...");
                        loadData();
                        Log.info("Done loading location data...");
                    } catch (Exception e) {
                        throw new RuntimeException (e);
                    }
                }
            }
        }
        
        return data.get(zip);
    }
    
    private void loadData() throws Exception {
        data = DataUtils.csvToMap(
                getDataInputStream(LOCATION_DATABASE), 1,
                new int[]{0,2,3,5,6}, 
                new String[] {"zip", "city", "state", "latitude", "longitude"},
                String.class, Location.class);        
    }
    
    private InputStream getDataInputStream(String name) {
        InputStream in = getClass().getResourceAsStream(name);
        if(in == null) {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
        }
        return in;
    }
}

package com.townwizard.globaldata.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.townwizard.db.logger.Log;
import com.townwizard.db.util.DataUtils;
import com.townwizard.globaldata.model.Location;

@Component("LocationService")
public class LocationServiceImpl implements LocationService {
    
    // source: http://federalgovernmentzipcodes.us/
    //private static final String TEST_LOCATION_DATABASE = "free-zipcode-database-Primary.csv"; 
    // source: http://download.geonames.org/export/zip/
    private static final String LOCATION_DATABASE = "allCountries.txt"; 
    
    private static Map<String, List<Location>> data;

    @Override
    public List<Location> getLocations(String zip, String countryCode) {
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
        
        List<Location> allLocations = data.get(zip);
        List<Location> locations = new ArrayList<>();
        for(Location l : allLocations) if(countryCode.equals(l.getCountryCode())) locations.add(l);
        return locations;
    }
    
    @Override
    public Integer distance(Location location, String zip, String countryCode) {
        List<Location> locations = getLocations(zip, countryCode);
        if(locations != null && !locations.isEmpty()) {
            return distance (location, locations.get(0));    
        }
        return null;
    }
    
    @Override
    public Integer distance(Location location1, Location location2) {
        if(location1 != null && location2 != null) {
            Float lat1 = location1.getLatitude();
            Float lat2 = location2.getLatitude();
            Float lon1 = location1.getLongitude();
            Float lon2 = location2.getLongitude();
            
            //formula source: http://www.movable-type.co.uk/scripts/latlong.html
            if(lat1 != null && lat2 != null && lon1 != null && lon2 != null) {
                double lat1Rad = StrictMath.toRadians(lat1);
                double lat2Rad = StrictMath.toRadians(lat2);
                double lon1Rad = StrictMath.toRadians(lon1);
                double lon2Rad = StrictMath.toRadians(lon2);

                int result = (int)((StrictMath.acos(
                        StrictMath.sin(lat1Rad) * StrictMath.sin(lat2Rad) + 
                        StrictMath.cos(lat1Rad) * StrictMath.cos(lat2Rad) * StrictMath.cos(lon2Rad - lon1Rad)
                        )) * 6371000); // Earth radius in meters
                
                return result;
            }
        }
        return null;
    }
    
    private void loadData() throws Exception {
        data = DataUtils.csvToMap(
                getDataInputStream(LOCATION_DATABASE), 0,
                new int[]{1, 0, 2, 4, 9, 10}, 
                new String[] {"zip", "countryCode", "city", "state", "latitude", "longitude"},
                String.class, Location.class, "\t", "");
    }
    
    /*
    private void loadTestData() throws Exception {
        data = DataUtils.csvToMap(
                getDataInputStream(TEST_LOCATION_DATABASE), 1,
                new int[]{0,2,3,5,6}, 
                new String[] {"zip", "city", "state", "latitude", "longitude"},
                String.class, Location.class, ",", "\"");        
    }
    */
    private InputStream getDataInputStream(String name) {
        InputStream in = getClass().getResourceAsStream(name);
        if(in == null) {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
        }
        return in;
    }
}

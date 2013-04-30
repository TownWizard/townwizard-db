package com.townwizard.globaldata.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.springframework.stereotype.Component;

import com.townwizard.db.logger.Log;
import com.townwizard.db.util.DataUtils;
import com.townwizard.globaldata.model.Location;

/**
 * LocationService implementation, which uses allCountries.txt data file.
 * The data is loaded on the first request and kept in the memory.
 */
@Component("LocationService")
public class LocationServiceImpl implements LocationService {
    
    private static final String LOCATION_DATABASE = "allCountries.txt"; 
    
    private static Map<String, List<Location>> locationsByZip;
    private static SortedMap<Float, SortedSet<Location>> locationsByLatitude;
    private static SortedMap<Float, SortedSet<Location>> locationsByLongitude;
        
    @Override
    public List<Location> getLocations(String zip, String countryCode) {
        checkDataLoaded();
        List<Location> allLocations = locationsByZip.get(zip);
        if(allLocations != null) {
            List<Location> locations = new ArrayList<>();
            for(Location l : allLocations) if(countryCode.equals(l.getCountryCode())) locations.add(l);
            return locations;
        }
        return Collections.emptyList();        
    }
    
    /**
     * Gets all locations by zip info, and from the returned list takes the first location
     * which has latitude and longitude.
     */
    @Override
    public Location getPrimaryLocation(String zip, String countryCode) {
        List<Location> zipLocations = getLocations(zip, countryCode);        
        for(Location l : zipLocations) {
            if(l.getLatitude() != null && l.getLongitude() != null) {
                return l;
            }
        }
        return null;
    }
    
    /**
     * Load all locations for the latitude.
     * Load all locations for the longitude.
     * Put them all in one list.
     * For each location calculate the distance from it to the requested latitude/longitude.
     * Return the location with the smaller distance.
     */
    @Override
    public Location getLocation(double latitude, double longitude) {
        checkDataLoaded();
        
        float lat = (float)latitude;
        float lon = (float)longitude;
        Set<Location> lByLatitude = findInMap(locationsByLatitude, lat);
        Set<Location> lByLongitude = findInMap(locationsByLongitude, lon);
        
        if(lByLatitude != null && lByLongitude != null) {
            Set<Location> allLocations = lByLatitude;
            allLocations.addAll(lByLongitude);
            
            Location orig = new Location(lat, lon);
            
            int minDistance = Integer.MAX_VALUE;
            Location selected = null;
            for(Location l : allLocations) {
               int distance = distance(orig, l);
               if(distance < minDistance) {
                   minDistance = distance;
                   selected = l;
               }
            }
            return selected;            
        }
        
        return null;
    }
    
    @Override
    public List<String> getCities(String zip, String countryCode) {
        List<Location> locations = getLocations(zip, countryCode);
        if(locations != null && !locations.isEmpty()) {
            List<String> cities = new ArrayList<>(locations.size());                
            for(Location l : locations) if(l.getCity() != null) cities.add(preprocessCityName(l.getCity()));
            return cities;
        }
        return Collections.emptyList();
    }
    
    /**
     * Get the distance between two location by formula, described here:
     * http://www.movable-type.co.uk/scripts/latlong.html
     */
    @Override
    public Integer distance(Location location1, Location location2) {
        if(location1 != null && location2 != null) {
            Float lat1 = location1.getLatitude();
            Float lat2 = location2.getLatitude();
            Float lon1 = location1.getLongitude();
            Float lon2 = location2.getLongitude();
            
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
    
    private void checkDataLoaded() {
        if(locationsByZip == null) {
            synchronized(this) {
                if(locationsByZip == null) {
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
    }    
    
    private void loadData() throws Exception {
        locationsByZip = DataUtils.dataFileToMap(
                getDataInputStream(LOCATION_DATABASE), 0,
                new int[]{1, 0, 2, 9, 10}, 
                new String[] {"zip", "countryCode", "city", "latitude", "longitude"},
                String.class, Location.class, "\t", "");
        
        locationsByLatitude = new TreeMap<>();
        locationsByLongitude = new TreeMap<>();
        
        for(Map.Entry<String, List<Location>> e : locationsByZip.entrySet()) {            
            List<Location> locations = e.getValue();
            if(locations != null) {
                for(Location l : locations) {
                    Float lat = l.getLatitude();
                    Float lon = l.getLongitude();
                    String zip = l.getZip();
                    String cc = l.getCountryCode();
                    if(lat != null && lon != null && zip != null && cc != null) {
                        addToMap(locationsByLatitude, lat, l);
                        addToMap(locationsByLongitude, lon, l);
                    }
                }
            }            
        }
    }

    //finds locations by latitude or longitude in a map
    //the logic here is to round the given key to two decimal places,
    //then go through hash map keys and find a key which is between the candidate key and candidate key + 0.01.
    //Then retrieve the locations for the found key
    private Set<Location> findInMap(SortedMap<Float, SortedSet<Location>> map, float candidateKey) {
        // if key is 40.552544 or -74.15088
        int k1Int = (int)(candidateKey * 100);                  //-> 4055 or -7415
        int k2Int = (candidateKey > 0) ? k1Int + 1 : k1Int - 1; //-> 4056 or -7416
        float k1 = (float)(k1Int/100.0);
        float k2 = (float)(k2Int/100.0);    
        float hiKey = Math.max(k1, k2);    //-> 40.5600 or -74.1500
        float loKey = Math.min(k1, k2);    //-> 40.5500 or -74.1600
        
        SortedSet<Float> keys = (SortedSet<Float>)map.keySet();
        Set<Location> locations = new TreeSet<>(new ZipCodeComparator());
        Iterator<Float> i = keys.iterator();
        while(i.hasNext()) {
            Float next = i.next();
            if(next > hiKey) break;
            if(next >= loKey && next <= hiKey) {
                locations.addAll(map.get(next));
            }
        }        
        return locations;
    }
    
    private void addToMap(Map<Float, SortedSet<Location>> map, Float key, Location l) {        
        SortedSet<Location> value = map.get(key);
        if(value == null) {
            value = new TreeSet<>(new ZipCodeComparator());
            map.put(key, value);
        }
        value.add(l);
    }
    
    private static class ZipCodeComparator implements Comparator<Location> {
        @Override
        public int compare(Location l1, Location l2) {
            return l1.getZip().compareTo(l2.getZip());
        }
    }
 
    private InputStream getDataInputStream(String name) {
        InputStream in = getClass().getResourceAsStream(name);
        if(in == null) {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
        }
        return in;
    }
    
    private String preprocessCityName(String cityName) {        
        return cityName.replace(" City", "");
    }
    
}
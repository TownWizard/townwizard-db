package com.townwizard.globaldata.service;

import java.util.List;

import com.townwizard.globaldata.model.Event;
import com.townwizard.globaldata.model.Location;

/**
 * Service which contains method global data retrieval.
 * This service talks to different specific provides such as FB, Google, etc,
 * and gets the data from them.  
 * 
 * If particular data comes from different sources, the final list will contain the merged data
 */
public interface GlobalDataService {

    /**
     * Get events by either zip info, or location, or client IP
     */
    List<Event> getEvents(LocationParams params);
    
    /**
     * Get locations (places) by either zip info, or location, or client IP, and also by
     * distance, optional main category, and optional comma-separated list of categories.
     * If main category and/or categories is null locations of all categories are retrieved
     */    
    List<Location> getLocations(LocationParams params,
            int distanceInMeters, String mainCategory, String categories);
    
    /**
     * Get sorted location categories (such as restaurants, dental, pizza, etc) from locations
     * retrieved by zip/location/ip and distance for some optional main category.
     * If main category is null location categories for all locations are retrieved
     */
    List<String> getLocationCategories(LocationParams params, 
            int distanceInMeters, String mainCategory);
    
    /**
     * Get zip code by either location (latitude and longitude) or IP
     * @param params The location params object which should have ether location or IP parts populated
     */
    String getZipCode(LocationParams params);

    /**
     * A helper class to encapuslate location parameters.
     * A piece of data can be retrieved either by zip/country code, or by latitude/longitude,
     * or by client IP. 
     * 
     * Instances of this class are passed to the service methods, which in turn are responsible of
     * figure out which exactly params to use.
     */
    public static class LocationParams {

        private String zip;
        private String countryCode;
        private double latitude;
        private double longitude;
        private String ip;
        
        public LocationParams(String zip, String countryCode, String location, String ip) {
            this.zip = zip;
            this.countryCode = countryCode;
            this.ip = ip;
            setLatitudeAndLongitude(location);
        }
        
        public boolean isLocationSet() {
            return latitude != 0 && longitude != 0;
        }
        
        public boolean isZipInfoSet() {
            return zip != null && !zip.isEmpty() && countryCode != null && !countryCode.isEmpty();
        }
        
        public boolean isIpSet() {
            return ip != null && !ip.isEmpty();
        }
        
        public double getLatitude() {
            return latitude;
        }
        
        public double getLongitude() {
            return longitude;
        }
        
        public String getZip() {
            return zip;
        }
        
        public String getCountryCode() {
            return countryCode;
        }
        
        public String getIp() {
            return ip;
        }
        
        private void setLatitudeAndLongitude(String location) {
            if(location != null) {
                String[] latAndLon = location.split(",");
                if(latAndLon.length == 2) {
                    try {
                        latitude = Double.parseDouble(latAndLon[0]);
                        longitude = Double.parseDouble(latAndLon[1]);                         
                    } catch (NumberFormatException e) {
                        latitude = longitude = 0;
                    }
                }
            }
        }

    }

}
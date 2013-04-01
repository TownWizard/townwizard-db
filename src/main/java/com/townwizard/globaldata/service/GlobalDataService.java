package com.townwizard.globaldata.service;

import java.util.List;
import java.util.SortedSet;

import com.townwizard.globaldata.model.Event;
import com.townwizard.globaldata.model.Location;

public interface GlobalDataService {

    List<Event> getEvents(LocationParams params);
    List<Location> getLocations(LocationParams params,
            int distanceInMeters, String mainCategory, String categories);
    SortedSet<String> getLocationCategories(LocationParams params, 
            int distanceInMeters, String mainCategory);

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
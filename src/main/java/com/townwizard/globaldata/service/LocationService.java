package com.townwizard.globaldata.service;

import java.util.List;

import com.townwizard.globaldata.model.Location;

public interface LocationService {
    
    List<Location> getLocations(String zip, String countryCode);
    Location getPrimaryLocation(String zip, String countryCode);
    Location getLocation(double latitude, double longitude);
    List<String> getCities(String zip, String countryCode);
    Integer distance(Location location, String zip, String countryCode);
    Integer distance(Location location1, Location location2);

}

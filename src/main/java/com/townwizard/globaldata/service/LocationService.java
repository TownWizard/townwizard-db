package com.townwizard.globaldata.service;

import java.util.List;

import com.townwizard.globaldata.model.directory.Place;

/**
 * Contains location related methods
 */
public interface LocationService {
    /**
     * Get locations by zip info
     */
    List<Place> getLocations(String zip, String countryCode);
    
    /**
     * Get primary location by zip info
     */
    Place getPrimaryLocation(String zip, String countryCode);

    /**
     * Get location by latitude and longitude
     */
    Place getLocation(double latitude, double longitude);

    /**
     * Get list of city names by zip info
     */
    List<String> getCities(String zip, String countryCode);

    /**
     * Return distance in meters between two locations
     */
    Integer distance(Place location1, Place location2);

}

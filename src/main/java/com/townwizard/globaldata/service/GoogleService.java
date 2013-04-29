package com.townwizard.globaldata.service;

import java.util.List;

import com.townwizard.globaldata.model.directory.Location;

/**
 * Contains methods to retrieve data from Google
 */
public interface GoogleService {
    
    /**
     * Get Google locations (places) by latitude, longitude, and distance
     */
    List<Location> getLocations(double latitude, double longitude, int distanceInMeters);

}

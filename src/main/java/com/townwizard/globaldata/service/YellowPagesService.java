package com.townwizard.globaldata.service;

import java.util.List;

import com.townwizard.globaldata.model.Location;

/**
 * Contains methods to retrieve data from Yellow Pages
 */
public interface YellowPagesService {

    /**
     * Get Yellow Pages locations (places) by search term, zip, and distance
     */
    List<Location> getLocations(String term, String zip, double distanceInMiles);
    
}

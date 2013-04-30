package com.townwizard.globaldata.service.provider;

import java.util.List;

import com.townwizard.globaldata.model.directory.Place;

/**
 * Contains methods to retrieve data from Yellow Pages
 */
public interface YellowPagesService {

    /**
     * Get Yellow Pages locations (places) by search term, zip, and distance
     */
    List<Place> getLocations(String term, String zip, double distanceInMiles);
    
}

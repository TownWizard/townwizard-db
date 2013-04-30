package com.townwizard.globaldata.service.provider;

import java.util.List;

import com.townwizard.globaldata.model.directory.Place;

/**
 * Contains methods to retrieve data from Google
 */
public interface GoogleService {
    
    /**
     * Get Google places by latitude, longitude, and distance
     */
    List<Place> getPlaces(double latitude, double longitude, int distanceInMeters);

}

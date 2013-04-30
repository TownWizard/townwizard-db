package com.townwizard.globaldata.service.provider;

import java.util.List;

import com.townwizard.globaldata.model.Event;
import com.townwizard.globaldata.model.directory.Place;

/**
 * Contains methods to retrieve data from Facebook
 */
public interface FacebookService {

    /**
     * Get Facebook events for list of search terms
     */
    List<Event> getEvents(List<String> terms);
    
    /**
     * Get Facebook places given latitude, longitude, and distance
     */
    List<Place> getPlaces(double latitude, double longitude, int distanceInMeters);
    
}

package com.townwizard.globaldata.service;

import java.util.List;

import com.townwizard.globaldata.model.Event;
import com.townwizard.globaldata.model.Location;
import com.townwizard.globaldata.model.directory.Place;

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
    List<Event> getEvents(Location location);
    
    /**
     * Get places by either zip info, or location, or client IP for a given category or term,
     * which is mandatory.  Return an empty list of categoryOrTerm is null or empty.
     */    
    List<Place> getPlaces(Location location, String categoryOrTerm);
    
    /**
     * Get sorted place categories (such as restaurants, dental, pizza, etc) from places
     * for some optional main category.
     * If main category is null all place categories are retrieved
     */
    List<String> getPlaceCategories(String mainCategory);
    
    /**
     * Get zip code by either location (latitude and longitude) or IP
     * @param params The location params object which should have ether location or IP parts populated
     */
    String getZipCode(Location location);

}
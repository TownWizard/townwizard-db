package com.townwizard.globaldata.service;

import java.util.List;

import com.townwizard.globaldata.model.directory.Place;
import com.townwizard.globaldata.model.directory.PlaceCategory;
import com.townwizard.globaldata.model.directory.PlaceIngest;

public interface PlaceService {
    
    /**
     * Return the list of all place categories
     */
    List<PlaceCategory> getAllPlaceCategories();
    
    /**
     * Return the list of places related to the given ingest.
     */
    List<Place> getPlaces(PlaceIngest ingest);    

    /**
     * Check if an ingest exists for the given zip, country code, and the term.
     * If not, create a new ingest (which is unfinished).
     * Return the old or new ingest. 
     */
    PlaceIngest getIngest(
            String zipCode, String countryCode, int distanceInMeters, String categoryOrTerm);
    
    /**
     * Do whatever is necessary to do to ingest places (save places, update categories, etc) for
     * a given unfinished ingest.
     * 
     * If the ingest given is finished and valid, do nothing
     */
    void completeIngest(PlaceIngest ingest, List<Place> places); 
}

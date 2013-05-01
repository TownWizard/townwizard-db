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
     * Return sorted list of all place category names
     */
    List<String> getAllPlaceCategoryNames();
    
    /**
     * Return the list of places related to the given ingest.
     */
    List<Place> getPlaces(PlaceIngest ingest);    

    /**
     * Check if an ingest exists for the given zip, country code, and the term.
     * If an ingest exist, but not done, return ingest with the state IN_PROGRESS.
     * If no ingest exist, create one and return it with the state NEW.
     * Othewise, return the ingest with the state DONE.
     */
    PlaceIngest getIngest(
            String zipCode, String countryCode, int distanceInMeters, String categoryOrTerm);
    
    /**
     * Save ingest and associate places with it.
     */
    void saveIngest(PlaceIngest ingest, List<Place> places); 
}

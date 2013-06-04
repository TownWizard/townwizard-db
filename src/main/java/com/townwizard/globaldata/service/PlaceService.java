package com.townwizard.globaldata.service;


import java.util.List;

import com.townwizard.globaldata.model.directory.PlaceCategory;
import com.townwizard.globaldata.model.directory.ZipIngest;

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
     * Get places for zip info, and a category.  If places are available in the local DB, get them
     * from their.  Otherwise, bring them from the source and start places ingest.
     * 
     *  Page number is an optional parameter, if it is null, then all places will be retrieved.
     */
    Object[] getPlaces(String zipCode, String countryCode, String categoryOrTerm, Integer pageNum);

    /**
     * Get zip ingest.
     * If it doesn't exist, create a new with status NEW.
     * If ingest exists, but not done, return ingest with status IN_PROGRESS
     * If ingest exists, but expired, delete it, and create a new ingest with status NEW
     * Or return the ingest with status READY
     */
    ZipIngest getZipIngest(String zip, String countryCode);
    
    /**
     * Update zip ingest.
     */
    void updateZipIngest(ZipIngest zipIngest);
}

package com.townwizard.globaldata.dao;

import java.util.List;

import com.townwizard.db.dao.AbstractDao;
import com.townwizard.globaldata.model.directory.Place;
import com.townwizard.globaldata.model.directory.PlaceCategory;
import com.townwizard.globaldata.model.directory.PlaceIngest;
import com.townwizard.globaldata.model.directory.ZipIngest;

/**
 * Contains methods to get/save locations (places) in our local DB
 */
public interface PlaceDao extends AbstractDao {
    
    /**
     * Return the list of all place categories
     */
    List<PlaceCategory> getAllPlaceCategories();
    
    /**
     * Get place ingest by zip, country code, and category or term.
     * First, search for a category, then, search for a term.
     */
    PlaceIngest getIngest(String zip, String countryCode, String categoryOrTerm);
    
    /**
     * Get places for a given ingest
     */
    List<Place> getPlaces(PlaceIngest ingest);

    /**
     * Delete places associated with the ingest (and only with this ingest),
     * and then delete ingest itself.
     */
    void deleteIngest(PlaceIngest ingest);
    
    /**
     * Delete zip ingest and associated ingests with places
     */
    void deleteZipIngest(ZipIngest ingest);
   
    /**
     * Get zip ingest.
     */
    ZipIngest getZipIngest(String zip, String countryCode);
    
}

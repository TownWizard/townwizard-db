package com.townwizard.globaldata.dao;

import java.util.List;

import com.townwizard.db.dao.AbstractDao;
import com.townwizard.globaldata.model.directory.Place;
import com.townwizard.globaldata.model.directory.PlaceCategory;
import com.townwizard.globaldata.model.directory.PlaceIngest;

/**
 * Contains methods to get/save locations (places) in our local DB
 */
public interface PlaceDao extends AbstractDao {
    
    /**
     * Get a places ingest object from the DB by zip info.
     * This method actually brings all the locations associated with this location ingest, so
     * should be treated as "get locations" method. 
     */
    PlaceIngest getPlaceIngest(String zip, String countryCode);
    
    /**
     * Return the list of all place categories
     */
    List<PlaceCategory> getAllPlaceCategories();
    
    /**
     * Get sorted list of category names for an ingest 
     */
    List<String> getPlaceCategories(Long ingestId);
    
    /** 
     * The list of places given to this method can be a mix of old (associated with some
     * other location ingest) and new places.
     * 
     *  The method will save the new places and will associate both old and new places 
     *  with the passed place ingest.
     */
    void savePlaces(List<Place> places, PlaceIngest ingest);

}

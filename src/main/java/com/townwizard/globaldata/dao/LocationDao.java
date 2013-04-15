package com.townwizard.globaldata.dao;

import java.util.List;

import com.townwizard.db.dao.AbstractDao;
import com.townwizard.globaldata.model.Location;
import com.townwizard.globaldata.model.LocationCategory;
import com.townwizard.globaldata.model.LocationIngest;

/**
 * Contains methods to get/save locations (places) in our local DB
 */
public interface LocationDao extends AbstractDao {
    
    /**
     * Get a location ingest object from the DB by zip info.
     * This method actually brings all the locations associated with this location ingest, so
     * should be treated as "get locations" method. 
     */
    LocationIngest getLocationIngest(String zip, String countryCode);
    
    /**
     * Return the list of all location categories
     */
    List<LocationCategory> getAllLocationCategories();
    
    /**
     * Get sorted list of category names for an ingest 
     */
    List<String> getLocationCategories(Long ingestId);
    
    /** 
     * The list of locations given to this method can be a mix of old (associated with some
     * other location ingest) and new locations.
     * 
     *  The method will save the new locations and will associate both old and new locations 
     *  with the passed location ingest.
     */
    void saveLocations(List<Location> locations, LocationIngest ingest);

}

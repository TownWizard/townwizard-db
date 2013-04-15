package com.townwizard.globaldata.dao;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import com.townwizard.db.dao.AbstractDaoHibernateImpl;
import com.townwizard.db.util.CollectionUtils;
import com.townwizard.globaldata.model.Location;
import com.townwizard.globaldata.model.LocationCategory;
import com.townwizard.globaldata.model.LocationIngest;

/**
 * Hibernate implementation of LocationDao
 */
@Component("locationDao")
public class LocationDaoHibernateImpl extends AbstractDaoHibernateImpl implements LocationDao {

    @Override
    public LocationIngest getLocationIngest(String zip, String countryCode) {
        return (LocationIngest)getSession()
                .createQuery("from LocationIngest where zip = :zip and countryCode = :countryCode")
                .setString("zip", zip)
                .setString("countryCode", countryCode)
                .uniqueResult();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<LocationCategory> getAllLocationCategories() {
        return getSession().createQuery("from LocationCategory").list();
    }
    
    @Override
    public List<String> getLocationCategories(Long ingestId) {
        String sql = 
                "SELECT DISTINCT c.name FROM LocationCategory c " + 
                "JOIN Location_LocationCategory llc ON c.id = llc.location_category_id " +  
                "JOIN Location l ON llc.location_id = l.id " + 
                "JOIN Location_LocationIngest lli ON l.id = lli.location_id " + 
                "JOIN LocationIngest li ON lli.location_ingest_id = li.id " + 
                "WHERE li.id = ?";

        Session session = getSession();
        session.flush();
        
        @SuppressWarnings("unchecked")
        List<String> result = session.createSQLQuery(sql).setLong(0, ingestId).list();
        Collections.sort(result);        
        return result;
    }
    
    @Override
    public void saveLocations(List<Location> locations, LocationIngest ingest) {

        List<String> externalIds = new ArrayList<>(locations.size());
        for(Location location : locations) {
            externalIds.add(location.getExternalId());
        }
        
        Map<String, List<Location>> locationsFromDb = getLocationsByExternalIds(externalIds);
        
        List<Location> oldLocations = new ArrayList<>();
        List<Location> newLocations = new ArrayList<>();
        
        for(Location location : locations) {
            String externalId = location.getExternalId();
            List<Location> locationsById = locationsFromDb.get(externalId);
            if(locationsById == null) {
                newLocations.add(location);                
            } else {
                boolean exists = false;
                for(Location fromDb : locationsById) {
                    if(fromDb.getSource().equals(location.getSource())) {
                        exists = true;
                        oldLocations.add(fromDb);
                        break;
                    }
                }
                if(!exists) {
                    newLocations.add(location);
                }
            }
        }
        
        for(Location l : oldLocations) {
            l.addIngest(ingest);
            update(l);
        }
        
        if(!newLocations.isEmpty()) {
            createMissingCategories(newLocations);
            Map<String, LocationCategory> allCategories = getLocationCategoriesMap();
            
            for(Location location : newLocations) {
                for(String categoryName : location.extractCategoryNames()) {
                    LocationCategory c = allCategories.get(categoryName);
                    location.addCategory(c);
                }
                location.addIngest(ingest);
                create(location);
            }
        }
    }
    
    private Map<String, List<Location>> getLocationsByExternalIds(List<String> externalIds) {
        @SuppressWarnings("unchecked")
        List<Location> locations = getSession()
            .createQuery("from Location where externalId in (" + CollectionUtils.join(externalIds, ",", "'") + ")")
            .list();
        
        Map<String, List<Location>> result = new HashMap<>();
        for(Location l : locations) {
            String externalId = l.getExternalId();
            List<Location> locationsById = result.get(externalId);
            if(locationsById == null) {
                locationsById = new LinkedList<>();
                result.put(externalId, locationsById);
            }
            locationsById.add(l);
        }
        return result;
    }
    
    private Set<String> collectCategoryNames(List<Location> locations) {
        Set<String> result = new HashSet<>();
        for(Location l : locations) {
            result.addAll(l.extractCategoryNames());
        }
        return result;
    }
    
    private Map<String, LocationCategory> getLocationCategoriesMap() {
        Map<String, LocationCategory> result = new HashMap<>();
        @SuppressWarnings("unchecked")
        List<LocationCategory> allCategories = getSession().createQuery("from LocationCategory").list();
        for(LocationCategory c : allCategories) {
            result.put(c.getName(), c);
        }
        return result;
    }
    
    private void createMissingCategories(List<Location> locations) {
        Set<String> allCategoryNames = collectCategoryNames(locations);
        Map<String, LocationCategory> allCategories = getLocationCategoriesMap();
        Session session = getSession();
        
        for(String categoryName : allCategoryNames) {
            LocationCategory c = allCategories.get(categoryName);
            if(c == null) {
                c = new LocationCategory();
                c.setName(categoryName);
                session.save(c);
            }
        }
    }

}

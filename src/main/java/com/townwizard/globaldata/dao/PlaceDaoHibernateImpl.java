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
import com.townwizard.globaldata.model.directory.Place;
import com.townwizard.globaldata.model.directory.PlaceCategory;
import com.townwizard.globaldata.model.directory.PlaceIngest;

/**
 * Hibernate implementation of PlaceDao
 */
@Component("placeDao")
public class PlaceDaoHibernateImpl extends AbstractDaoHibernateImpl implements PlaceDao {

    @Override
    public PlaceIngest getPlaceIngest(String zip, String countryCode) {
        return (PlaceIngest)getSession()
                .createQuery("from PlaceIngest where zip = :zip and countryCode = :countryCode")
                .setString("zip", zip)
                .setString("countryCode", countryCode)
                .uniqueResult();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<PlaceCategory> getAllPlaceCategories() {
        return getSession().createQuery("from PlaceCategory").list();
    }
    
    @Override
    public List<String> getPlaceCategories(Long ingestId) {
        String sql = 
                "SELECT DISTINCT c.name FROM Category c " + 
                "JOIN Location_Category llc ON c.id = llc.category_id " +  
                "JOIN Location l ON llc.location_id = l.id " + 
                "JOIN Location_Ingest lli ON l.id = lli.location_id " + 
                "JOIN Ingest li ON lli.ingest_id = li.id " + 
                "WHERE li.id = ?";

        Session session = getSession();
        session.flush();
        
        @SuppressWarnings("unchecked")
        List<String> result = session.createSQLQuery(sql).setLong(0, ingestId).list();
        Collections.sort(result);        
        return result;
    }
    
    @Override
    public void savePlaces(List<Place> locations, PlaceIngest ingest) {

        List<String> externalIds = new ArrayList<>(locations.size());
        for(Place location : locations) {
            externalIds.add(location.getExternalId());
        }
        
        Map<String, List<Place>> locationsFromDb = getLocationsByExternalIds(externalIds);
        
        List<Place> oldLocations = new ArrayList<>();
        List<Place> newLocations = new ArrayList<>();
        
        for(Place location : locations) {
            String externalId = location.getExternalId();
            List<Place> locationsById = locationsFromDb.get(externalId);
            if(locationsById == null) {
                newLocations.add(location);                
            } else {
                boolean exists = false;
                for(Place fromDb : locationsById) {
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
        
        for(Place l : oldLocations) {
            l.addIngest(ingest);
            update(l);
        }
        
        if(!newLocations.isEmpty()) {
            createMissingCategories(newLocations);
            Map<String, PlaceCategory> allCategories = getLocationCategoriesMap();
            
            for(Place location : newLocations) {
                for(String categoryName : location.extractCategoryNames()) {
                    PlaceCategory c = allCategories.get(categoryName);
                    location.addCategory(c);
                }
                location.addIngest(ingest);
                create(location);
            }
        }
    }
    
    private Map<String, List<Place>> getLocationsByExternalIds(List<String> externalIds) {
        @SuppressWarnings("unchecked")
        List<Place> locations = getSession()
            .createQuery("from Place where externalId in (" + CollectionUtils.join(externalIds, ",", "'") + ")")
            .list();
        
        Map<String, List<Place>> result = new HashMap<>();
        for(Place l : locations) {
            String externalId = l.getExternalId();
            List<Place> locationsById = result.get(externalId);
            if(locationsById == null) {
                locationsById = new LinkedList<>();
                result.put(externalId, locationsById);
            }
            locationsById.add(l);
        }
        return result;
    }
    
    private Set<String> collectCategoryNames(List<Place> locations) {
        Set<String> result = new HashSet<>();
        for(Place l : locations) {
            result.addAll(l.extractCategoryNames());
        }
        return result;
    }
    
    private Map<String, PlaceCategory> getLocationCategoriesMap() {
        Map<String, PlaceCategory> result = new HashMap<>();
        @SuppressWarnings("unchecked")
        List<PlaceCategory> allCategories = getSession().createQuery("from PlaceCategory").list();
        for(PlaceCategory c : allCategories) {
            result.put(c.getName(), c);
        }
        return result;
    }
    
    private void createMissingCategories(List<Place> locations) {
        Set<String> allCategoryNames = collectCategoryNames(locations);
        Map<String, PlaceCategory> allCategories = getLocationCategoriesMap();
        Session session = getSession();
        
        for(String categoryName : allCategoryNames) {
            PlaceCategory c = allCategories.get(categoryName);
            if(c == null) {
                c = new PlaceCategory();
                c.setName(categoryName);
                session.save(c);
            }
        }
    }

}

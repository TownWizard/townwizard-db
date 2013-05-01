package com.townwizard.globaldata.dao;


import java.util.ArrayList;
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
    @SuppressWarnings("unchecked")
    public List<PlaceCategory> getAllPlaceCategories() {
        return getSession().createQuery("from PlaceCategory").list();
    }
    
    @Override
    public PlaceCategory getCategory(String name) {
        return (PlaceCategory)getSession().createQuery("from PlaceCategory where name = :name")
                .setString("name", name).uniqueResult();
    }
    
    @Override
    public List<Place> getPlaces(PlaceIngest ingest) {
        Session session = getSession();
        PlaceIngest fromDB = (PlaceIngest)session.get(PlaceIngest.class, ingest.getId());
        Set<Place> ingestPlaces = fromDB.getPlaces();
        List<Place> places = new ArrayList<>(ingestPlaces.size());
        places.addAll(ingestPlaces);
        return places;
    }
    
    @Override
    public PlaceIngest getPlaceIngest(String zip, String countryCode, String categoryOrTerm) {
        @SuppressWarnings("unchecked")
        List<Object[]> ingests = getSession()
                .createQuery("from PlaceIngest i left join i.placeCategory c " + 
                            "where i.zip = :zip and i.countryCode = :countryCode " + 
                            "and (c.name = :categoryOrTerm or i.term = lower(:categoryOrTerm))")
                .setString("zip", zip)
                .setString("countryCode", countryCode)
                .setString("categoryOrTerm", categoryOrTerm)
                .list();
        
        if(!ingests.isEmpty()) {
            Object[] ingestAndCategory = ingests.get(0); 
            return (PlaceIngest) ingestAndCategory[0];
        }
        return null;
    }
    
    @Override
    public void deleteIngest(PlaceIngest ingest) {
        Session session = getSession();
        Set<Place> places = ingest.getPlaces();
        for(Place p : places) {
            if(p.getIngests().size() == 1) {
                session.delete(p);
            }
        }
        session.delete(ingest);
    }
    
    @Override
    public void saveIngest(PlaceIngest ingest, List<Place> locations) {

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
        
        update(ingest);
    }

    ///////////////////////////// private methods //////////////////////////////////////////
    
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
    
    private Set<String> collectCategoryNames(List<Place> locations) {
        Set<String> result = new HashSet<>();
        for(Place l : locations) {
            result.addAll(l.extractCategoryNames());
        }
        return result;
    }
    
}

package com.townwizard.globaldata.dao;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import com.townwizard.db.dao.AbstractDaoHibernateImpl;
import com.townwizard.db.logger.Log;
import com.townwizard.db.util.CollectionUtils;
import com.townwizard.globaldata.model.directory.Place;
import com.townwizard.globaldata.model.directory.PlaceCategory;
import com.townwizard.globaldata.model.directory.PlaceIngest;
import com.townwizard.globaldata.model.directory.ZipIngest;

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
    public List<Place> getPlaces(PlaceIngest ingest) {
        @SuppressWarnings("cast")
        PlaceIngest fromDB = (PlaceIngest)getById(PlaceIngest.class, ingest.getId());
        Set<Place> ingestPlaces = fromDB.getPlaces();
        List<Place> places = new ArrayList<>(ingestPlaces.size());
        if(!ingestPlaces.isEmpty()) {
            places.addAll(ingestPlaces);
            populatePlacesWithCategories(places);
        }
        return places;
    }
    
    @Override
    public PlaceIngest getIngest(String zip, String countryCode, String categoryOrTerm) {
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
        
        session.createSQLQuery("DELETE FROM Location_Ingest WHERE ingest_id = :ingest_id")
            .setLong("ingest_id", ingest.getId()).executeUpdate();
        
        session.createSQLQuery("DELETE FROM Ingest WHERE id = :ingest_id")
            .setLong("ingest_id", ingest.getId()).executeUpdate();

        session.createSQLQuery(
                "DELETE FROM Location_Category " +
                "WHERE location_id IN (" +
                    "SELECT id FROM Location " +
                    "WHERE id NOT IN (SELECT location_id FROM Location_Ingest))")
            .executeUpdate();

        session.createSQLQuery("DELETE FROM Location WHERE id NOT IN (SELECT location_id FROM Location_Ingest)")
            .executeUpdate();
        
        session.flush();
    }
    
    @Override
    public ZipIngest getZipIngest(String zip, String countryCode) {
        return (ZipIngest)getSession()
                .createQuery("from ZipIngest where zip = :zip and countryCode = :countryCode")
                .setString("zip", zip).setString("countryCode", countryCode).uniqueResult();
        
    }
    
    @Override
    public void deleteZipIngest(ZipIngest ingest) {
        Session session = getSession();
        
        session.createSQLQuery("DELETE FROM Location_Ingest WHERE ingest_id IN " +
        		"(SELECT id FROM Ingest WHERE zip = :zip AND country_code = :countryCode)")
            .setString("zip", ingest.getZip()).setString("countryCode", ingest.getCountryCode())
            .executeUpdate();
        
        session.createSQLQuery("DELETE FROM Ingest WHERE zip = :zip AND country_code = :countryCode")
            .setString("zip", ingest.getZip()).setString("countryCode", ingest.getCountryCode())
            .executeUpdate();        
        
        session.createSQLQuery(
                "DELETE FROM Location_Category " +
                "WHERE location_id IN (" +
                    "SELECT id FROM Location " +
                    "WHERE id NOT IN (SELECT location_id FROM Location_Ingest))")
            .executeUpdate();

        session.createSQLQuery("DELETE FROM Location WHERE id NOT IN (SELECT location_id FROM Location_Ingest)")
            .executeUpdate();
        
        session.createSQLQuery("DELETE FROM ZipIngest WHERE id = :id")
            .setLong("id", ingest.getId()).executeUpdate();        
        
        session.flush();
    }
    
    ///////////////////////////// private methods //////////////////////////////////////////
    
    private void populatePlacesWithCategories(List<Place> places) {
        List<Long> placeIds = new ArrayList<>(places.size());
        for(Place p : places) placeIds.add(p.getId());
            
        Session session = getSession();
        
        @SuppressWarnings("unchecked")
        List<Object[]> idMap = session.createSQLQuery("SELECT location_id, category_id FROM Location_Category " +  
                "WHERE location_id IN (" + CollectionUtils.join(placeIds) + ")").list();
        
        Set<Long> categoryIds = new HashSet<>();
        Map<Long, List<Long>> placeToCategories = new HashMap<>();
        for(Object[] o : idMap) {
            Long locationId = ((BigInteger)o[0]).longValue();
            Long categoryId = ((BigInteger)o[1]).longValue();
            categoryIds.add(categoryId);
            
            List<Long> categoriesForLocation = placeToCategories.get(locationId);
            if(categoriesForLocation == null) {
                categoriesForLocation = new ArrayList<>();                
                placeToCategories.put(locationId, categoriesForLocation);
            }
            categoriesForLocation.add(categoryId);
        }
        
        @SuppressWarnings("unchecked")
        List<PlaceCategory> categories = session.createQuery("from PlaceCategory where id in (" 
                + CollectionUtils.join(categoryIds) + ")").list();
        Map<Long, PlaceCategory> idToCategory = new HashMap<>();
        for(PlaceCategory c : categories) idToCategory.put(c.getId(), c);
        
        for(Place p : places) {
            p.setCategories(new HashSet<PlaceCategory>());
            List<Long> placeCategoryIds = placeToCategories.get(p.getId());
            if(placeCategoryIds != null) {
                for(Long placeCategoryId : placeCategoryIds) {
                    p.getCategories().add(idToCategory.get(placeCategoryId));
                }
            } else {
                Log.warning("/////////////////// No categories exist for place: " + p);
            }
        }
    }

}

package com.townwizard.globaldata.ingest.place;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.townwizard.db.logger.Log;
import com.townwizard.globaldata.model.directory.Place;
import com.townwizard.globaldata.model.directory.PlaceCategory;
import com.townwizard.globaldata.model.directory.PlaceIngest;

public abstract class AbstractIngester implements Ingester {
    
    private String zipCode;
    private String countryCode;
    private String categoryOrTerm;
    private boolean highPriorityIngest;
    private boolean highPriorityIngestDone;
    private Map<String, PlaceCategory> categoryNameToCategory;
    private LinkedList<String> submittionList;
    private Set<String> processedCategories;
    
    public AbstractIngester(
            String zipCode, String countryCode, List<PlaceCategory> categories, String categoryOrTerm) {
        this.zipCode = zipCode;
        this.countryCode = countryCode;
        this.categoryOrTerm = categoryOrTerm;
        highPriorityIngest = categoryOrTerm != null;
    
        categoryNameToCategory = new HashMap<>();
        for(PlaceCategory c : categories) {
            categoryNameToCategory.put(c.getName(), c);
        }
        submittionList = new LinkedList<>();
        submittionList.addAll(categoryNameToCategory.keySet());
        processedCategories = new HashSet<>();        
    }
    
    @Override
    public String getZipCode() {
        return zipCode;
    }
    
    @Override
    public String getCountryCode() {
        return countryCode;
    }
    
    @Override
    public boolean hasNextCategory() {
        if(highPriorityIngest) return !highPriorityIngestDone;
        return submittionList.size() > 0;
    }
    
    @Override
    public String getNextCategory() {
        if(highPriorityIngest) return categoryOrTerm;
        return submittionList.removeFirst();
    }
    
    @Override
    public boolean allDone() {
        if(highPriorityIngest) return highPriorityIngestDone;
        return processedCategories.size() == categoryNameToCategory.size();
    }
    
    @Override
    public void ingest(IngestTask task) {
        try {
            //go through places category names and
            //1) collect new categories
            //2) create a map of categories (including new ones) to places
            Map<String, Set<Place>> categoryToPlaces = new HashMap<>();
            Set<String> newCategories = new HashSet<>();
            for(Place p : task.getPlaces()) {
                for(String cName : p.extractCategoryNames()) {
                    if(!categoryNameToCategory.containsKey(cName)) {
                        newCategories.add(cName);
                    }
                    Set<Place> categoryPlaces = categoryToPlaces.get(cName);
                    if(categoryPlaces == null) {
                        categoryPlaces = new TreeSet<>(new Place.SourceAndExternalIdComparator());
                        categoryToPlaces.put(cName, categoryPlaces);
                        }
                    categoryPlaces.add(p);
                }
            }

            beforeIngest();
            PlaceIngest ingest = createIngest(task);
            
            markIngestInProgress(ingest);            
            mergePlaces(task.getPlaces());            
            mapPlacesToIngest(ingest);
            addNewCategories(newCategories);
            mapPlacesToCategories(categoryToPlaces);
            markIngestReady(ingest);
            
            if(highPriorityIngest) {
                highPriorityIngestDone = true;
                Log.debug("Ingested category '" + task.getCategory() + "' for zip " + task.getZipCode());
            } else {
                processedCategories.add(task.getCategory());
                /*
                if(Log.isDebugEnabled()) {
                    Log.debug("Zip " + task.getZipCode() + ": " + 
                        (categoryNameToCategory.size() - processedCategories.size()) + 
                        " categories left after ingesting " + task.getPlaces().size() + 
                        " places for '" + task.getCategory() + "'");
                }
                */
            }
        } catch (Exception e) {
            Log.exception(e);
            onError();
        } finally {
            afterIngest();
        }
    }
    
    protected abstract void markIngestInProgress(PlaceIngest ingest);
    protected abstract void mergePlaces(Collection<Place> places);
    protected abstract void mapPlacesToIngest(PlaceIngest ingest);
    protected abstract void mapPlacesToCategories(Map<String, Set<Place>> categoryToPlaces);
    protected abstract void addNewCategories(Set<String> newCategoryNames);
    protected abstract void markIngestReady(PlaceIngest ingest);
    protected abstract void beforeIngest();
    protected abstract void afterIngest();
    protected abstract void onError();
    
    private  PlaceIngest createIngest(IngestTask task) {
        PlaceIngest ingest = new PlaceIngest();
        ingest.setZip(getZipCode());
        ingest.setCountryCode(getCountryCode());
        PlaceCategory category = categoryNameToCategory.get(task.getCategory());
        if(category != null) {        
            ingest.setPlaceCategory(category);
        } else {
            ingest.setTerm(task.getCategory());
        }
        ingest.setPlaces(new HashSet<>(task.getPlaces()));
        return ingest;        
    }
    
}

package com.townwizard.globaldata.ingest.place;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.townwizard.globaldata.model.directory.Place;
import com.townwizard.globaldata.model.directory.PlaceCategory;

public abstract class AbstractIngester implements Ingester {
    
    private String zipCode;
    private String countryCode;
    private PlaceCategory[] categories;
    private int nextCategoryIndex = 0;
    
    private Map<String, PlaceCategory> categoryNameToCategory;
    private Map<String, Set<Place>> categoryNameToPlaces;
    private Set<String> newCategoryNames;
    private Set<String> existingCategoryNames;
    private Set<Place> uniquePlaces;

    public AbstractIngester(String zipCode, String countryCode, List<PlaceCategory> categories) {
        if(zipCode == null || countryCode == null || categories == null || categories.isEmpty()) {
            throw new IllegalArgumentException("Illegal arguments given to ingester");
        }
        this.zipCode = zipCode;
        this.countryCode = countryCode;
        this.categories = categories.toArray(new PlaceCategory[]{});
        
        Place.SourceAndExternalIdComparator placeComparator = new Place.SourceAndExternalIdComparator();
        categoryNameToCategory = new HashMap<>();
        categoryNameToPlaces = new HashMap<>();
        newCategoryNames = new HashSet<>();
        existingCategoryNames = new HashSet<>();
        uniquePlaces = new TreeSet<>(placeComparator);        
        for(PlaceCategory c : categories) {
            String name = c.getName();
            categoryNameToCategory.put(name, c);
            existingCategoryNames.add(name);
            categoryNameToPlaces.put(name, new TreeSet<>(placeComparator));
        }
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
        return nextCategoryIndex < categories.length - 1;
    }
    
    @Override
    public PlaceCategory getNextCategory() {
        return categories[nextCategoryIndex++];
    }
    
    @Override
    public void addProcessedIngestTaskResult(IngestTask task) {
        for(Place p : task.getPlaces()) {
            uniquePlaces.add(p);
            for(String cName : p.extractCategoryNames()) {
                categoryNameToPlaces.get(cName).add(p);
                if(!existingCategoryNames.contains(cName)) {
                    newCategoryNames.add(cName);
                }
                
            }
        }
    }
    
}

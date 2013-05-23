package com.townwizard.globaldata.ingest.place;

import java.util.List;

import com.townwizard.globaldata.model.directory.Place;

public final class IngestTask {
    private String zipCode;
    private String countryCode;
    private String category;
    private List<Place> places;    
    
    public IngestTask(String zipCode, String countryCode, String category, List<Place> places) {
        this.zipCode = zipCode;
        this.countryCode = countryCode;            
        this.category = category;
        this.places = places;        
    }

    public String getZipCode() {
        return zipCode;
    }
    public String getCountryCode() {
        return countryCode;
    }
    public String getCategory() {
        return category;
    }
    public List<Place> getPlaces() {
        return places;
    }
    
}
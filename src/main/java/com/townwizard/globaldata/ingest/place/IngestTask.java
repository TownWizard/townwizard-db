package com.townwizard.globaldata.ingest.place;

import java.util.List;

import com.townwizard.globaldata.model.directory.Place;

public final class IngestTask {
    private String zipCode;
    private String countryCode;
    private String category;
    private List<Place> places;
    private boolean highPriority;
    
    public IngestTask(String zipCode, String countryCode, String category, boolean highPriority, List<Place> places) {
        this.zipCode = zipCode;
        this.countryCode = countryCode;            
        this.category = category;
        this.highPriority = highPriority;
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
    public boolean isHighPriority() {
        return highPriority;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        result = prime * result + ((countryCode == null) ? 0 : countryCode.hashCode());
        result = prime * result + ((zipCode == null) ? 0 : zipCode.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IngestTask other = (IngestTask) obj;
        if (category == null) {
            if (other.category != null)
                return false;
        } else if (!category.equals(other.category))
            return false;
        if (countryCode == null) {
            if (other.countryCode != null)
                return false;
        } else if (!countryCode.equals(other.countryCode))
            return false;
        if (zipCode == null) {
            if (other.zipCode != null)
                return false;
        } else if (!zipCode.equals(other.zipCode))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Ingest: (" + zipCode + ", " + category + ") - " + places.size() + " places";
    }
    
}
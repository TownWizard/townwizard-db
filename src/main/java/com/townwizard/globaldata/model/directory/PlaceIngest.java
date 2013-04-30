package com.townwizard.globaldata.model.directory;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.townwizard.db.model.AuditableEntity;

/**
 * Location ingest is an object which is related to locations collected at particular time,
 * by a particular zip code, and a particular distance.
 * 
 * Location and LocationIngest classes have many-to-many relationships.
 * 
 * The notion of location ingest is necessary in order to cache in the DB and keep track of the locations
 * already downloaded from the provider.
 * 
 * Whenever a request for locations is made, the application first checks if such a request has
 * been already made (by comparing last request time, distance, and zip code)
 * and if yes, the locations are loaded from the DB rather than from the source.
 */
@Entity
@Table(name = "Ingest")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="locations")
public class PlaceIngest extends AuditableEntity {
    
    public static enum Status {NEW, IN_PROGRESS, DONE}

    private static final long serialVersionUID = -5910483030029302936L;
    
    private String zip;
    private String countryCode;
    private String term;
    private Integer distance;
    private Status status;
    
    @OneToOne(fetch = FetchType.EAGER) @JoinColumn(name = "categoryId")
    private PlaceCategory placeCategory;
    
    @ManyToMany (fetch=FetchType.LAZY)    
    @JoinTable (
            name = "Location_Ingest",
            joinColumns= {@JoinColumn (name="ingest_id")},
            inverseJoinColumns = {@JoinColumn(name="location_id")}
    )    
    private Set<Place> places;    
    
    public String getZip() {
        return zip;
    }
    public void setZip(String zip) {
        this.zip = zip;
    }
    public String getCountryCode() {
        return countryCode;
    }
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    public String getTerm() {
        return term;
    }
    public void setTerm(String term) {
        this.term = term;
    }
    public Integer getDistance() {
        return distance;
    }
    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }
    public void setDistance(Integer distance) {
        this.distance = distance;
    }
    public Set<Place> getPlaces() {
        return places;
    }
    public void setPlaces(Set<Place> places) {
        this.places = places;
    }
    public PlaceCategory getPlaceCategory() {
        return placeCategory;
    }
    public void setPlaceCategory(PlaceCategory placeCategory) {
        this.placeCategory = placeCategory;
    }
    /**
     * Convenience method to add place to this object.  This method will not set both
     * sides of the relationship, this is done on the Place side.
     */
    public void addPlace(Place l) {
        if(places == null) {
            places = new HashSet<>();
        }
        places.add(l);        
    }

}

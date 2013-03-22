package com.townwizard.globaldata.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.townwizard.db.model.AuditableEntity;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="locations")
public class LocationIngest extends AuditableEntity {

    private static final long serialVersionUID = -5910483030029302936L;
    
    private String zip;
    private String countryCode;
    private Integer distance;
    
    @ManyToMany (fetch=FetchType.LAZY)    
    @JoinTable (
            name = "Location_LocationIngest",
            joinColumns= {@JoinColumn (name="location_ingest_id")},
            inverseJoinColumns = {@JoinColumn(name="location_id")}
    )    
    private Set<Location> locations;    
    
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
    public Integer getDistance() {
        return distance;
    }
    public void setDistance(Integer distance) {
        this.distance = distance;
    }
    public Set<Location> getLocations() {
        return locations;
    }
    public void setLocations(Set<Location> locations) {
        this.locations = locations;
    }
    
    public void addLocation(Location l) {
        if(locations == null) {
            locations = new HashSet<>();
        }
        locations.add(l);        
    }

}

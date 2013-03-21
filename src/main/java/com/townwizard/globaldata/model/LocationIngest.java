package com.townwizard.globaldata.model;

import javax.persistence.Entity;

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

}

package com.townwizard.globaldata.model.directory;

import javax.persistence.Entity;
import javax.persistence.Transient;

import com.townwizard.db.model.AuditableEntity;

@Entity
public class ZipIngest extends AuditableEntity {
    
    private static final long serialVersionUID = -6567028287277983751L;
    
    public static enum Status {NEW, IN_PROGRESS, DONE}
    
    private String zip;
    private String countryCode;
    @Transient
    private Status status;
    
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
    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }
    
}

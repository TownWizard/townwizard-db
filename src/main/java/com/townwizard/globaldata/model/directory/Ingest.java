package com.townwizard.globaldata.model.directory;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class Ingest {
    
    public static enum Status {
        N, I, R; // New, In progress, Ready
    }
    
    @Id @GeneratedValue @Column(nullable = false, updatable = false)
    private Long id;    
    private String zip;
    private String countryCode;
    @Enumerated(EnumType.STRING)
    private Status status;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
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

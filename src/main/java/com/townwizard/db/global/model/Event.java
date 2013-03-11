package com.townwizard.db.global.model;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize (include = JsonSerialize.Inclusion.NON_EMPTY)
public class Event {

    private String id;
    private String name;
    private String location;
    private String description;
    private String street;
    private String city;
    private String state;
    private String country;
    private String zip;
    private String locationId;
    private String picture;
    private String privacy;
    
    public String getStreet() {
        return street;
    }
    public void setStreet(String street) {
        this.street = street;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public String getZip() {
        return zip;
    }
    public void setZip(String zip) {
        this.zip = zip;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getLocationId() {
        return locationId;
    }
    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }
    public String getPicture() {
        return picture;
    }
    public void setPicture(String picture) {
        this.picture = picture;
    }
    public String getPrivacy() {
        return privacy;
    }
    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }


    
}

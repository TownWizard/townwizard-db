package com.townwizard.db.global.facebook.model;

import com.townwizard.db.global.model.Convertible;
import com.townwizard.db.global.model.Event;

public class FacebookEvent implements Convertible<Event> {

    /** Do not rename fields **/
    private String eid;
    private String name;
    private String location;
    private String description;
    private Venue venue;
    
    public String getEid() {
        return eid;
    }
    public String getName() {
        return name;
    }
    public String getLocation() {
        return location;
    }
    public String getDescription() {
        return description;
    }
    public Venue getVenue() {
        return venue;
    }
    
    public Event convert() {
        Event e = new Event();
        e.setId(getEid());
        e.setName(getName());
        e.setLocation(getLocation());
        e.setDescription(getDescription());
        if(venue != null) {
            e.setStreet(venue.getStreet());
            e.setCity(venue.getCity());
            e.setState(venue.getState());
            e.setCountry(venue.getCountry());
            /* TODO : set zip*/
        }
        return e;
    }
    
}

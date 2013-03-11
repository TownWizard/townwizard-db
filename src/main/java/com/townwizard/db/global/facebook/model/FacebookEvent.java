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
    private String pic;
    private String pic_big;
    private String pic_small;
    private String pic_square; 
    private String privacy;

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
    public String getPrivace() {
        return privacy;
    }
    public String getPic_square() {
        return pic_square;
    }
    public void setPic_square(String pic_square) {
        this.pic_square = pic_square;
    }
    public String getPic() {
        return pic;
    }
    public String getPic_big() {
        return pic_big;
    }
    public String getPic_small() {
        return pic_small;
    }
    
    public Event convert() {
        Event e = new Event();
        e.setId(getEid());
        e.setName(getName());
        e.setLocation(getLocation());
        e.setDescription(getDescription());
        e.setPrivacy(getPrivace());
        
        if(venue != null) {
            e.setStreet(venue.getStreet());
            e.setCity(venue.getCity());
            e.setState(venue.getState());
            e.setCountry(venue.getCountry());
            e.setLocationId(venue.getId());
        }
        
        String picture = getPic_big();
        if(picture == null) picture = getPic();
        if(picture == null) picture = getPic_small();
        if(picture == null) picture = getPic_square();
        e.setPicture(picture);

        return e;
    }
    
}

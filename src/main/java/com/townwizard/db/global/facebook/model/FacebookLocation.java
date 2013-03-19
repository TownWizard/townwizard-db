package com.townwizard.db.global.facebook.model;

import com.townwizard.db.global.model.Convertible;
import com.townwizard.db.global.model.Location;

public class FacebookLocation implements Convertible <Location> {

    /** Do not rename fields **/
    private String id;
    private String name;
    private String category;    
    private Venue location;

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getCategory() {
        return category;
    }
    public Venue getVenue() {
        return location;
    }
    
    public Location convert() {
        Location l = new Location();
        l.setSource(Location.Source.FACEBOOK);
        l.setId(getId());
        l.setName(getName());
        l.setCategory(getCategory());
        if(location != null) {
            l.setZip(location.getZip());
            l.setCity(location.getCity());
            l.setState(location.getState());
            l.setCountry(location.getCountry());
            l.setLatitude(new Float(location.getLatitude()));
            l.setLongitude(new Float(location.getLongitude()));            
        }
        return l;
    }
    
}

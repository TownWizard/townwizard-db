package com.townwizard.globaldata.model.facebook;

import com.townwizard.db.constants.Constants;
import com.townwizard.globaldata.model.Convertible;
import com.townwizard.globaldata.model.Event;

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
    private String start_time;
    private String end_time;

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
    public String getStart_time() {
        return start_time;
    }
    public String getEnd_time() {
        return end_time;
    }

    public Event convert() {
        Event e = new Event();
        e.setId(getEid());
        e.setName(getName());
        e.setLocation(getLocation());
        e.setDescription(getDescription());
        e.setPrivacy(getPrivace());
        e.setStartTime(getStart_time());
        e.setEndTime(getEnd_time());
        e.setLink(Constants.FACEBOOK_EVENTS + "/" + getEid());
        
        Venue v = getVenue();
        if(v != null) {            
            e.setStreet(v.getStreet());
            e.setCity(v.getCity());
            e.setState(v.getState());
            e.setCountry(v.getCountry());
            e.setLocationId(v.getId());
        }
        
        String picture = getPic_big();
        if(picture == null) picture = getPic();
        if(picture == null) picture = getPic_small();
        if(picture == null) picture = getPic_square();
        e.setPicture(picture);

        return e;
    }
    
}

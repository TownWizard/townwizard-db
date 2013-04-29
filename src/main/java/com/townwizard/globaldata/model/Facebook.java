package com.townwizard.globaldata.model;

import com.townwizard.db.constants.Constants;

/**
 * Wrapper class for all Facebook specific classes (location, event, venue, etc)
 * 
 * This classes are populated from JSON returned from Facebook using reflection, that's why
 * there are no setters in them.
 */
public class Facebook {

    /**
     * Populated from FB page JSON
     */
    public static class Page {
        private String page_id;
        private Venue location;
        
        public String getPage_id() {
            return page_id;
        }
        public Venue getLocation() {
            return location;
        }   
    }
    
    /**
     * Populated from FB venue JSON
     */
    public static class Venue {
        private String id;
        @SuppressWarnings("unused") private String name;
        private String street;
        private String city;
        private String state;
        private String country;
        private String zip;
        private Double latitude;
        private Double longitude;
        
        public void fillEvent(com.townwizard.globaldata.model.Event e) {
            e.setState(street);
            e.setCity(city);
            e.setCountry(country);
            e.setState(state);
            e.setZip(zip);
            e.setLatitude(latitude);
            e.setLongitude(longitude);
        }
    }
    
    /**
     * Populated from FB location JSON, and is converted into generic Location object
     */
    public static class Location implements Convertible <com.townwizard.globaldata.model.directory.Location> {
        private String id;
        private String name;
        private String category;    
        private Venue location;
        
        public com.townwizard.globaldata.model.directory.Location convert() {
            com.townwizard.globaldata.model.directory.Location l = new com.townwizard.globaldata.model.directory.Location();
            l.setSource(com.townwizard.globaldata.model.directory.Location.Source.FACEBOOK);
            l.setExternalId(id);
            l.setName(name);
            l.setCategory(category);
            if(location != null) {
                l.setZip(location.zip);
                l.setCity(location.city);
                l.setState(location.state);
                l.setLatitude(new Float(location.latitude));
                l.setLongitude(new Float(location.longitude));            
            }
            return l;
        }        
    }
    
    /**
     * Populated from FB event JSON, and is converted into generic Event object
     */
    public static class Event implements Convertible<com.townwizard.globaldata.model.Event> {
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

        public com.townwizard.globaldata.model.Event convert() {
            com.townwizard.globaldata.model.Event e = new com.townwizard.globaldata.model.Event();
            e.setId(eid);
            e.setName(name);
            e.setLocation(location);
            e.setDescription(description);
            e.setPrivacy(privacy);
            e.setStartTime(start_time);
            e.setEndTime(end_time);
            e.setLink(Constants.FACEBOOK_EVENTS + "/" + eid);
            
            if(venue != null) {            
                e.setStreet(venue.street);
                e.setCity(venue.city);
                e.setState(venue.state);
                e.setCountry(venue.country);
                e.setLocationId(venue.id);
            }
            
            String picture = pic_big;
            if(picture == null) picture = pic;
            if(picture == null) picture = pic_small;
            if(picture == null) picture = pic_square;
            e.setPicture(picture);

            return e;
        }
    }
    
}

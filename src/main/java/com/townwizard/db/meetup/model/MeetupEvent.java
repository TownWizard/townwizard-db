package com.townwizard.db.meetup.model;

import com.townwizard.db.global.model.Convertible;
import com.townwizard.db.global.model.Event;

public class MeetupEvent implements Convertible<Event> {
    
    private String id;
    private String name;
    private String description;
    private String event_url;    
    private String status;
    private String visibility;
    private Long time;
    private Long utc_offset;
    private Long duration;
    private Integer head_count;
    private Integer waitlist_count;
    private Integer yes_rsvp_count;
    private Integer maybe_rsvp_count;
    private Venue venue;

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public String getEvent_url() {
        return event_url;
    }
    public String getStatus() {
        return status;
    }
    public String getVisibility() {
        return visibility;
    }
    public Long getTime() {
        return time;
    }
    public Long getUtc_offset() {
        return utc_offset;
    }
    public Long getDuration() {
        return duration;
    }
    public Integer getHead_count() {
        return head_count;
    }
    public Integer getWaitlist_count() {
        return waitlist_count;
    }
    public Integer getYes_rsvp_count() {
        return yes_rsvp_count;
    }
    public Integer getMaybe_rsvp_count() {
        return maybe_rsvp_count;
    }
    public Venue getVenue() {
        return venue;
    }

    /*
    private String id;
    private String name;    
    private String description;
    private String street;
    private String city;
    private String state;
    private String country;
    private String zip;
    
    private String picture;
    private String privacy;
    private Double latitude;
    private Double longitude;
    private Integer distance;
    private Double distanceInMiles;
    private String startTime;
    private String endTime;
    private String link;
         */
    
    @Override
    public Event convert() {

        return null;
    }

}

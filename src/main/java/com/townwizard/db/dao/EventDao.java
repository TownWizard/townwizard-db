package com.townwizard.db.dao;

import java.util.Date;
import java.util.List;

import com.townwizard.db.model.Event;
import com.townwizard.db.model.EventResponse;
import com.townwizard.db.model.User;

/**
 * Contains methods to manipulate event related objects (that is Event, EventResponse) in the DB.
 * 
 * Event is a subclass of Content, that is a content with content type EVENT. EventResponse class
 * represents an RSVP object
 */
public interface EventDao extends AbstractDao {

    /**
     * Given site id and event id (and assuming content type EVENT) return an Event object
     */
    Event getEvent(Integer siteId, Long eventId);

    /**
     * Get a list of event responses (RSVPs) for a given user in a specified time period.
     * Both from and to dates are included in the search
     */
    List<EventResponse> getUserEventResponses(User user, Date from, Date to);

    /**
     * Get a list of event responses (RSVPs) for a given event
     */
    List<EventResponse> getEventResponses(Event event);
    
    /**
     * Get an event response (RSVP) for a given user for a given event
     */
    EventResponse getEventResponse(User user, Event event);
    
}

package com.townwizard.db.services;

import java.util.Date;
import java.util.List;

import com.townwizard.db.model.Content.ContentType;
import com.townwizard.db.model.EventResponse;
import com.townwizard.db.model.Rating;

/**
 * Service interface with methods to handle different types of content in the system
 */
public interface ContentService {

    /**
     * Save (that is create or update) an indivudual user content rating.
     * If the content being rated does not exist, create it, first.
     */
    Long saveRating(Long userId, Integer siteId, 
            ContentType contentType, Long externalContentId,  Float value);    
    
    /**
     * Retrieve individual user rating.
     */
    Rating getUserRating(Long userId, Integer siteId,
            ContentType contentType, Long externalContentId);

    /**
     * Retrieve list of individual user ratings. 
     */
    List<Rating> getUserRatings(Long userId, Integer siteId,
            ContentType contentType, List<Long> externalContentIds);    
    
    /**
     * Retrieve content average rating and rating count 
     */
    Rating getAverageRating(Integer siteId,
            ContentType contentType, Long externalContentId);
    
    /**
     * Retrieve list of average ratings and ratings counts for a list of contents
     */
    List<Rating> getAverageRatings(Integer siteId,
            ContentType contentType, List<Long> externalContentIds);    
    
    /**
     * Retrieve RSVPs for a particular user.
     * Both from and to dates are optional.  When no from date is given the start date is assumed to be
     * far in the past, and when no to date is given the the end date is assumed to be far in the future.
     * Events which have no date information in the Townwizard DB also will be retrieved. 
     */
    List<EventResponse> getUserEventResponses(Long userId, Date from, Date to);
    
    /**
     * Retrieve RSVP for a particular user for a particular event.
     * Return null if nothing found.
     * If the optional event date parameter is given, update the event date in the DB
     */
    EventResponse getUserEventResponse(Integer siteId, Long eventId, Long userId, Date eventDate);
    
    /**
     * Retrieve RSVPs for a particular event.
     * Even event date (optional) parameter is given, update the event date in the DB (if different)
     */
    List<EventResponse> getEventResponses(Integer siteId, Long eventId, Date eventDate);

    /**
     * Save RSVP object in the system.
     * If the event for which RSVP is being created does not exist, create it
     */
    Long saveEventResponse(Long userId, Integer siteId, Long eventId, Date eventDate, Character value);
}
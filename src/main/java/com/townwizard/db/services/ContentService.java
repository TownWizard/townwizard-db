package com.townwizard.db.services;

import java.util.Date;
import java.util.List;

import com.townwizard.db.model.Content.ContentType;
import com.townwizard.db.model.EventResponse;

public interface ContentService {

    Long saveRating(Long userId, Integer siteId, 
            ContentType contentType, Long externalContentId,  Float value);    
    
    Float getUserRating(Long userId, Integer siteId,
            ContentType contentType, Long externalContentId);
    
    Float[] getUserRatings(Long userId, Integer siteId,
            ContentType contentType, Long[] externalContentIds);    
    
    Float getAverageRating(Integer siteId,
            ContentType contentType, Long externalContentId);
    
    List<EventResponse> getUserEventResponses(Long userId, Date from, Date to);
    
    List<EventResponse> getEventResponses(Integer siteId, Long eventId, Date eventDate);
    
    Long saveEventResponse(Long userId, Integer siteId, Long eventId, Date eventDate, Character value);
}
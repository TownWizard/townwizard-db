package com.townwizard.db.dao;

import java.util.Date;
import java.util.List;

import com.townwizard.db.model.Event;
import com.townwizard.db.model.EventResponse;
import com.townwizard.db.model.User;

public interface EventDao extends AbstractDao {

    Event getEvent(Integer siteId, Long eventId);
    
    List<EventResponse> getUserEventResponses(User user, Date from, Date to);
    
    List<EventResponse> getEventResponses(Event event);
    
    EventResponse getEventResponse(User user, Event event);
    
}

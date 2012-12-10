package com.townwizard.db.dao;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import com.townwizard.db.model.Content.ContentType;
import com.townwizard.db.model.Event;
import com.townwizard.db.model.EventResponse;
import com.townwizard.db.model.User;

@Component("eventDao")
public class EventDaoHibernateImpl extends AbstractDaoHibernateImpl implements EventDao {

    @Override
    public Event getEvent(Integer siteId, Long eventId) {
        return (Event)getSession().createQuery(
                "from Event where " + 
                "externalId = :external_id and siteId = :site_id and contentType = :type and active = true")
                .setLong("external_id", eventId)
                .setInteger("site_id", siteId)
                .setInteger("type", ContentType.EVENT.getId()).uniqueResult();
    }
    
    @Override
    public EventResponse getEventResponse(User user, Event event) {
        return (EventResponse)getSession().createQuery(
                "from EventResponse where user = :user and event = :event and active = true")
            .setEntity("user", user)
            .setEntity("content", event).uniqueResult();
    }
    

    @Override
    public List<EventResponse> getUserEventResponses(User user, Date from, Date to) {
        @SuppressWarnings("unchecked")
        List<EventResponse> retVal = getSession().createQuery(
                "from EventResponse where user = :user and event.date between :from and :to and active = true")
                .list();
        return retVal;
    }

    @Override
    public List<EventResponse> getEventResponses(Event event) {
        @SuppressWarnings("unchecked")
        List<EventResponse> retVal = getSession().createQuery(
                "from EventResponse where user = :user and event = :event and active = true")
                .list();
        return retVal;
    }

}

package com.townwizard.db.resources;

import java.io.StringReader;
import java.util.List;

import org.apache.http.StatusLine;
import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.Query;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Test;

import com.townwizard.db.model.Event;
import com.townwizard.db.model.EventResponse;
import com.townwizard.db.model.User;
import com.townwizard.db.model.dto.EventResponseDTO;

public class EventResponseResourceTest extends ResourceTest {
    
    private static final Long TEST_EVENT_ID = 123456788L;
    
    @Test
    public void testGetForUnexistingRsvp() {
        try {
            String getUrl = "/rsvps/9999999"; //unknown user id
            String response = executeGetRequest(getUrl);
            EventResponseDTO rsvp = rsvpFromJson(response);
            Assert.assertTrue("Rsvp must be null", rsvp == null);            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
    
    
    @Test
    public void testPostAndGet() {
        String email = "event_test_user1@test.com";
        try {
            deleteTestEventAndEventResponse();
            deleteUserByEmail(email);            
            
            createTestUserViaService(email);
            User u = getUserByEmailFromTheService(email);
            
            //create rsvp            
            StatusLine statusLine = executePostJsonRequest("/rsvps", getEventResponseJson(u.getId(), 'Y'));
            int status = statusLine.getStatusCode();
            Assert.assertEquals(
                    "HTTP status should be 201 (created) when creating rsvp", 201, status);
            
            //get rsvp by user id
            String getUrl = "/rsvps/" + u.getId();
            String response = executeGetRequest(getUrl);
            EventResponseDTO rsvp = rsvpFromJson(response);
            Assert.assertTrue("A valid rsvp must be retrieved", rsvp != null && rsvp.isValid());
            if(rsvp != null) {
                Assert.assertEquals("Rsvp value should match", new Character('Y'), rsvp.getValue());
            }
            
            //change rsvp value
            statusLine = executePostJsonRequest("/rsvps", getEventResponseJson(u.getId(), 'M'));
            status = statusLine.getStatusCode();
            Assert.assertEquals(
                    "HTTP status should be 201 (created) when updating rsvp", 201, status);
            
            //check changed value
            response = executeGetRequest(getUrl);
            rsvp = rsvpFromJson(response);
            Assert.assertTrue("A valid rsvp must be retrieved", rsvp != null && rsvp.isValid());
            if(rsvp != null) {
                Assert.assertEquals("Rsvp value should change", new Character('M'), rsvp.getValue());
            }
            
            //get rsvp by event id
            getUrl = "/rsvps/15/" + TEST_EVENT_ID;
            response = executeGetRequest(getUrl);
            rsvp = rsvpFromJson(response);
            Assert.assertTrue("A valid rsvp must be retrieved", rsvp != null && rsvp.isValid());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            deleteTestEventAndEventResponse();
            deleteUserByEmail(email);
        }
    }
    
    @Test
    public void testCreateEventResponseWithEventDate() {
        String email = "event_test_user2@test.com";
        try {
            deleteTestEventAndEventResponse();
            deleteUserByEmail(email);            
            
            createTestUserViaService(email);
            User u = getUserByEmailFromTheService(email);
            
            //create rsvp
            long eventDate = 1355353200000L;
            StatusLine statusLine = executePostJsonRequest(
                    "/rsvps", getEventResponseWithDateJson(u.getId(), 'Y', eventDate));
            int status = statusLine.getStatusCode();
            Assert.assertEquals(
                    "HTTP status should be 201 (created) when creating rsvp", 201, status);
            
            //get rsvp by user id
            String getUrl = "/rsvps/" + u.getId() + "?from="+ (eventDate - 1) + "&to=" + (eventDate+1);
            String response = executeGetRequest(getUrl);
            EventResponseDTO rsvp = rsvpFromJson(response);
            Assert.assertTrue("A valid rsvp must be retrieved", rsvp != null && rsvp.isValid());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            deleteTestEventAndEventResponse();
            deleteUserByEmail(email);
        }  
    }
    
    @Test
    public void testGetEventResponsesForEventWithEventDate() {
        String email = "event_test_user3@test.com";
        try {
            deleteTestEventAndEventResponse();
            deleteUserByEmail(email);            
            
            createTestUserViaService(email);
            User u = getUserByEmailFromTheService(email);
            
            //create rsvp
            long eventDate = 1355353200000L;
            StatusLine statusLine = executePostJsonRequest(
                    "/rsvps", getEventResponseWithDateJson(u.getId(), 'Y', eventDate));
            int status = statusLine.getStatusCode();
            Assert.assertEquals(
                    "HTTP status should be 201 (created) when creating rsvp", 201, status);
            
            //get rsvp by event id with modified date
            long newEventDate = eventDate + 1000 * 3600;
            String getUrl = "/rsvps/15/" + TEST_EVENT_ID + "/?d=" + (newEventDate);
            String response = executeGetRequest(getUrl);
            EventResponseDTO rsvp = rsvpFromJson(response);
            Assert.assertTrue("A valid rsvp must be retrieved", rsvp != null && rsvp.isValid());
            
            Event e = getTestEvent();
            Assert.assertEquals("Event date should change", newEventDate, e.getDate().getTime());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            deleteTestEventAndEventResponse();
            deleteUserByEmail(email);
        }  
    }
    
    
    private String getEventResponseJson(Long userId, Character value) {
        return "{\"userId\":" + userId + 
                ",\"siteId\":15,\"eventId\":" + TEST_EVENT_ID + 
                ",\"value\":\"" + value +"\"}";
    }
    
    private String getEventResponseWithDateJson(Long userId, Character value, long dateInMillis) {
        String json = "{\"userId\":" + userId + 
                ",\"siteId\":15,\"eventId\":" + TEST_EVENT_ID +
                ",\"eventDate\":\"" + dateInMillis + "\"" + 
                ",\"value\":\"" + value +"\"}";
        return json;
    }
    
    private EventResponseDTO rsvpFromJson(String json) throws Exception {
        ObjectMapper m = new ObjectMapper();
        EventResponseDTO[] rsvps = m.readValue(new StringReader(json), EventResponseDTO[].class);
        if(rsvps.length > 0) return rsvps[0];
        return null;
    } 
    
    private Event getTestEvent() {
        Session session = null;
        try {
            session = getSessionFactory().openSession();
            session.beginTransaction();
            
            Query q = session.createQuery("from Event where externalId = :external_id")
                    .setLong("external_id", TEST_EVENT_ID);
            
            @SuppressWarnings("unchecked")
            List<Event> events = q.list();
            session.getTransaction().commit();
            
            if(events.size() > 0) {
                return events.get(0);
            }
        } finally {
            if(session != null) {
                session.close();
            }
        }
        
        return null;
    }
    
    private void deleteTestEventAndEventResponse() {
        Session session = null;
        try {
            session = getSessionFactory().openSession();
            session.beginTransaction();
            
            Query q = session.createQuery("from EventResponse where event.externalId = :external_id")
                    .setLong("external_id", TEST_EVENT_ID);

            @SuppressWarnings("unchecked")
            List<EventResponse> responses = q.list();
            for(EventResponse r : responses) {
                session.delete(r);
            }
            
            q = session.createQuery("from Event where externalId = :external_id")
                    .setLong("external_id", TEST_EVENT_ID);
            
            @SuppressWarnings("unchecked")
            List<Event> events = q.list();
            for(Event e : events) {
                session.delete(e);
            }
            
            session.getTransaction().commit();
        } finally {
            if(session != null) {
                session.close();
            }
        }
    }

}

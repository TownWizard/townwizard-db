package com.townwizard.db.resources;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.model.EventResponse;
import com.townwizard.db.model.dto.EventResponseDTO;
import com.townwizard.db.services.ContentService;

/**
 * Represents RSVP related REST endpoints
 */
@Component
@Path("/rsvps")
public class EventResponseResource extends ResourceSupport {

    @Autowired
    private ContentService contentService;
    
    /**
     * Given a GET request with user id as a path parameter, and optional parameters
     * for start and end dates, return JSON containing a list of RSVP objects     
     * 
     * This is a "get RSVPs by user" service
     */
    @GET
    @Path("/{userid}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<EventResponseDTO> getRsvpsByPersion(
            @PathParam ("userid") Long userId,
            @QueryParam ("from") Long fromDateMillis,
            @QueryParam ("to") Long toDateMillis) {
        List<EventResponseDTO> rsvps = new ArrayList<>();
        try {
            Date from = fromDateMillis != null ? new Date(fromDateMillis) : null;
            Date to = toDateMillis != null ? new Date(toDateMillis) : null;
            List<EventResponse> responses = contentService.getUserEventResponses(userId, from, to);            
            for(EventResponse r : responses) {                
                rsvps.add(new EventResponseDTO(r.getUser(), r.getEvent().getExternalId(), r.getValue()));
            }
        } catch (Exception e) {
            handleGenericException(e);
        }
        
        return rsvps;
    }
    
    /**
     * Given a GET request with site id and event id path parameters, and an optional
     * event date (d) parameter, return JSON containing a list of RSVP objects
     * 
     * This is a "get RSVPs by event" service.
     * 
     * If the event date parameter is given, this service will also update the event date
     * in the DB
     */
    @GET
    @Path("/{siteid}/{eventid}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<EventResponseDTO> getRsvpsByEvent(
            @PathParam ("siteid") Integer siteId,
            @PathParam ("eventid") Long eventId,
            @QueryParam ("d") Long eventDateMillis) {        
        List<EventResponseDTO> rsvps = new ArrayList<>();
        try {
            Date eventDate = (eventDateMillis != null) ? new Date(eventDateMillis) : null;
            List<EventResponse> responses = contentService.getEventResponses(siteId, eventId, eventDate);
            for(EventResponse r : responses) {               
                rsvps.add(new EventResponseDTO(r.getUser(), eventId, r.getValue()));
            }
        } catch (Exception e) {
            handleGenericException(e);
        }
        
        return rsvps;
    }
    

    /**
     * Translate a POST request's JSON body into a RSVP object, and save it in the DB
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRsvp(InputStream is) {
        EventResponseDTO rsvp = null;
        try {
            rsvp = parseJson(EventResponseDTO.class, is);
        } catch(Exception e) {
            handleGenericException(e);
        }
        
        if(rsvp == null || !rsvp.isValid() || rsvp.getSiteId() == null) {
            throw new WebApplicationException(Response
                    .status(Status.BAD_REQUEST)
                    .entity("Cannot create rsvp: missing or invalid data")
                    .type(MediaType.TEXT_PLAIN).build());
        }

        try {
            Long id = contentService.saveEventResponse(rsvp.getUserId(), rsvp.getSiteId(), 
                    rsvp.getEventId(), rsvp.getEventDate(), rsvp.getValue());
            if(id == null) {
                sendServerError(new Exception("Problem saving rsvp: rsvp id is null"));
            }
        } catch(Exception e) {
            handleGenericException(e);
        }
        
        return Response.status(Status.CREATED).entity(rsvp).build();
    }
    
}

package com.townwizard.db.resources;

import java.io.InputStream;
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

@Component
@Path("/rsvp")
public class EventResponseResource extends ResourceSupport {

    @Autowired
    private ContentService contentService;
    
    @GET
    @Path("/{userid}")
    @Produces(MediaType.APPLICATION_JSON)
    public EventResponseDTO[] getRsvpsByPersion(
            @PathParam ("userid") Long userId,
            @QueryParam ("from") Date from,
            @QueryParam ("to") Date to) {
        EventResponseDTO[] rsvps = null;
        try {
           List<EventResponse> responses = contentService.getUserEventResponses(userId, from, to);
           return toArray(responses);
        } catch (Exception e) {
            handleGenericException(e);
        }
        
        return rsvps;
    }
    
    @GET
    @Path("/{siteid}/{eventid}/{eventdate}")
    @Produces(MediaType.APPLICATION_JSON)
    public EventResponseDTO[] getRsvpsByEvent(
            @PathParam ("siteid") Integer siteId,
            @PathParam ("eventid") Long eventId,
            @PathParam ("eventdate") Date eventDate) {
        EventResponseDTO[] rsvps = null;
        try {
           List<EventResponse> responses = contentService.getEventResponses(siteId, eventId, eventDate);
           return toArray(responses);
        } catch (Exception e) {
            handleGenericException(e);
        }
        
        return rsvps;
    }
    

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
        
        if(rsvp == null || !rsvp.isValid()) {
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
    
    private EventResponseDTO[] toArray(List<EventResponse> responses) {
        EventResponseDTO[] rsvps = new EventResponseDTO[responses.size()];
        int i = 0;
        for(EventResponse r : responses) {
            EventResponseDTO er = new EventResponseDTO();
            er.setUserId(r.getUser().getId());
            er.setSiteId(r.getEvent().getSiteId());
            er.setEventId(r.getEvent().getExternalId());
            er.setValue(r.getValue());
            rsvps[i++] = er;
        }
        return rsvps;        
    }
}

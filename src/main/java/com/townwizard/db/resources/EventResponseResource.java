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
@Path("/rsvps")
public class EventResponseResource extends ResourceSupport {

    @Autowired
    private ContentService contentService;
    
    @GET
    @Path("/{userid}")
    @Produces(MediaType.APPLICATION_JSON)
    public EventResponseDTO[] getRsvpsByPersion(
            @PathParam ("userid") Long userId,
            @QueryParam ("from") Long fromDateMillis,
            @QueryParam ("to") Long toDateMillis) {
        EventResponseDTO[] rsvps = null;
        try {
            Date from = fromDateMillis != null ? new Date(fromDateMillis) : null;
            Date to = toDateMillis != null ? new Date(toDateMillis) : null;
            List<EventResponse> responses = contentService.getUserEventResponses(userId, from, to);
            rsvps = new EventResponseDTO[responses.size()];
            int i = 0;
            for(EventResponse r : responses) {               
                rsvps[i++] = new EventResponseDTO(userId, r.getEvent().getExternalId(), r.getValue());
            }
           return rsvps;
        } catch (Exception e) {
            handleGenericException(e);
        }
        
        return rsvps;
    }
    
    @GET
    @Path("/{siteid}/{eventid}")
    @Produces(MediaType.APPLICATION_JSON)
    public EventResponseDTO[] getRsvpsByEvent(
            @PathParam ("siteid") Integer siteId,
            @PathParam ("eventid") Long eventId,
            @QueryParam ("d") Long eventDateMillis) {        
        EventResponseDTO[] rsvps = null;
        try {
            Date eventDate = (eventDateMillis != null) ? new Date(eventDateMillis) : null;
            List<EventResponse> responses = contentService.getEventResponses(siteId, eventId, eventDate);
            rsvps = new EventResponseDTO[responses.size()];
            int i = 0;
            for(EventResponse r : responses) {               
                rsvps[i++] = new EventResponseDTO(r.getUserId(), eventId, r.getValue());
            }
            return rsvps;
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

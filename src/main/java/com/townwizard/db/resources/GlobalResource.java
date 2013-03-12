package com.townwizard.db.resources;

import static com.townwizard.db.constants.Constants.DEFAULT_DISTANCE_IN_METERS;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.global.facebook.service.FacebookService;
import com.townwizard.db.global.model.Event;
import com.townwizard.db.global.model.Location;
import com.townwizard.db.util.ReflectionUtils;

@Component
@Path("/fb")
public class GlobalResource extends ResourceSupport {
    
    @Autowired
    private FacebookService facebookService;
    
    @GET
    @Path("/events")
    @Produces(MediaType.TEXT_HTML)
    public Response events(
            @QueryParam ("s") String searchText,
            @QueryParam ("zip") String zip) {
        try {
            List<Event> events = null;
            String search = null;
            if(zip != null) {
                events = facebookService.getEvents(zip, DEFAULT_DISTANCE_IN_METERS);
                search = zip;
            } else if(searchText != null) {
                search = searchText;
                events = facebookService.getEvents(searchText);
            } else {
                events = Collections.emptyList();
            }
            return Response.status(Status.OK).entity(objectsToHtml(events, search)).build();
        } catch(Exception e) {
            handleGenericException(e);
        }
        return Response.status(Status.BAD_REQUEST).build();
    }    
    
    @GET
    @Path("/locations")
    @Produces(MediaType.TEXT_HTML)
    public Response locations(@QueryParam ("zip") String zip) {
        try {
            if(zip != null) {
                List<Location> locations = facebookService.getLocations(zip, DEFAULT_DISTANCE_IN_METERS);            
                return Response.status(Status.OK).entity(objectsToHtml(locations, null)).build();
            }
        } catch(Exception e) {
            handleGenericException(e);
        }
        return Response.status(Status.BAD_REQUEST).build();
    }    
    
    
    private String objectsToHtml(List<?> objects, String search) {
        StringBuilder sb = new StringBuilder("<html><head></head><body>");
        for(Object o : objects) {
            sb.append(ReflectionUtils.toHtml(o));
        }
        sb.append("</body></html>");
        String result = sb.toString();
        if(search != null) {
            String s = search.startsWith("\"") ? search.substring(1, search.length()-1) : search;
            result = result.replaceAll(s, "<span style=\"color:red;\">" + s + "</span>");
        }
        return result;
    }

}

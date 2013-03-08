package com.townwizard.db.resources;

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
public class FBResource extends ResourceSupport {
    
    @Autowired
    private FacebookService facebookService;       
    
    @GET
    @Path("/events")
    @Produces(MediaType.TEXT_HTML)
    public Response events(@QueryParam ("s") String searchText) {
        try {
            List<Event> events = facebookService.getEvents(searchText);
            return Response.status(Status.OK).entity(objectsToHtml(events)).build();
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
            List<Location> locations = facebookService.getLocations(zip, 50000);            
            return Response.status(Status.OK).entity(objectsToHtml(locations)).build();
        } catch(Exception e) {
            handleGenericException(e);
        }
        return Response.status(Status.BAD_REQUEST).build();
    }    
    
    
    private String objectsToHtml(List<?> objects) {
        StringBuilder sb = new StringBuilder("<html><head></head><body>");
        for(Object o : objects) {
            sb.append(ReflectionUtils.toHtml(o));
        }
        sb.append("</body></html>");
        return sb.toString();
    }

}

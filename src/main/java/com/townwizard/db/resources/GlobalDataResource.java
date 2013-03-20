package com.townwizard.db.resources;

import static com.townwizard.db.constants.Constants.DEFAULT_COUNTRY_CODE;
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

import com.townwizard.db.util.ReflectionUtils;
import com.townwizard.globaldata.model.Event;
import com.townwizard.globaldata.model.Location;
import com.townwizard.globaldata.service.FacebookService;
import com.townwizard.globaldata.service.GlobalDataService;

@Component
@Path("/g")
public class GlobalDataResource extends ResourceSupport {
    
    @Autowired
    private FacebookService facebookService;
    @Autowired
    private GlobalDataService globalDataService;    
    
    @GET
    @Path("/events")
    @Produces(MediaType.APPLICATION_JSON)
    public Response events(
            @QueryParam ("s") String searchText,
            @QueryParam ("zip") String zip) {
        try {
            List<Event> events = getEvents(searchText, zip);
            return Response.status(Status.OK).entity(events).build();
        } catch(Exception e) {
            handleGenericException(e);
        }
        return Response.status(Status.BAD_REQUEST).build();
    }    
    
    @GET
    @Path("/events/html")
    @Produces(MediaType.TEXT_HTML)
    public Response eventsHtml(
            @QueryParam ("s") String searchText,
            @QueryParam ("zip") String zip) {
        try {
            List<Event> events = getEvents(searchText, zip);
            return Response.status(Status.OK).entity(objectsToHtml(events)).build();
        } catch(Exception e) {
            handleGenericException(e);
        }
        return Response.status(Status.BAD_REQUEST).build();
    }

    @GET
    @Path("/locations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response locations(@QueryParam ("zip") String zip) {
        try {
            if(zip != null) {
                List<Location> locations = getLocations(zip);            
                return Response.status(Status.OK).entity(locations).build();
            }
        } catch(Exception e) {
            handleGenericException(e);
        }
        return Response.status(Status.BAD_REQUEST).build();
    }
    
    @GET
    @Path("/locations/html")
    @Produces(MediaType.TEXT_HTML)
    public Response locationsHtml(@QueryParam ("zip") String zip) {
        try {            
            List<Location> locations = getLocations(zip);
            return Response.status(Status.OK).entity(objectsToHtml(locations)).build();            
        } catch(Exception e) {
            handleGenericException(e);
        }
        return Response.status(Status.BAD_REQUEST).build();
    }    
        
    private List<Event> getEvents(String searchText, String zip) {
        if(zip != null) {
            return facebookService.getEvents(zip, DEFAULT_COUNTRY_CODE, DEFAULT_DISTANCE_IN_METERS);            
        } else if(searchText != null) {            
            return facebookService.getEvents(searchText);
        }
        return Collections.emptyList();        
    }
    
    private List<Location> getLocations(String zip) {
        if(zip != null) {
            return globalDataService.getLocations(zip, DEFAULT_COUNTRY_CODE, DEFAULT_DISTANCE_IN_METERS);
        }
        return Collections.emptyList();
    }
        
    private String objectsToHtml(List<?> objects) {
        StringBuilder sb = new StringBuilder("<html><head></head><body>");
        for(Object o : objects) {
            sb.append(ReflectionUtils.toHtml(o));
        }
        sb.append("</body></html>");
        String result = sb.toString();
        return result;
    }

}

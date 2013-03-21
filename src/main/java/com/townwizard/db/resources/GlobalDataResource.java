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
import com.townwizard.globaldata.service.GlobalDataService;

@Component
@Path("/g")
public class GlobalDataResource extends ResourceSupport {
    
    @Autowired
    private GlobalDataService globalDataService;    
    
    @GET
    @Path("/events")
    @Produces(MediaType.APPLICATION_JSON)
    public Response events(@QueryParam ("zip") String zip) {
        try {
            List<Event> events = getEvents(zip);
            return Response.status(Status.OK).entity(events).build();
        } catch(Exception e) {
            handleGenericException(e);
        }
        return Response.status(Status.BAD_REQUEST).build();
    }    
    
    @GET
    @Path("/events/html")
    @Produces(MediaType.TEXT_HTML)
    public Response eventsHtml(@QueryParam ("zip") String zip) {
        try {
            List<Event> events = getEvents(zip);
            return Response.status(Status.OK).entity(objectsToHtml(events)).build();
        } catch(Exception e) {
            handleGenericException(e);
        }
        return Response.status(Status.BAD_REQUEST).build();
    }

    @GET
    @Path("/locations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response locations(
            @QueryParam ("zip") String zip,
            @QueryParam ("l") String location) {
        try {
            List<Location> locations = getLocations(zip, location);
            return Response.status(Status.OK).entity(locations).build();
        } catch(Exception e) {
            handleGenericException(e);
        }
        return Response.status(Status.BAD_REQUEST).build();
    }
    
    @GET
    @Path("/locations/html")
    @Produces(MediaType.TEXT_HTML)
    public Response locationsHtml(
            @QueryParam ("zip") String zip,
            @QueryParam ("l") String location) {
        try {
            List<Location> locations = getLocations(zip, location);
            return Response.status(Status.OK).entity(objectsToHtml(locations)).build();            
        } catch(Exception e) {
            handleGenericException(e);
        }
        return Response.status(Status.BAD_REQUEST).build();
    }    
        
    private List<Event> getEvents(String zip) {
        if(zip != null) {
            return globalDataService.getEvents(zip, DEFAULT_COUNTRY_CODE);            
        }
        return Collections.emptyList();        
    }
    
    private List<Location> getLocations(String zip, String location) {
        List<Location> locations = null;
        if(zip != null) {
            locations = getLocationsByZip(zip);                
        } else if (location != null) {
            locations = getLocationsByLatitudeAndLongitude(location);                    
        } else {
            locations = Collections.emptyList();
        }
        return locations;
    }
    
    private List<Location> getLocationsByZip(String zip) {
        return globalDataService.getLocations(zip, DEFAULT_COUNTRY_CODE, DEFAULT_DISTANCE_IN_METERS);
    }
    
    private List<Location> getLocationsByLatitudeAndLongitude(String location) {
        String[] latAndLon = location.split(",");
        if(latAndLon.length == 2) {
            try {
                double latitude = Double.parseDouble(latAndLon[0]);
                double longitude = Double.parseDouble(latAndLon[1]);
                return globalDataService.getLocations(latitude, longitude, DEFAULT_DISTANCE_IN_METERS);
            } catch (NumberFormatException e) {
                //nothing to do here, let's just return an empty list
            }
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

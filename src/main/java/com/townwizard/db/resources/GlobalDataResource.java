package com.townwizard.db.resources;

import static com.townwizard.db.constants.Constants.DEFAULT_COUNTRY_CODE;
import static com.townwizard.db.constants.Constants.DEFAULT_DISTANCE_IN_METERS;

import java.util.List;
import java.util.SortedSet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.globaldata.model.Event;
import com.townwizard.globaldata.model.Location;
import com.townwizard.globaldata.service.GlobalDataService;
import com.townwizard.globaldata.service.GlobalDataService.LocationParams;

/**
 * Web services related to global data (events, locations, etc)
 */
@Component
@Path("/g")
public class GlobalDataResource extends ResourceSupport {
    
    @Autowired
    private GlobalDataService globalDataService;    
    
    /**
     * Service to return events as JSON, given either zip, location, or client ip
     */
    @GET
    @Path("/events")
    @Produces(MediaType.APPLICATION_JSON)
    public Response events(
            @QueryParam ("zip") String zip,
            @QueryParam ("l") String location,
            @QueryParam ("ip") String ip) {
        try {
            List<Event> events = globalDataService.getEvents(
                    new LocationParams(zip, DEFAULT_COUNTRY_CODE, location, ip));
            return Response.status(Status.OK).entity(events).build();
        } catch(Exception e) {
            handleGenericException(e);
        }
        return Response.status(Status.BAD_REQUEST).build();
    }    
    
    /**
     * Service to return locations (places) JSON by either zip code, location (latitude and longitude),
     * or client ip.
     * 
     * Main category is something like "restaurants" or "directory" and is a main filter by which
     * filter the locations.
     * 
     * Categories is a comma separated list of more granular category terms for which to find locations.
     * Both, main category and categories are optional.
     */
    @GET
    @Path("/locations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response locations(
            @QueryParam ("zip") String zip,
            @QueryParam ("l") String location,
            @QueryParam ("ip") String ip,
            @QueryParam ("s") String categories,
            @QueryParam ("cat") String mainCategory) {
        try {
            List<Location> locations = globalDataService.getLocations(
                    new LocationParams(zip, DEFAULT_COUNTRY_CODE, location, ip),
                    DEFAULT_DISTANCE_IN_METERS, mainCategory, categories);
            return Response.status(Status.OK).entity(locations).build();
        } catch(Exception e) {
            handleGenericException(e);
        }
        return Response.status(Status.BAD_REQUEST).build();
    }
    
    /**
     * Service to return location categories as JSON by either zip code, location, or client ip.
     * The optional main category is described in the method above.
     */
    @GET
    @Path("/lcategories")
    @Produces(MediaType.APPLICATION_JSON)
    public Response locationCategories(
            @QueryParam ("zip") String zip,
            @QueryParam ("l") String location,
            @QueryParam ("ip") String ip,
            @QueryParam ("cat") String mainCategory) {
        try {
            SortedSet<String> categories = globalDataService.getLocationCategories(
                    new LocationParams(zip, DEFAULT_COUNTRY_CODE, location, ip),
                    DEFAULT_DISTANCE_IN_METERS, mainCategory);
            return Response.status(Status.OK).entity(categories).build();
        } catch(Exception e) {
            handleGenericException(e);
        }
        return Response.status(Status.BAD_REQUEST).build();
    }    
    
}
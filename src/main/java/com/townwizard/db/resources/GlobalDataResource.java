package com.townwizard.db.resources;

import static com.townwizard.db.constants.Constants.DEFAULT_COUNTRY_CODE;
import static com.townwizard.db.constants.Constants.DEFAULT_DISTANCE_IN_METERS;

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

import com.townwizard.globaldata.model.Event;
import com.townwizard.globaldata.model.Location;
import com.townwizard.globaldata.model.directory.Place;
import com.townwizard.globaldata.service.GlobalDataService;

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
                    new Location(zip, DEFAULT_COUNTRY_CODE, location, ip));
            return Response.status(Status.OK).entity(events).build();
        } catch(Exception e) {
            handleGenericException(e);
        }
        return Response.status(Status.BAD_REQUEST).build();
    }    
    
    /**
     * Service to return places JSON by either zip code, location (latitude and longitude),
     * or client ip for a given category or term.
     */
    @GET
    @Path("/locations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response places(
            @QueryParam ("zip") String zip,
            @QueryParam ("l") String location,
            @QueryParam ("ip") String ip,
            @QueryParam ("s") String categoryOrTerm) {
        try {
            List<Place> places = globalDataService.getPlaces(
                    new Location(zip, DEFAULT_COUNTRY_CODE, location, ip),
                    DEFAULT_DISTANCE_IN_METERS, categoryOrTerm);            
            
            return Response.status(Status.OK).entity(places).build();
        } catch(Exception e) {
            handleGenericException(e);
        }
        return Response.status(Status.BAD_REQUEST).build();
    }
    
    /**
     * Service to return place categories as JSON.
     * Filter by main category if given.
     */
    @GET
    @Path("/lcategories")
    @Produces(MediaType.APPLICATION_JSON)
    public Response placeCategories(@QueryParam ("cat") String mainCategory) {
        try {
            List<String> categories = globalDataService.getPlaceCategories(mainCategory);
            return Response.status(Status.OK).entity(categories).build();
        } catch(Exception e) {
            handleGenericException(e);
        }
        return Response.status(Status.BAD_REQUEST).build();
    }
    
    
    @GET
    @Path("/zip")
    @Produces(MediaType.TEXT_PLAIN)
    public Response zipCode(
            @QueryParam ("l") String location,
            @QueryParam ("ip") String ip) {
        try {
            String zipCode = globalDataService.getZipCode(
                    new Location(null, null, location, ip));
            return Response.status(Status.OK).entity(zipCode).build();
        } catch(Exception e) {
            handleGenericException(e);
        }
        return Response.status(Status.BAD_REQUEST).build();
    }
    
}
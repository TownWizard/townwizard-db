package com.townwizard.db.resources;

import static com.townwizard.db.constants.Constants.DEFAULT_COUNTRY_CODE;
import static com.townwizard.db.constants.Constants.DEFAULT_DISTANCE_IN_METERS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.constants.Constants;
import com.townwizard.db.util.CollectionUtils;
import com.townwizard.db.util.StringUtils;
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
    public Response events(
            @QueryParam ("zip") String zip,
            @QueryParam ("l") String location,
            @QueryParam ("ip") String ip) {
        try {
            List<Event> events = getEvents(zip, location, ip);
            return Response.status(Status.OK).entity(events).build();
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
            @QueryParam ("l") String location,
            @QueryParam ("ip") String ip,
            @QueryParam ("s") String categories,
            @QueryParam ("cat") String mainCategory) {
        try {
            List<Location> locations = getLocations(zip, location, ip, categories, mainCategory);
            return Response.status(Status.OK).entity(locations).build();
        } catch(Exception e) {
            handleGenericException(e);
        }
        return Response.status(Status.BAD_REQUEST).build();
    }
    
    @GET
    @Path("/lcategories")
    @Produces(MediaType.APPLICATION_JSON)
    public Response locationCategories(
            @QueryParam ("zip") String zip,
            @QueryParam ("l") String location,
            @QueryParam ("ip") String ip,
            @QueryParam ("cat") String mainCategory) {
        try {
            List<Location> locations = getLocations(zip, location, ip, null, mainCategory);
            Set<String> categories = collectCategories(locations);
            return Response.status(Status.OK).entity(categories).build();
        } catch(Exception e) {
            handleGenericException(e);
        }
        return Response.status(Status.BAD_REQUEST).build();
    }    
    
    private List<Event> getEvents(String zip, String location, String ip) {
        if(zip != null) {
            return getEventsByZip(zip);
        } else if (location != null) {
            return getEventsByLatitudeAndLongitude(location);
        } else if(ip != null) {
            return getEventsByIp(ip);
        } 
        return Collections.emptyList();
    }
    
    private List<Event> getEventsByZip(String zip) {
        return globalDataService.getEvents(zip, DEFAULT_COUNTRY_CODE);
    }
    
    private List<Event> getEventsByLatitudeAndLongitude(String location) {
        double[] coords = getLatitudeAndLongitudeFromParam(location);
        if(coords != null) {
            return globalDataService.getEvents(coords[0], coords[1]);
        }        
        return Collections.emptyList();
    }
    
    private List<Event> getEventsByIp(String ip) {
        return globalDataService.getEvents(ip);
    }
    
    private List<Location> getLocations(String zip, String location, String ip, String categories, String mainCategory) {
        List<Location> locations = null;
        if(zip != null && !zip.isEmpty()) {
            locations = getLocationsByZip(zip);
        } else if (location != null && !location.isEmpty()) {
            locations = getLocationsByLatitudeAndLongitude(location);
        } else if (ip != null && !ip.isEmpty()) {
            locations = getLocationsByIp(ip);
        } else {
            locations = Collections.emptyList();
        }
        locations = filterLocationsByCategories(locations, categories, false);
        
        if(mainCategory != null && !mainCategory.isEmpty()) {
            if(Constants.RESTAURANTS.equals(mainCategory)) {
                locations = filterLocationsByCategories(locations, getRestaurantsCategories(), false);
            } else if(Constants.DIRECTORY.equals(mainCategory)){
                locations = filterLocationsByCategories(locations, getRestaurantsCategories(), true);
            }
        }
        return locations;
    }
    
    private List<Location> getLocationsByZip(String zip) {
        return globalDataService.getLocations(zip, DEFAULT_COUNTRY_CODE, DEFAULT_DISTANCE_IN_METERS);
    }
    
    private List<Location> getLocationsByLatitudeAndLongitude(String location) {
        double[] coords = getLatitudeAndLongitudeFromParam(location);
        if(coords != null) {
            return globalDataService.getLocations(coords[0], coords[1], DEFAULT_DISTANCE_IN_METERS);
        }        
        return Collections.emptyList();
    }
    
    private List<Location> getLocationsByIp(String ip) {
        return globalDataService.getLocations(ip, DEFAULT_DISTANCE_IN_METERS);
    }
    
    private List<Location> filterLocationsByCategories(List<Location> locations, String categories, boolean negate) {
        if(categories == null || categories.isEmpty() || locations.isEmpty()) {            
            return locations;
        }
        List<Location> filtered = new ArrayList<>(locations.size());
        Set<String> cats = StringUtils.split(categories, ",", true);
        outer: for(Location location : locations) {
            String categoriesString = CollectionUtils.join(location.getCategoryNames()).toLowerCase();
            for(String c : cats) {
                boolean contains = categoriesString.contains(c); 
                if(contains && !negate || !contains && negate) {
                    filtered.add(location);
                    continue outer;
                }
            }            
        }
        return filtered;
    }
    
    private String getRestaurantsCategories() {
        return "restaurant";
    }
    
    private Set<String> collectCategories(List<Location> locations) {
        Set<String> categories = new TreeSet<>();
        for(Location l : locations) {            
            categories.addAll(l.getCategoryNames());
        }
        return categories;
    }
    
    private double[] getLatitudeAndLongitudeFromParam(String location) {
        String[] latAndLon = location.split(",");
        if(latAndLon.length == 2) {
            try {
                double latitude = Double.parseDouble(latAndLon[0]);
                double longitude = Double.parseDouble(latAndLon[1]);
                return new double[]{latitude, longitude}; 
            } catch (NumberFormatException e) {
                //nothing to do here, let's just return null
            }
        }
        return null;
    }

}

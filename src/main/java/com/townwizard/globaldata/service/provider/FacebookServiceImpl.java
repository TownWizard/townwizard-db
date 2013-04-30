package com.townwizard.globaldata.service.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.logger.Log;
import com.townwizard.db.util.CollectionUtils;
import com.townwizard.db.util.JSONUtils;
import com.townwizard.globaldata.connector.FacebookConnector;
import com.townwizard.globaldata.model.Event;
import com.townwizard.globaldata.model.Facebook;
import com.townwizard.globaldata.model.directory.Place;

/**
 * Implementation for FacebookService.
 * Methods of this class delegate calls to facebook connector, and convert FB JSON into domain
 * objects.
 */
@Component("facebookService")
public class FacebookServiceImpl implements FacebookService {
    
    @Autowired
    private FacebookConnector connector;

    /**
     * Executes two FB queries.
     * 
     * 1) Gets a list of FB events by running FQL request
     * 2) Since events are coming witn locations not populated properly, it collect event ids, and
     * executes pages request by ids to get proper populated locations.
     */
    @Override
    public List<Event> getEvents(List<String> terms) {
        if(terms.isEmpty()) return Collections.emptyList();
        try {
            String fql = getSearchEventsFql(terms);
            String json = connector.executeFQL(fql);
            List<Facebook.Event> fbEvents = jsonToObjects(json, Facebook.Event.class);
            List<Event> events = ServiceUtils.convertList(fbEvents);
            List<String> locationIds = collectEventLocationIds(events);
            List<Facebook.Page> pages = getPagesByIds(locationIds);
            populateLocations(events, pages);
            return events;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Executes one search request and converts FB JSON to the list of places
     */
    @Override
    public List<Place> getPlaces(double latitude, double longitude, int distanceInMeters) {
        try {
            String json = connector.executeLocationsRequest(latitude, longitude, distanceInMeters);
            List<Facebook.Location> fbObjects = jsonToObjects(json, Facebook.Location.class);
            List<Place> objects = ServiceUtils.convertList(fbObjects);
            return objects;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }    
    
    private String getSearchEventsFql(List<String> searchStrings) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT eid, name, location, description, venue, privacy, start_time, end_time, ")
          .append("pic, pic_big, pic_small, pic_square ")
          .append("FROM event WHERE ");
        boolean first = true;
        for(String searchStr : searchStrings) {
            if(!first) sb.append(" OR ");
            sb.append("contains('").append(searchStr).append("')");
            if(first) first = false;
        }
        String fql = sb.toString();
        if(Log.isDebugEnabled()) Log.debug(fql);
        return fql;
    }

    private List<String> collectEventLocationIds(List<Event> events) {
        List<String> locationIds = new ArrayList<>(events.size());
        for(Event e : events) {
            String locationId = e.getLocationId();
            if(locationId != null) {
                locationIds.add(locationId);
            }
        }
        return locationIds;
    }

    private List<Facebook.Page> getPagesByIds(List<String> pageIds) {
        if(pageIds.isEmpty()) return Collections.emptyList();
        try {
            String fql = 
                    "SELECT page_id, location " + 
                    "FROM page WHERE page_id in (" + CollectionUtils.join(pageIds, ",", "'") + ")";
            String json = connector.executeFQL(fql);
            return jsonToObjects(json, Facebook.Page.class);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private void populateLocations(List<Event> events, List<Facebook.Page> pages) {
        Map<String, Facebook.Venue> locationsMap = new HashMap<>();
        
        for(Facebook.Page p : pages) {
            Facebook.Venue location = p.getLocation();
            if(location != null) {
                locationsMap.put(p.getPage_id(), location);
            }
        }
        
        for(Event e : events) {            
            String locationId = e.getLocationId();
            Facebook.Venue v = locationsMap.get(locationId);
            if(v != null) {
                v.fillEvent(e);
            }            
        }        
    }
    
    private <T> List<T> jsonToObjects (String json, Class<T> objectClass) 
            throws JSONException, IllegalAccessException, InstantiationException {
        JSONObject j = new JSONObject(json);        
        JSONArray data = j.getJSONArray("data");
        return JSONUtils.jsonToObjects(data, objectClass);
    }

}

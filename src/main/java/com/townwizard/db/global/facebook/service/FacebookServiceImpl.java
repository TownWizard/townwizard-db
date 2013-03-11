package com.townwizard.db.global.facebook.service;

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

import com.townwizard.db.global.facebook.connect.FacebookConnector;
import com.townwizard.db.global.facebook.model.FacebookEvent;
import com.townwizard.db.global.facebook.model.FacebookLocation;
import com.townwizard.db.global.facebook.model.Page;
import com.townwizard.db.global.facebook.model.Venue;
import com.townwizard.db.global.location.service.LocationService;
import com.townwizard.db.global.model.Convertible;
import com.townwizard.db.global.model.Event;
import com.townwizard.db.global.model.Location;
import com.townwizard.db.util.CollectionUtils;
import com.townwizard.db.util.ReflectionUtils;

@Component("facebookService")
public class FacebookServiceImpl implements FacebookService {
    
    @Autowired
    private FacebookConnector connector;
    @Autowired
    private LocationService locationService;

    @Override
    public List<Event> getEvents(String searchText) {
        try {
            String fql = "SELECT eid, name, location, description, venue, privacy, pic, pic_big, pic_small, pic_square " + 
                         "FROM event WHERE contains('" + searchText + "')";
            String json = connector.executeFQL(fql);
            List<FacebookEvent> fbEvents = jsonToObjects(json, FacebookEvent.class);
            List<Event> events = convertList(fbEvents);
            return events;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public List<Event> getEvents(String zip, Integer distanceInMeters) {        
        List<Location> locations = getLocations(zip, distanceInMeters);
        
        String city = null;
        for(Location location : locations) {
            city = location.getCity();
            if(city != null) break;
        }
        
        List<Event> events = null;
        if(city != null) {
            events = getEvents(city);
        }
        
        if(events != null) {
            List<String> locationIds = new ArrayList<>(events.size());
            for(Event e : events) {
                String locationId = e.getLocationId();
                if(locationId != null) {
                    locationIds.add(locationId);
                }
            }
            
            Map<String, String> pages = getZipCodesForIds(locationIds);

            List<Event> filteredByZip = new ArrayList<>();
            for(Event e : events) {
                String locationId = e.getLocationId();
                if(locationId != null) {
                    String zipForLocation = pages.get(locationId);
                    if(zip.equals(zipForLocation)) {
                        e.setZip(zipForLocation);
                        filteredByZip.add(e);
                    }
                }
            }
            return filteredByZip;
        }
        
        return Collections.emptyList();
    }
    
    @Override
    public List<Location> getLocations(String zip, Integer distanceInMeters) {
        try {
            Location zipLocation = locationService.getLocationByZip(zip);            
            String json = connector.executeLocationsRequest(zipLocation, distanceInMeters);
            List<FacebookLocation> fbObjects = jsonToObjects(json, FacebookLocation.class);
            List<Location> objects = convertList(fbObjects);
            return objects;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private Map<String, String> getZipCodesForIds(List<String> pageIds) {
        try {
            String fql = 
                    "SELECT page_id, location " + 
                    "FROM page WHERE page_id in (" + CollectionUtils.join(pageIds, ",", "'") + ")";
            String json = connector.executeFQL(fql);
            List<Page> pages = jsonToObjects(json, Page.class);
            
            Map<String, String> result = new HashMap<>();
            for(Page p : pages) {
                Venue location = p.getLocation();
                if(location != null) {
                    result.put(p.getId(), location.getZip());
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }    
    
    private <T> List<T> jsonToObjects (String json, Class<T> objectClass) 
            throws JSONException, IllegalAccessException, InstantiationException {
        List<T> objects = new ArrayList<>();
        JSONObject j = new JSONObject(json);
        JSONArray data = j.getJSONArray("data");        
        for(int i = 0; i < data.length(); i++) {
            JSONObject o = data.optJSONObject(i);
            @SuppressWarnings("cast")
            T object = (T)objectClass.newInstance();
            ReflectionUtils.populateFromJson(object, o);
            objects.add(object);
        }
        return objects;
    }
    
    private <T> List<T> convertList(List<? extends Convertible<T>> fbObjects) {
        List<T> objects = new ArrayList<>(fbObjects.size());
        for(Convertible<T> c : fbObjects) {
            objects.add(c.convert());
        }
        return objects;
    }

}

package com.townwizard.db.global.facebook.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
            
            List<String> locationIds = collectEventLocationIds(events);
            List<Page> pages = getPagesByIds(locationIds);
            populateLocations(events, pages);
            
            return events;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public List<Event> getEvents(String zip, Integer distanceInMeters) {        
        Location location = locationService.getZipLocation(zip);
        if(location != null && location.getCity() != null) {
            List<Event> events = getEvents(location.getCity());
            populateEventDistances(location, events);
            sortEventsByDistance(events);
            return events;
        }
        
        /* filter events by zip code strictly */
        /*
        if(events != null) {
            List<String> locationIds = collectEventLocationIds(events);
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
        */
        return Collections.emptyList();
    }
    
    @Override
    public List<Location> getLocations(String zip, Integer distanceInMeters) {
        try {
            Location zipLocation = locationService.getZipLocation(zip);
            String json = connector.executeLocationsRequest(zipLocation, distanceInMeters);
            List<FacebookLocation> fbObjects = jsonToObjects(json, FacebookLocation.class);
            List<Location> objects = convertList(fbObjects);
            return objects;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private void populateEventDistances(Location origLocation, List<Event> events) {
        for(Event e : events) {
            Double eLat = e.getLatitude();
            Double eLon = e.getLongitude();
            String eZip = e.getZip();
            if(eLat != null && eLon != null) {
                Location eventLocation = new Location();
                eventLocation.setLatitude(eLat.floatValue());
                eventLocation.setLongitude(eLon.floatValue());
                e.setDistance(locationService.distance(origLocation, eventLocation));
            } else if(eZip != null) {
                e.setDistance(locationService.distance(origLocation, eZip));
            }
        }
    }
    
    private void sortEventsByDistance(List<Event> events) {
        class EventDistanceComparator implements Comparator<Event> {
            @Override            
            public int compare(Event e1, Event e2) {
                Integer d1 = e1.getDistance();
                Integer d2 = e2.getDistance();
                if(d1 != null && d2 != null) return d1.compareTo(d2);
                else if(d1 == null && d2 != null) return 1;
                else if(d1 != null && d2 == null) return -1;
                return compareNames(e1.getName(), e2.getName());
            }
            
            private int compareNames(String name1, String name2) {
                if(name1 == null) return 1;
                else if(name2 == null) return -1;
                return name1.compareTo(name2);
            }
            
        }
        
        Collections.sort(events, new EventDistanceComparator());
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
    /*
    private Map<String, String> getZipCodesForIds(List<String> pageIds) {
        List<Page> pages = getPagesByIds(pageIds);
            
        Map<String, String> result = new HashMap<>();
        for(Page p : pages) {
            Venue location = p.getLocation();
            if(location != null) {
                result.put(p.getId(), location.getZip());
            }
        }
        return result;
    }
    */
    private List<Page> getPagesByIds(List<String> pageIds) {
        try {
            String fql = 
                    "SELECT page_id, location " + 
                    "FROM page WHERE page_id in (" + CollectionUtils.join(pageIds, ",", "'") + ")";
            String json = connector.executeFQL(fql);
            return jsonToObjects(json, Page.class);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private void populateLocations(List<Event> events, List<Page> pages) {
        Map<String, Venue> locationsMap = new HashMap<>();
        for(Page p : pages) {
            Venue location = p.getLocation();
            if(location != null) {
                locationsMap.put(p.getId(), location);
            }
        }
        
        for(Event e : events) {            
            String locationId = e.getLocationId();
            Venue v = locationsMap.get(locationId);
            if(v != null) {
                e.setState(v.getStreet());
                e.setCity(v.getCity());
                e.setCountry(v.getCountry());
                e.setState(v.getState());
                e.setZip(v.getZip());
                e.setLatitude(v.getLatitude());
                e.setLongitude(v.getLongitude());
            }            
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

package com.townwizard.globaldata.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.constants.Constants;
import com.townwizard.db.util.DateUtils;
import com.townwizard.globaldata.dao.GlobalDataDao;
import com.townwizard.globaldata.model.CityLocation;
import com.townwizard.globaldata.model.Event;
import com.townwizard.globaldata.model.Location;
import com.townwizard.globaldata.service.geo.LocationService;
import com.townwizard.globaldata.service.provider.FacebookService;

@Component("eventHelper")
public final class GlobalDataServiceEventHelper {
    
    @Autowired
    private LocationService locationService;
    @Autowired
    private GlobalDataDao globalDataDao;
    @Autowired
    private FacebookService facebookService;
    
    public List<Event> getEventsByZipInfo(String zip, String countryCode) {
        Location origin = locationService.getPrimaryLocation(zip, countryCode);
        return getEvents(zip, countryCode, origin);
    }
    
    public List<Event> getEventsByLocation(double latitude, double longitude) {
        Location origin = locationService.getLocation(latitude, longitude);
        return getEvents(origin.getZip(), origin.getCountryCode(), origin);
    }
    
    public List<Event> getEventsByIp(String ip) {
        CityLocation cityLocation = globalDataDao.getCityLocationByIp(ip);
        if(cityLocation != null) {
            if(cityLocation.hasPostalCodeAndCountry()) {
                return getEventsByZipInfo(cityLocation.getPostalCode(), cityLocation.getCountryCode());
            } else if(cityLocation.hasLocation()) {
                return getEventsByLocation(cityLocation.getLatitude(), cityLocation.getLongitude());
            }
        }
        return Collections.emptyList();
    }
    
    /////////////// private methods ///////////////////
    
    private List<Event> getEvents(String zip, String countryCode, Location origin) {
        List<String> terms = locationService.getCities(zip, countryCode);
        List<Event> events = facebookService.getEvents(terms);
        List<Event> processedEvents = postProcessEvents(origin, countryCode, events);
        return processedEvents;
    }
    
    //this calculates and sets event distances as well as
    //properly set events date and time (with time zone) 
    //removes events in the past and sorts the remaining events by time/date
    private List<Event> postProcessEvents(Location origin, String countryCode, List<Event> events) {
        for(Event e : events) {
            if(origin != null) {
                setEventDistance(e, countryCode, origin);
            }
            setEventDates(e);
        }
        
        List<Event> eventsFiltered = filterEventsByDate(events);
        eventsFiltered = filterEventsByDistance(events);
        
        Collections.sort(eventsFiltered, new Comparator<Event>() {
            @Override
            public int compare(Event e1, Event e2) {
                Calendar e1Start = e1.getStartDate();
                Calendar e2Start = e2.getStartDate();
                if(e1Start != null && e2Start != null) {
                    return e1Start.compareTo(e2Start);
                } else if(e1Start != null && e2Start == null) {
                    return -1;
                } else if(e1Start == null && e2Start != null) {
                    return 1;
                }
                return 0;
            }
        });
        
        return eventsFiltered;
    }
    
    private void setEventDistance(Event e, String countryCode, Location origin) {
        Double eLat = e.getLatitude();
        Double eLon = e.getLongitude();
        String eZip = e.getZip();
        Location eventLocation = null;
        if(eLat != null && eLon != null) {
            eventLocation = new Location(eLat.floatValue(), eLon.floatValue());
        } else if (eZip != null) {
            eventLocation = locationService.getPrimaryLocation(eZip, countryCode);
        }
        if(eventLocation != null) {
            e.setDistance(locationService.distance(origin, eventLocation));
        }
    }    
    
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final DateFormat FB_EVENT_DATE_FORMAT_TIME = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    static { FB_EVENT_DATE_FORMAT_TIME.setTimeZone(GMT); }    
    private static final DateFormat FB_EVENT_DATE_FORMAT_DATE_ONLY = new SimpleDateFormat("yyyy-MM-dd");
    private static final int FB_EVENT_DATE_FORMAT_DATE_ONLY_LENGTH = "yyyy-MM-dd".length();    
    
    private void setEventDates(Event e) {
        e.setStartDate(calculateEventDate(e, e.getStartTime()));
        e.setEndDate(calculateEventDate(e, e.getEndTime()));
    }
    
    private Calendar calculateEventDate(Event e, String timeStr) {
        if(timeStr == null) return null;
        Calendar date = null;
        try {
            if(timeStr.length() > FB_EVENT_DATE_FORMAT_DATE_ONLY_LENGTH) {
                DateFormat format = FB_EVENT_DATE_FORMAT_TIME;
                date = Calendar.getInstance();
                date.setTimeZone(GMT);
                date.setTime(format.parse(timeStr));
            } else {
                DateFormat format = FB_EVENT_DATE_FORMAT_DATE_ONLY;
                format.setTimeZone(GMT);
                
                String zip = e.getZip();
                if(zip != null) {
                    String timeZone = globalDataDao.getTimeZoneByZip(e.getZip());
                    if(timeZone != null) {
                        format.setTimeZone(TimeZone.getTimeZone(timeZone));                        
                    }
                }
                date = Calendar.getInstance();
                date.setTimeZone(format.getTimeZone());
                date.setTime(format.parse(timeStr));            
            }
        } catch (Exception ex) {
          //nothing keep the date null
        }
        return date;
    }
        
    private List<Event> filterEventsByDate(List<Event> events) {
        List<Event> result = new ArrayList<>(events.size());        
        for(Event e : events) {
            Calendar startDate = e.getStartDate();
            Calendar endDate = e.getEndDate();
            
            Date now = startDate != null ? DateUtils.now(startDate.getTimeZone()) : 
                (endDate != null ? DateUtils.now(endDate.getTimeZone()) : null);
            
            Date latestTime = endDate != null ?
                endDate.getTime() : (startDate != null ? DateUtils.ceiling(startDate.getTime()) : null);
                
            if(latestTime == null || now == null || latestTime.after(now)) {
                result.add(e);
            }
        }
        return result;
    }
    
    private List<Event> filterEventsByDistance(Collection<Event> events) {
        List<Event> result = new ArrayList<>(events.size());        
        for(Event e : events) {
            Integer distance = e.getDistance();
            if(distance == null || distance <= Constants.DEFAULT_DISTANCE_IN_METERS) {
                result.add(e);
            }
        }
        return result;
    }

}

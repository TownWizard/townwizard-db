package com.townwizard.globaldata.service;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.townwizard.db.constants.Constants;
import com.townwizard.db.logger.Log;
import com.townwizard.db.util.DateUtils;
import com.townwizard.globaldata.dao.GlobalDataDao;
import com.townwizard.globaldata.dao.LocationDao;
import com.townwizard.globaldata.model.CityLocation;
import com.townwizard.globaldata.model.DistanceComparator;
import com.townwizard.globaldata.model.Event;
import com.townwizard.globaldata.model.Location;
import com.townwizard.globaldata.model.LocationIngest;

@Component("globalDataService")
public class GlobalDataServiceImpl implements GlobalDataService {
    
    @Autowired
    private FacebookService facebookService;
    @Autowired
    private GoogleService googleService;
    @Autowired
    private YellowPagesService yellowPagesService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private LocationDao locationDao;
    @Autowired
    private GlobalDataDao globalDataDao;
    
    private ExecutorService executors = Executors.newFixedThreadPool(60);

    @Override
    public List<Event> getEvents(String ip) {
        CityLocation cityLocation = globalDataDao.getCityLocationByIp(ip);
        if(cityLocation != null) {
            if(cityLocation.hasPostalCodeAndCountry()) {
                return getEvents(cityLocation.getPostalCode(), cityLocation.getCountryCode());
            } else if(cityLocation.hasLocation()) {
                return getEvents(cityLocation.getLatitude(), cityLocation.getLongitude());
            }
        }
        return Collections.emptyList();
    }
    
    @Override
    public List<Event> getEvents(String zip, String countryCode) {
        Location origin = locationService.getPrimaryLocation(zip, countryCode);
        return getEvents(zip, countryCode, origin);
    }

    @Override
    public List<Event> getEvents(double latitude, double longitude) {
        Location origin = locationService.getLocation(latitude, longitude);
        return getEvents(origin.getZip(), origin.getCountryCode(), origin);
    }    

    @Override
    @Transactional
    public List<Location> getLocations(String ip, int distanceInMeters) {
        CityLocation cityLocation = globalDataDao.getCityLocationByIp(ip);
        if(cityLocation != null) {
            if(cityLocation.hasLocation()) {
                return getLocations(cityLocation.getLatitude(), cityLocation.getLongitude(), distanceInMeters);
            } else if(cityLocation.hasPostalCodeAndCountry()) {
                return getLocations(cityLocation.getPostalCode(), cityLocation.getCountryCode(), distanceInMeters);
            }            
        }
        return Collections.emptyList();
    }
    
    @Override
    @Transactional
    public List<Location> getLocations(String zip, String countryCode, int distanceInMeters) {
        Location origin = locationService.getPrimaryLocation(zip, countryCode);
        return getLocations(zip, countryCode, distanceInMeters, origin);        
    }
    
    @Override
    @Transactional
    public List<Location> getLocations(double latitude, double longitude, int distanceInMeters) {
        Location orig = locationService.getLocation(latitude, longitude);
        return getLocations(orig.getZip(), orig.getCountryCode(), distanceInMeters, orig);
    }
    
    private List<Event> getEvents(String zip, String countryCode, Location origin) {
        List<String> terms = locationService.getCities(zip, countryCode);
        List<Event> events = facebookService.getEvents(terms);
        List<Event> processedEvents = postProcessEvents(origin, countryCode, events);
        return processedEvents;
    }
    
    private List<Location> getLocations(String zip, String countryCode, int distanceInMeters, Location origin) {
        List<Location> locations = null;
        LocationIngest ingest = locationDao.getLocationIngest(zip, countryCode);
        if(!locationIngestRequired(ingest, distanceInMeters)) {
            Set<Location> ingestLocations = ingest.getLocations();
            locations = new ArrayList<>(ingestLocations.size());
            locations.addAll(ingestLocations);
        } else {
            locations = getLocationsFromSource(zip, countryCode, distanceInMeters);
            if(!locations.isEmpty()) {
                LocationIngest updatedIngest = 
                        createOrUpdateLocationIngest(ingest, zip, countryCode, distanceInMeters);
                locationDao.saveLocations(locations, updatedIngest);
            }
        }
        
        for(Location l : locations) {
            l.setDistance(locationService.distance(origin, l));
        }
        
        locations = filterLocationsByDistance(locations, distanceInMeters);
        
        Collections.sort(locations, new DistanceComparator());        
        
        return locations;
    }
    
    private boolean locationIngestRequired(LocationIngest ingest, int distanceInMeters) {
        return ingest == null ||
               ingest.getDistance() < distanceInMeters ||
               DateUtils.addDays(ingest.getUpdated(), Constants.REFRESH_LOCATIONS_PERIOD_IN_DAYS).before(new Date());  
    }
    
    private LocationIngest createOrUpdateLocationIngest(
            LocationIngest ingest, String zip, String countryCode, int distanceInMeters) {
        if(ingest != null) {
            ingest.setDistance(distanceInMeters);
            locationDao.update(ingest);
            return ingest;
        }
        
        LocationIngest newIngest = new LocationIngest();
        newIngest.setZip(zip);
        newIngest.setCountryCode(countryCode);
        newIngest.setDistance(distanceInMeters);
        locationDao.create(newIngest);
        return newIngest;
    }
    
    private List<Location> getLocationsFromSource(
            final String zip, String countryCode, int distanceInMeters) {
        
        if(Log.isInfoEnabled()) Log.info("Getting locations from source for zip: " + zip);
        
        List<String> terms = getSearchTerms(zip, countryCode, distanceInMeters);
        List<Future<List<Location>>> results = new ArrayList<>(terms.size());
        
        final double distanceInMiles = distanceInMeters / Constants.METERS_IN_MILE;
        
        for(final String term : terms) {
            Callable<List<Location>> worker = new Callable<List<Location>>() {
                @Override
                public List<Location> call() throws Exception {
                    return yellowPagesService.getLocations(term, zip, distanceInMiles);                    
                }                
            };            
            results.add(executors.submit(worker));
        }
        
        Set<Location> locations = new TreeSet<>(new Comparator<Location>() {
            @Override
            public int compare(Location l1, Location l2) {
                int result = l1.getExternalId().compareTo(l2.getExternalId());
                if(result == 0) result = l1.getSource().compareTo(l2.getSource());
                return result;
            }
        });
        
        long start = System.currentTimeMillis();
        try {
            for(Future<List<Location>> r : results) {
                List<Location> ypLocationsForName = r.get();
                locations.addAll(ypLocationsForName);
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        long end = System.currentTimeMillis();
        if(Log.isInfoEnabled()) Log.info(
                "Executed " + terms.size() + " requests and brought: " + 
                        locations.size()  + " locations in " + (end - start) + " ms");
        
        
        
        for(Location l : locations) {
            l.setCountryCode(countryCode);
        }

        return new ArrayList<>(locations);
    }
    
    private List<String> getSearchTerms(String zip, String countryCode, int distanceInMeters) {
        Location origin = locationService.getPrimaryLocation(zip, countryCode);
        if(origin != null) {
            List<Location> googleLocations = googleService.getLocations(
                    origin.getLatitude().doubleValue(), origin.getLongitude().doubleValue(), distanceInMeters);            
            List<String> terms = new ArrayList<>(googleLocations.size());
            for(final Location gLocation : googleLocations) {
                terms.add(gLocation.getName());
            }        
            return terms;            
        }
        return Collections.emptyList();
    }
    
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
            eventLocation = new Location();
            eventLocation.setLatitude(eLat.floatValue());
            eventLocation.setLongitude(eLon.floatValue());
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
        } catch (ParseException ex) {
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
    
    private List<Location> filterLocationsByDistance(Collection<Location> locations, int distanceInMeters) {
        List<Location> result = new ArrayList<>(locations.size());        
        for(Location l : locations) {
            Integer distance = l.getDistance();
            if(distance == null || distance <= distanceInMeters) {
                result.add(l);
            }
        }
        return result;
    }
}

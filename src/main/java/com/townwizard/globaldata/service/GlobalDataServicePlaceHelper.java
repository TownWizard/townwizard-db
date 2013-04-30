package com.townwizard.globaldata.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.constants.Constants;
import com.townwizard.db.logger.Log;
import com.townwizard.db.util.CollectionUtils;
import com.townwizard.db.util.DateUtils;
import com.townwizard.db.util.StringUtils;
import com.townwizard.globaldata.dao.GlobalDataDao;
import com.townwizard.globaldata.dao.PlaceDao;
import com.townwizard.globaldata.model.CityLocation;
import com.townwizard.globaldata.model.DistanceComparator;
import com.townwizard.globaldata.model.Location;
import com.townwizard.globaldata.model.directory.Place;
import com.townwizard.globaldata.model.directory.PlaceCategory;
import com.townwizard.globaldata.model.directory.PlaceIngest;
import com.townwizard.globaldata.service.geo.LocationService;
import com.townwizard.globaldata.service.provider.GoogleService;
import com.townwizard.globaldata.service.provider.YellowPagesService;

@Component("placeHelper")
public final class GlobalDataServicePlaceHelper {

    @Autowired
    private LocationService locationService;
    @Autowired
    private PlaceDao placeDao;
    @Autowired
    private GlobalDataDao globalDataDao;
    @Autowired
    private YellowPagesService yellowPagesService;
    @Autowired
    private GoogleService googleService;
    @Autowired
    private PlaceService placeService;
    
    private ExecutorService executors = Executors.newFixedThreadPool(60);    
    
    public List<Place> getPlacesByZipInfo(String zip, String countryCode,
            String categoryOrTerm, int distanceInMeters) {
        Location origin = locationService.getPrimaryLocation(zip, countryCode);
        return getPlaces(zip, countryCode, categoryOrTerm, distanceInMeters, origin);        
    }

    public List<Place> getPlacesByLocation(double latitude, double longitude,
            String categoryOrTerm, int distanceInMeters) {
        Location orig = locationService.getLocation(latitude, longitude);
        return getPlaces(orig.getZip(), orig.getCountryCode(),
                categoryOrTerm, distanceInMeters, orig);
    }
    
    public List<Place> getPlacesByIp(String ip, String categoryOrTerm, int distanceInMeters) {
        CityLocation cityLocation = globalDataDao.getCityLocationByIp(ip);
        if(cityLocation != null) {
            if(cityLocation.hasPostalCodeAndCountry()) {
                return getPlacesByZipInfo(cityLocation.getPostalCode(), cityLocation.getCountryCode(),
                        categoryOrTerm, distanceInMeters);
            }
            if(cityLocation.hasLocation()) {
                return getPlacesByLocation(cityLocation.getLatitude(), cityLocation.getLongitude(),
                        categoryOrTerm, distanceInMeters);
            }             
        }
        return Collections.emptyList();
    }
    
    public List<String> getPlaceCategories(String mainCategory) {
        List<PlaceCategory> cats = placeService.getAllPlaceCategories();
        List<String> categories = new ArrayList<>(cats.size());
        for(PlaceCategory c : cats) categories.add(c.getName());
        
        if(mainCategory != null && !mainCategory.isEmpty()) {
            if(Constants.RESTAURANTS.equals(mainCategory)) {
                categories = filterLocationCategories(categories, getRestaurantsCategories(), false);
            } else if(Constants.DIRECTORY.equals(mainCategory)){
                categories = filterLocationCategories(categories, getRestaurantsCategories(), true);
            }
        }
        
        Collections.sort(categories);
        return categories;
    }


    ///////////////////////// private methods //////////////////////////
    
    //main location retrieval method
    private List<Place> getPlaces(
            String zip, String countryCode,
            String categoryOrTerm, 
            int distanceInMeters, Location origin) {
        /*
        List<Place> places = null;
        PlaceIngest ingest = placeDao.getPlaceIngest(zip, countryCode);
        if(!locationIngestRequired(ingest, distanceInMeters)) {
            Set<Place> ingestPlaces = ingest.getPlaces();
            places = new ArrayList<>(ingestPlaces.size());
            places.addAll(ingestPlaces);
        } else {
            places = getPlacesFromSource(zip, countryCode, distanceInMeters);
            if(!places.isEmpty()) {
                PlaceIngest updatedIngest = 
                        createOrUpdateLocationIngest(ingest, zip, countryCode, distanceInMeters);
                placeDao.savePlaces(places, updatedIngest);
            }
        }
        */

        List<Place> places = Collections.emptyList();

        PlaceIngest ingest = placeService.getIngest(zip, countryCode, distanceInMeters, categoryOrTerm);
        PlaceIngest.Status status = ingest.getStatus();
        
        if(PlaceIngest.Status.DONE == status) {
            places = placeService.getPlaces(ingest);
        } else {
            double distanceInMiles = distanceInMeters / Constants.METERS_IN_MILE;
            places = yellowPagesService.getPlaces(zip, distanceInMiles, categoryOrTerm);
            if(PlaceIngest.Status.NEW == status) {
                placeService.completeIngest(ingest, places);
            }
        }
        
        for(Place p : places) {
            Location l = new Location(p.getLatitude(), p.getLongitude());
            p.setDistance(locationService.distance(origin, l));
        }
        places = filterPlacesByDistance(places, distanceInMeters);
        
        Collections.sort(places, new DistanceComparator());
        return places;
    }
    
    private boolean locationIngestRequired(PlaceIngest ingest, int distanceInMeters) {
        return ingest == null ||
               ingest.getDistance() < distanceInMeters ||
               DateUtils.addDays(ingest.getUpdated(), Constants.REFRESH_LOCATIONS_PERIOD_IN_DAYS).before(new Date());  
    }
    
    private PlaceIngest createOrUpdateLocationIngest(
            PlaceIngest ingest, String zip, String countryCode, int distanceInMeters) {
        if(ingest != null) {
            ingest.setDistance(distanceInMeters);
            placeDao.update(ingest);
            return ingest;
        }
        
        PlaceIngest newIngest = new PlaceIngest();
        newIngest.setZip(zip);
        newIngest.setCountryCode(countryCode);
        newIngest.setDistance(distanceInMeters);
        placeDao.create(newIngest);
        return newIngest;
    }
    

    
    
    // get location search terms from Google
    // get locations by terms from Yellow Pages
    private List<Place> getPlacesFromSource(
            final String zip, String countryCode, int distanceInMeters) {
        
        if(Log.isInfoEnabled()) Log.info("Getting locations from source for zip: " + zip);
        
        List<String> terms = getSearchTerms(zip, countryCode, distanceInMeters);
        //List<String> terms = getSearchTerms();
        List<Future<List<Place>>> results = new ArrayList<>(terms.size());
        
        final double distanceInMiles = distanceInMeters / Constants.METERS_IN_MILE;
        
        for(final String term : terms) {
            Callable<List<Place>> worker = new Callable<List<Place>>() {
                @Override
                public List<Place> call() throws Exception {
                    return yellowPagesService.getPlaces(zip, distanceInMiles, term);                    
                }                
            };            
            results.add(executors.submit(worker));
        }
        
        SortedSet<Place> places = new TreeSet<>(new Comparator<Place>() {
            @Override
            public int compare(Place l1, Place l2) {
                int result = l1.getExternalId().compareTo(l2.getExternalId());
                if(result == 0) result = l1.getSource().compareTo(l2.getSource());
                return result;
            }
        });
        
        long start = System.currentTimeMillis();
        try {
            for(Future<List<Place>> r : results) {
                List<Place> ypLocationsForTerm = null;
                try {
                    ypLocationsForTerm = r.get(5, TimeUnit.SECONDS);
                } catch(Exception e) {
                    Log.debug(e.getMessage());
                }
                if(ypLocationsForTerm != null) {
                    places.addAll(ypLocationsForTerm);
                }
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        long end = System.currentTimeMillis();
        if(Log.isInfoEnabled()) Log.info(
                "Executed " + terms.size() + " requests and brought: " + 
                        places.size()  + " places in " + (end - start) + " ms");
        
        
        
        for(Place l : places) {
            l.setCountryCode(countryCode);
        }

        return new ArrayList<>(places);
    }
    
    //the logic of getting the search terms is encapsulated in this method
    //currently the search terms are location names retrieved from Google, but it may change
    //to some predefined list of strings in the future
    
    private List<String> getSearchTerms(String zip, String countryCode, int distanceInMeters) {
        Location origin = locationService.getPrimaryLocation(zip, countryCode);
        if(origin != null) {
            List<Place> googlePlaces = googleService.getPlaces(
                    origin.getLatitude().doubleValue(), origin.getLongitude().doubleValue(), distanceInMeters);            
            List<String> terms = new ArrayList<>(googlePlaces.size());
            for(final Place gLocation : googlePlaces) {
                terms.add(gLocation.getName());
            }        
            return terms;            
        }
        return Collections.emptyList();
    }
    
    private List<String> getSearchTerms() {        
        List<PlaceCategory> categories = placeDao.getAllPlaceCategories();
        List<String> terms = new ArrayList<>(categories.size());
        for(PlaceCategory c : categories) {
            terms.add(c.getName());
        }
        return terms;
    }    
    
    private List<Place> filterPlacesByDistance(Collection<Place> places, int distanceInMeters) {
        List<Place> result = new ArrayList<>(places.size());        
        for(Place l : places) {
            Integer distance = l.getDistance();
            if(distance == null || distance <= distanceInMeters) {
                result.add(l);
            }
        }
        return result;
    }
    
    private List<String> filterLocationCategories(
            List<String> locationCategories, String categories, boolean negate) {
        if(categories == null || categories.isEmpty()) return locationCategories;
        List<String> filtered = new ArrayList<>(locationCategories.size());

        Set<String> cats = StringUtils.split(categories, ",", true);
        outer: for(String c : locationCategories) {
            String categoriesString = c.toLowerCase();
            for(String cat : cats) {
                boolean contains = categoriesString.contains(cat); 
                if(contains && !negate || !contains && negate) {
                    filtered.add(c);
                    continue outer;
                }
            }            
        }
        return filtered;
    }
    
    private String getRestaurantsCategories() {
        return "restaurants";
    }
    
}

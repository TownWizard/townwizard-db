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
    
    private ExecutorService executors = Executors.newFixedThreadPool(60);    
    
    public List<Place> getPlacesByZipInfo(String zip, String countryCode,
            String mainCategory, String categories, int distanceInMeters) {
        Location origin = locationService.getPrimaryLocation(zip, countryCode);
        return getPlaces(zip, countryCode, mainCategory, categories, distanceInMeters, origin);        
    }

    public List<Place> getPlacesByLocation(double latitude, double longitude,
            String mainCategory, String categories, int distanceInMeters) {
        Location orig = locationService.getLocation(latitude, longitude);
        return getPlaces(orig.getZip(), orig.getCountryCode(),
                mainCategory, categories, distanceInMeters, orig);
    }
    
    public List<Place> getPlacesByIp(
            String ip, String mainCategory, String categories, int distanceInMeters) {
        CityLocation cityLocation = globalDataDao.getCityLocationByIp(ip);
        if(cityLocation != null) {
            if(cityLocation.hasPostalCodeAndCountry()) {
                return getPlacesByZipInfo(cityLocation.getPostalCode(), cityLocation.getCountryCode(),
                        mainCategory, categories, distanceInMeters);
            }
            if(cityLocation.hasLocation()) {
                return getPlacesByLocation(cityLocation.getLatitude(), cityLocation.getLongitude(),
                        mainCategory, categories, distanceInMeters);
            }             
        }
        return Collections.emptyList();
    }
    
    public List<String> getLocationCategoriesByZipInfo(String zip, String countryCode,
            String mainCategory, int distanceInMeters) {        
        return getLocationCategories(zip, countryCode, mainCategory, distanceInMeters);        
    }

    public List<String> getLocationCategoriesByLocation(double latitude, double longitude,
            String mainCategory, int distanceInMeters) {
        Location orig = locationService.getLocation(latitude, longitude);
        return getLocationCategories(orig.getZip(), orig.getCountryCode(), mainCategory, distanceInMeters);
    }
    
    public List<String> getLocationCategoriesByIp(
            String ip, String mainCategory, int distanceInMeters) {
        CityLocation cityLocation = globalDataDao.getCityLocationByIp(ip);
        if(cityLocation != null) {
            if(cityLocation.hasPostalCodeAndCountry()) {
                return getLocationCategoriesByZipInfo(cityLocation.getPostalCode(), cityLocation.getCountryCode(),
                        mainCategory, distanceInMeters);
            }
            if(cityLocation.hasLocation()) {
                return getLocationCategoriesByLocation(cityLocation.getLatitude(), cityLocation.getLongitude(),
                        mainCategory, distanceInMeters);
            }             
        }
       return Collections.emptyList();
    }

    ///////////////////////// private methods //////////////////////////
    
    //main location categories retrieval method
    private List<String> getLocationCategories(
            String zip, String countryCode, String mainCategory, int distanceInMeters) {
        PlaceIngest ingest = placeDao.getPlaceIngest(zip, countryCode);
        if(locationIngestRequired(ingest, distanceInMeters)) {
            List<Place> places = getPlacesFromSource(zip, countryCode, distanceInMeters);
            if(!places.isEmpty()) {
                PlaceIngest updatedIngest = 
                        createOrUpdateLocationIngest(ingest, zip, countryCode, distanceInMeters);
                placeDao.savePlaces(places, updatedIngest);
                ingest = updatedIngest;
            }
        }
        
        if(ingest != null) {
            List<String> categories = placeDao.getPlaceCategories(ingest.getId());
            if(mainCategory != null && !mainCategory.isEmpty()) {
                if(Constants.RESTAURANTS.equals(mainCategory)) {
                    categories = filterLocationCategories(categories, getRestaurantsCategories(), false);
                } else if(Constants.DIRECTORY.equals(mainCategory)){
                    categories = filterLocationCategories(categories, getRestaurantsCategories(), true);
                }
            }
            return categories;
        }
        return Collections.emptyList();
    }
    
    
    //main location retrieval method
    //it tries to get locations from the local DB first,
    //and if no ingest exists or ingest expired, gets the locations from the source
    private List<Place> getPlaces(
            String zip, String countryCode,
            String mainCategory, String categories, 
            int distanceInMeters, Location origin) {
        
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
        
        for(Place p : places) {
            Location l = new Location(p.getLatitude(), p.getLongitude());
            p.setDistance(locationService.distance(origin, l));
        }
        
        places = filterPlacesByDistance(places, distanceInMeters);   
        places = filterPlacesByCategories(places, categories, false);
        
        
        if(mainCategory != null && !mainCategory.isEmpty()) {
            if(Constants.RESTAURANTS.equals(mainCategory)) {
                places = filterPlacesByCategories(places, getRestaurantsCategories(), false);
            } else if(Constants.DIRECTORY.equals(mainCategory)){
                places = filterPlacesByCategories(places, getRestaurantsCategories(), true);
            }
        }
        
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
                    return yellowPagesService.getPlaces(term, zip, distanceInMiles);                    
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
    
    private List<Place> filterPlacesByCategories(
            List<Place> places, String categories, boolean negate) {
        if(categories == null || categories.isEmpty() || places.isEmpty()) {            
            return places;
        }
        
        List<Place> filtered = new ArrayList<>(places.size());
        Set<String> cats = StringUtils.split(categories, ",", true);
        outer: for(Place location : places) {
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

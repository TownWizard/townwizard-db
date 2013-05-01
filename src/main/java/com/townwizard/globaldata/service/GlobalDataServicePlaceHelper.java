package com.townwizard.globaldata.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.constants.Constants;
import com.townwizard.db.util.StringUtils;
import com.townwizard.globaldata.dao.GlobalDataDao;
import com.townwizard.globaldata.model.CityLocation;
import com.townwizard.globaldata.model.DistanceComparator;
import com.townwizard.globaldata.model.Location;
import com.townwizard.globaldata.model.directory.Place;
import com.townwizard.globaldata.model.directory.PlaceIngest;
import com.townwizard.globaldata.service.geo.LocationService;

@Component("placeHelper")
public final class GlobalDataServicePlaceHelper {

    @Autowired private LocationService locationService;
    @Autowired private GlobalDataDao globalDataDao;
    @Autowired private PlaceService placeService;
    @Autowired private PlaceIngester placeIngester;    
    
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
        List<String> categories = placeService.getAllPlaceCategoryNames();
        
        if(mainCategory != null && !mainCategory.isEmpty()) {
            if(Constants.RESTAURANTS.equals(mainCategory)) {
                categories = filterPlaceCategories(categories, getRestaurantsCategories(), false);
            } else if(Constants.DIRECTORY.equals(mainCategory)){
                categories = filterPlaceCategories(categories, getRestaurantsCategories(), true);
            }
        }        
        
        return categories;
    }


    ///////////////////////// private methods //////////////////////////
    
    private List<Place> getPlaces(
            String zip, String countryCode,
            String categoryOrTerm, 
            int distanceInMeters, Location origin) {

        Object[] ingestWithPlaces = placeIngester.ingestByZipAndCategory(
                zip, countryCode, distanceInMeters, categoryOrTerm);
        
        @SuppressWarnings("unchecked")
        List<Place> places = (List<Place>)ingestWithPlaces[1];
        if(places == null) {
            places = placeService.getPlaces((PlaceIngest)ingestWithPlaces[0]);
        }

        for(Place p : places) {
            Location l = new Location(p.getLatitude(), p.getLongitude());
            p.setDistance(locationService.distance(origin, l));
        }
        
        List<Place> result = filterPlacesByDistance(places, distanceInMeters);        
        
        Collections.sort(result, new DistanceComparator());
        
        return result;
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
    
    private List<String> filterPlaceCategories(
            List<String> placeCategories, String categories, boolean negate) {
        if(categories == null || categories.isEmpty()) return placeCategories;
        List<String> filtered = new ArrayList<>(placeCategories.size());

        Set<String> cats = StringUtils.split(categories, ",", true);
        outer: for(String c : placeCategories) {
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

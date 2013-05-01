package com.townwizard.globaldata.service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.logger.Log;
import com.townwizard.globaldata.model.directory.Place;
import com.townwizard.globaldata.model.directory.PlaceIngest;
import com.townwizard.globaldata.service.provider.YellowPagesService;

@Component("placeIngester")
public final class PlaceIngester {
    
    private static final ExecutorService executors = Executors.newFixedThreadPool(10);
    
    @Autowired
    private YellowPagesService yellowPagesService;
    @Autowired
    private PlaceService placeService;

    public void ingestByZip(final String zipCode, final String countryCode, final int distanceInMeters) {
        for(final String category : placeService.getAllPlaceCategoryNames()) {
            Runnable worker = new Runnable() {
                @Override
                public void run() {                   
                    PlaceIngester.this.ingestByZipAndCategory(zipCode, countryCode, distanceInMeters, category);
                }                
            };
            executors.submit(worker);
        }        
    }
    
    public Object[] ingestByZipAndCategory(
            String zipCode, String countryCode, int distanceInMeters, String categoryOrTerm) {
        
        PlaceIngest ingest = placeService.getIngest(
                zipCode, countryCode, distanceInMeters, categoryOrTerm);
        PlaceIngest.Status status = ingest.getStatus();
        
        List<Place> places = null;
        if(status != PlaceIngest.Status.DONE) {
            places = getPlacesFromSource(zipCode, countryCode, distanceInMeters, categoryOrTerm);
            if(status == PlaceIngest.Status.NEW) {
                placeService.saveIngest(ingest, places);
                if(ingest.getPlaceCategory() != null) {
                    if(Log.isDebugEnabled()) {
                        Log.debug("Ingested places for zip '" + zipCode + 
                                "' and category '" + categoryOrTerm + "'");
                    }
                }
            }
        }
        
        return new Object[]{ingest, places};
    } 
    
    private List<Place> getPlacesFromSource(
            String zipCode, String countryCode, int distanceInMeters, String categoryOrTerm) {
        List<Place> places = yellowPagesService.getPlaces(zipCode, distanceInMeters, categoryOrTerm);
        for(Place p : places) p.setCountryCode(countryCode);
        return places;
    }

}

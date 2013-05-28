package com.townwizard.globaldata.service;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.configuration.ConfigurationKey;
import com.townwizard.db.configuration.ConfigurationListener;
import com.townwizard.db.configuration.ConfigurationService;
import com.townwizard.db.logger.Log;
import com.townwizard.globaldata.ingest.place.Ingesters;
import com.townwizard.globaldata.model.directory.Ingest;
import com.townwizard.globaldata.model.directory.Place;
import com.townwizard.globaldata.model.directory.PlaceCategory;
import com.townwizard.globaldata.model.directory.PlaceIngest;
import com.townwizard.globaldata.model.directory.ZipIngest;
import com.townwizard.globaldata.service.provider.YellowPagesService;

@Component("placeIngester")
public final class PlaceIngester implements ConfigurationListener {
    
    @Autowired private YellowPagesService yellowPagesService;
    @Autowired private PlaceService placeService;
    @Autowired private ConfigurationService configurationService;
    @Autowired private Ingesters placeIngesters;
    
    @PostConstruct
    public void init() {
        configurationService.addConfigurationListener(this);
    }
    private static boolean stoppedFlag = false;

    private final ConfigurationKey[] keysOfInterest = {        
            ConfigurationKey.PLACE_INGEST_STOPPED
    };
    
    @Override
    public ConfigurationKey[] keysOfInterest() {
        return keysOfInterest;        
    }

    @Override
    public void configurationChanged(ConfigurationKey key) {
        if(key == ConfigurationKey.PLACE_INGEST_STOPPED) {
            stoppedFlag = configurationService.getBooleanValue(ConfigurationKey.PLACE_INGEST_STOPPED);
        }
    }    
    
    public void ingestByZip(String zipCode, String countryCode) {
        if(stoppedFlag) return;
        
        ZipIngest ingest = placeService.getZipIngest(zipCode, countryCode);
        if(ingest == null || ingest.getStatus() != ZipIngest.Status.N) return;
        
        if(Log.isInfoEnabled()) Log.info("Starting ingest for zip: " + zipCode);
        
        ingest.setStatus(Ingest.Status.I);
        placeService.updateZipIngest(ingest);
        
        List<PlaceCategory> categories = placeService.getAllPlaceCategories();
        
        placeIngesters.submitIngest(zipCode, countryCode, categories);
    }
    
    public Object[] ingestByZipAndCategory(
            String zipCode, String countryCode, String categoryOrTerm, Integer pageNum) {
        return getPlacesAndOptionallyIngest(zipCode, countryCode, categoryOrTerm, pageNum, null);
    } 
    
    private Object[] getPlacesAndOptionallyIngest(final String zipCode, final String countryCode,
            final String categoryOrTerm, Integer pageNum, List<Place> placeList) {
        
        final PlaceIngest ingest = placeService.getIngest(zipCode, countryCode, categoryOrTerm);
        if(ingest != null) {        
            PlaceIngest.Status status = ingest.getStatus();
            
            List<Place> places = placeList;
            boolean fromRemoteSource = false;
            
            if(status != PlaceIngest.Status.R) {
                boolean needPageOnly = (pageNum != null);
                
                if(needPageOnly) {
                    if(places == null) {
                        boolean error = false;
                        try {
                            places = getPlacesFromSource(zipCode, countryCode, categoryOrTerm, pageNum);
                        } catch (Exception e) {
                            Log.exception(e);
                            places = Collections.emptyList();
                            error = true;
                        }
                        fromRemoteSource = true;
                        
                        if(!error) {
                            if(status == PlaceIngest.Status.N) {
                                placeIngesters.submitHighPriorityIngest(zipCode, countryCode,
                                        categoryOrTerm, placeService.getAllPlaceCategories());
                            }
                        }
                    }
                } else {
                    if(places == null) {
                        try {
                            places = getPlacesFromSource(zipCode, countryCode, categoryOrTerm, null);                            
                        } catch (Exception e) {
                            places = Collections.emptyList();
                        }
                        fromRemoteSource = true;
                    }
                    if(status == PlaceIngest.Status.N) {
                        placeIngesters.submitHighPriorityIngest(zipCode, countryCode,
                                categoryOrTerm, placeService.getAllPlaceCategories());
                    }
                }
            } else {
                if(places == null) {
                    places = placeService.getPlaces(ingest);
                }
            }

            return new Object[]{places, fromRemoteSource};
        }
        return null;
    }
    
    private List<Place> getPlacesFromSource(
            String zipCode, String countryCode, String categoryOrTerm, Integer pageNum) throws Exception {        
        List<Place> places = (pageNum == null) ? 
                yellowPagesService.getPlaces(zipCode, categoryOrTerm) :
                yellowPagesService.getPageOfPlaces(zipCode, categoryOrTerm, pageNum, 
                        configurationService.getIntValue(ConfigurationKey.DIRECTORY_PAGE_SIZE));
        for(Place p : places) p.setCountryCode(countryCode);
        return places;
    }

}

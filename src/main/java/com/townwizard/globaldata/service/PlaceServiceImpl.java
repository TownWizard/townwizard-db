package com.townwizard.globaldata.service;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.townwizard.db.configuration.ConfigurationKey;
import com.townwizard.db.configuration.ConfigurationService;
import com.townwizard.db.constants.Constants;
import com.townwizard.db.logger.Log;
import com.townwizard.db.util.DateUtils;
import com.townwizard.globaldata.dao.PlaceDao;
import com.townwizard.globaldata.ingest.place.Ingesters;
import com.townwizard.globaldata.model.directory.Ingest;
import com.townwizard.globaldata.model.directory.Place;
import com.townwizard.globaldata.model.directory.PlaceCategory;
import com.townwizard.globaldata.model.directory.PlaceIngest;
import com.townwizard.globaldata.model.directory.ZipIngest;
import com.townwizard.globaldata.service.provider.YellowPagesService;

@Component("placeService")
@Transactional("directoryTransactionManager")
public final class PlaceServiceImpl implements PlaceService {
    
    @Autowired private PlaceDao placeDao;
    @Autowired private Ingesters placeIngesters;
    @Autowired private YellowPagesService yellowPagesService;
    @Autowired private ConfigurationService configurationService;
    
    @Override
    public List<PlaceCategory> getAllPlaceCategories() {
        return placeDao.getAllPlaceCategories();
    }
    
    @Override
    public List<String> getAllPlaceCategoryNames() {
        List<PlaceCategory> cats = getAllPlaceCategories();
        List<String> categories = new ArrayList<>(cats.size());
        for(PlaceCategory c : cats) categories.add(c.getName());
        Collections.sort(categories);
        return categories;
    }

    @Override
    public ZipIngest getZipIngest(String zip, String countryCode) {
        if(zip == null || countryCode == null) return null;
        
        ZipIngest ingest = placeDao.getZipIngest(zip, countryCode);
        
        if(ingest != null && isZipIngestInvalid(ingest)) {
            Log.info("About to delete zip ingest for " + ingest.getZip());
            long start = System.currentTimeMillis();
            placeDao.deleteZipIngest(ingest);
            long end = System.currentTimeMillis();
            Log.info("Deleted zip ingest for " + ingest.getZip() + " in " + (end - start) + " ms");
            ingest = null;
        }
        
        if(ingest == null) {
            ingest = createZipIngest(zip, countryCode);
        }
        
        return ingest;
    }
    
    @Override
    public void updateZipIngest(ZipIngest zipIngest) {        
        placeDao.update(zipIngest);
    }
    
    @Override
    public Object[] getPlaces(String zipCode, String countryCode, String categoryOrTerm, Integer pageNum) {
        PlaceIngest ingest = getIngest(zipCode, countryCode, categoryOrTerm);
        
        if(ingest != null && ingest.getStatus() == Ingest.Status.R) {
            return new Object[] {placeDao.getPlaces(ingest), false};
        }
        
        List<Place> places = null;
        boolean error = false;
        try {
            places = getPlacesFromSource(zipCode, countryCode, categoryOrTerm, pageNum);
        } catch (IOException e) {
            Log.info("IOException while getting places from source: " + e.getMessage());
            places = Collections.emptyList();
        } catch (Exception e) {
            Log.exception(e);
            places = Collections.emptyList();
            error = true;
        }
        if(!error) {   
             placeIngesters.submitHighPriorityIngest(zipCode, countryCode,
                     categoryOrTerm, getAllPlaceCategories());
        }

        return new Object[]{places, true};
    }    
    
    ////////////////////////// private methods ////////////////////////////////////////

    private PlaceIngest getIngest(String zipCode, String countryCode, String categoryOrTerm) {
        PlaceIngest ingest = placeDao.getIngest(zipCode, countryCode, categoryOrTerm);
        
        if(ingest != null && (isIngestInvalid(ingest) || Ingest.Status.N.equals(ingest.getStatus()))) {
            //normally, the DB ingest status should be never N
            //the N status is an indicator for the current thread
            //that ingest has been created by the current thread
            //if the DB has an ingest with such status, it's an error, and let's reingest it
            placeDao.deleteIngest(ingest);
            ingest = null;
        }

        return ingest;
    }
    
    
    private boolean isIngestInvalid(PlaceIngest ingest) {
        return DateUtils.addDays(ingest.getCreated(), Constants.REFRESH_PLACE_INGEST_PERIOD_IN_DAYS)
                .before(new Date());
    }
    
    /*
     * Zip ingest is invalid if:
     * 1) It's in progress for longer than 3 days
     * 2) It's ready but was created long ago
     */
    private boolean isZipIngestInvalid(ZipIngest ingest) {
        Ingest.Status status = ingest.getStatus();
        return status == Ingest.Status.I &&
                DateUtils.addDays(ingest.getStarted(), 3).before(new Date()) ||
                status == Ingest.Status.R &&
                DateUtils.addDays(ingest.getStarted(), Constants.REFRESH_PLACE_INGEST_PERIOD_IN_DAYS).before(new Date()); 
    }
    
    private ZipIngest createZipIngest(String zipCode, String countryCode) {        
        ZipIngest ingest = new ZipIngest();
        ingest.setZip(zipCode);
        ingest.setCountryCode(countryCode);
        ingest.setStatus(Ingest.Status.I);
        Date now = new Date();
        ingest.setStarted(now);        
        placeDao.create(ingest);
        
        ZipIngest detached = new ZipIngest();
        detached.setId(ingest.getId());
        detached.setZip(ingest.getZip());
        detached.setCountryCode(ingest.getCountryCode());
        detached.setStarted(ingest.getStarted());
        detached.setStatus(Ingest.Status.N);
        return detached;
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

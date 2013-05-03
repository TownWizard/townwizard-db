package com.townwizard.globaldata.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.townwizard.db.constants.Constants;
import com.townwizard.db.util.DateUtils;
import com.townwizard.globaldata.dao.PlaceDao;
import com.townwizard.globaldata.model.directory.Place;
import com.townwizard.globaldata.model.directory.PlaceCategory;
import com.townwizard.globaldata.model.directory.PlaceIngest;
import com.townwizard.globaldata.model.directory.ZipIngest;

@Component("placeService")
@Transactional("directoryTransactionManager")
public final class PlaceServiceImpl implements PlaceService {
    
    @Autowired
    private PlaceDao placeDao;
    
    @Override
    public PlaceIngest getIngest(String zipCode, String countryCode, int distanceInMeters,
            String categoryOrTerm) {
        if(zipCode == null || countryCode == null || categoryOrTerm == null) return null;
        
        PlaceIngest ingest = placeDao.getPlaceIngest(zipCode, countryCode, categoryOrTerm);
        if(ingest != null && isIngestInvalid(ingest, distanceInMeters)) {
            placeDao.deleteIngest(ingest);
            ingest = null;
        }
        
        boolean isNew = false;
        if(ingest == null) {
            ingest = createIngest(zipCode, countryCode, distanceInMeters, categoryOrTerm);
            isNew = true;
        }
        
        if(isNew) {
            ingest.setStatus(PlaceIngest.Status.NEW);
        }
        else if(ingest.getCreated().equals(ingest.getUpdated())) {
            ingest.setStatus(PlaceIngest.Status.IN_PROGRESS);
        } else {
            ingest.setStatus(PlaceIngest.Status.DONE);
        }

        return ingest;
    }
    
    @Override
    public void saveIngest(PlaceIngest ingest, List<Place> places) {
        placeDao.saveIngest(ingest, places);
    }

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
    public List<Place> getPlaces(PlaceIngest ingest) {
        return placeDao.getPlaces(ingest);
    }
    
    @Override
    public ZipIngest getZipIngest(String zip, String countryCode) {
        if(zip == null || countryCode == null) return null;
        
        ZipIngest ingest = placeDao.getZipIngest(zip, countryCode);
        
        if(ingest != null && isZipIngestInvalid(ingest)) {
            placeDao.delete(ingest);
            ingest = null;
        }
        
        boolean isNew = false;
        if(ingest == null) {
            ingest = createZipIngest(zip, countryCode);
            isNew = true;
        }
        
        if(isNew) {
            ingest.setStatus(ZipIngest.Status.NEW);
        } else if(ingest.getCreated().equals(ingest.getUpdated())) {
            ingest.setStatus(ZipIngest.Status.IN_PROGRESS);
        } else {
            ingest.setStatus(ZipIngest.Status.DONE);
        }
        
        return ingest;
    }
    
    @Override
    public void updateZipIngest(ZipIngest zipIngest) {
        placeDao.updateZipIngest(zipIngest);
    }
    
    ////////////////////////// private methods ////////////////////////////////////////
    
    private boolean isIngestInvalid(PlaceIngest ingest, int distanceInMeters) {
        boolean expired =
                DateUtils.addDays(ingest.getUpdated(), Constants.REFRESH_LOCATIONS_PERIOD_IN_DAYS)
                .before(new Date());
        return expired || ingest.getDistance() < distanceInMeters;
    }
    
    /*
     * A zip ingest is invalid if:
     * 1) its created and updated dates are the same
     * 2) and it's been 1 day since it was created
     */
    private boolean isZipIngestInvalid(ZipIngest ingest) {
        return ingest.getCreated().equals(ingest.getUpdated()) && 
               DateUtils.addDays(ingest.getCreated(), 1).before(new Date());
    }
    
    private PlaceIngest createIngest(String zipCode, String countryCode, int distanceInMeters,
            String categoryOrTerm) {
        PlaceCategory category = placeDao.getCategory(categoryOrTerm);
        PlaceIngest ingest = new PlaceIngest();
        ingest.setZip(zipCode);
        ingest.setCountryCode(countryCode);
        ingest.setDistance(distanceInMeters);
        if(category != null) {
            ingest.setPlaceCategory(category);
        } else {
            ingest.setTerm(categoryOrTerm.toLowerCase());
        }
        placeDao.create(ingest);
        return ingest;
    }
    
    private ZipIngest createZipIngest(String zipCode, String countryCode) {        
        ZipIngest ingest = new ZipIngest();
        ingest.setZip(zipCode);
        ingest.setCountryCode(countryCode);
        placeDao.create(ingest);
        return ingest;
    }    

}

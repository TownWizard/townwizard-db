package com.townwizard.globaldata.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.townwizard.db.constants.Constants;
import com.townwizard.db.logger.Log;
import com.townwizard.db.util.DateUtils;
import com.townwizard.globaldata.dao.PlaceDao;
import com.townwizard.globaldata.model.directory.Ingest;
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
    public PlaceIngest getIngest(String zipCode, String countryCode, String categoryOrTerm) {
        if(zipCode == null || countryCode == null || categoryOrTerm == null) {
            Log.warning("Either zip or countryCode, or term is null.  Cannot find ingest.  Returning null");
            return null;
        }
        
        PlaceIngest ingest = placeDao.getPlaceIngest(zipCode, countryCode, categoryOrTerm);
        
        if(ingest != null && isIngestInvalid(ingest)) {
            placeDao.deleteIngest(ingest);
            ingest = null;
        }

        if(ingest == null) {
            ingest = createIngest(zipCode, countryCode, categoryOrTerm);
            ingest.setStatus(Ingest.Status.N);
        }        

        return ingest;
    }
    
    @Override
    public ZipIngest getZipIngest(String zip, String countryCode) {
        if(zip == null || countryCode == null) return null;
        
        ZipIngest ingest = placeDao.getZipIngest(zip, countryCode);
        
        if(ingest != null && isZipIngestInvalid(ingest)) {
            placeDao.delete(ingest);
            ingest = null;
        }
        
        if(ingest == null) {
            ingest = createZipIngest(zip, countryCode);
            ingest.setStatus(Ingest.Status.N);
        }
        
        return ingest;
    }    
    
    @Override
    public void saveIngest(PlaceIngest ingest, List<Place> places) {
        ingest.setStatus(Ingest.Status.R);
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
    public void updateZipIngest(ZipIngest zipIngest) {        
        placeDao.update(zipIngest);
    }
    
    ////////////////////////// private methods ////////////////////////////////////////
    
    private boolean isIngestInvalid(PlaceIngest ingest) {
        return DateUtils.addDays(ingest.getCreated(), Constants.REFRESH_PLACE_INGEST_PERIOD_IN_DAYS)
                .before(new Date());
    }
    
    /*
     * Zip ingest is invalid if:
     * 1) It's in progress for longer than a day
     * 2) It's ready but was created long ago
     */
    private boolean isZipIngestInvalid(ZipIngest ingest) {
        Ingest.Status status = ingest.getStatus();
        return status == Ingest.Status.I &&
                DateUtils.addDays(ingest.getStarted(), 1).before(new Date()) ||
                status == Ingest.Status.R &&
                DateUtils.addDays(ingest.getStarted(), Constants.REFRESH_PLACE_INGEST_PERIOD_IN_DAYS).before(new Date()); 
    }
    
    private PlaceIngest createIngest(String zipCode, String countryCode, String categoryOrTerm) {
        PlaceCategory category = placeDao.getCategory(categoryOrTerm);
        PlaceIngest ingest = new PlaceIngest();
        ingest.setZip(zipCode);
        ingest.setCountryCode(countryCode);
        if(category != null) {
            ingest.setPlaceCategory(category);
        } else {
            ingest.setTerm(categoryOrTerm.toLowerCase());
        }
        ingest.setStatus(Ingest.Status.I);
        ingest.setCreated(new Date());
        placeDao.create(ingest);
        return ingest;
    }
    
    private ZipIngest createZipIngest(String zipCode, String countryCode) {        
        ZipIngest ingest = new ZipIngest();
        ingest.setZip(zipCode);
        ingest.setCountryCode(countryCode);
        ingest.setStatus(Ingest.Status.I);
        Date now = new Date();
        ingest.setStarted(now);        
        placeDao.create(ingest);
        return ingest;
    }    

}

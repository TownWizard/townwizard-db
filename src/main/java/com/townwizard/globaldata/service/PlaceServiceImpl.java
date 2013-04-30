package com.townwizard.globaldata.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.townwizard.globaldata.dao.PlaceDao;
import com.townwizard.globaldata.model.directory.Place;
import com.townwizard.globaldata.model.directory.PlaceCategory;
import com.townwizard.globaldata.model.directory.PlaceIngest;

@Component("placeService")
@Transactional("directoryTransactionManager")
public final class PlaceServiceImpl implements PlaceService {
    
    @Autowired
    private PlaceDao placeDao;
    
    @Override
    public PlaceIngest getIngest(
            String zipCode, String countryCode, int distanceInMeters, String categoryOrTerm) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void completeIngest(PlaceIngest ingest, List<Place> places) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PlaceCategory> getAllPlaceCategories() {
        return placeDao.getAllPlaceCategories();
    }

    @Override
    public List<Place> getPlaces(PlaceIngest ingest) {
        throw new UnsupportedOperationException();
    }

}

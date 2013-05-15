package com.townwizard.globaldata.service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.globaldata.dao.GlobalDataDao;
import com.townwizard.globaldata.model.CityLocation;
import com.townwizard.globaldata.model.Event;
import com.townwizard.globaldata.model.Location;
import com.townwizard.globaldata.model.directory.Place;
import com.townwizard.globaldata.service.geo.LocationService;

/**
 * GlobalDataService implementation
 */
@Component("globalDataService")
public class GlobalDataServiceImpl implements GlobalDataService {
    
    @Autowired
    private LocationService locationService;
    @Autowired
    private GlobalDataDao globalDataDao;
    
    @Autowired
    GlobalDataServiceEventHelper eventHelper;
    @Autowired
    GlobalDataServicePlaceHelper placeHelper;    

    /**
     * Currently events are retrieved from Facebook 
     */
    @Override
    public List<Event> getEvents(Location params) {
        if(params.isZipInfoSet()) {
            return eventHelper.getEventsByZipInfo(params.getZip(), params.getCountryCode());
        } else if(params.isLocationSet()) {
            return eventHelper.getEventsByLocation(params.getLatitude(), params.getLongitude());
        } else if(params.isIpSet()) {
            return eventHelper.getEventsByIp(params.getIp());
        }
        return Collections.emptyList();
    }
    
    @Override
    public List<Place> getPlaces(Location params, String categoryOrTerm, String mainCategory, Integer pageNum) {
        if(params.isZipInfoSet()) {
            return placeHelper.getPlacesByZipInfo(
                    params.getZip(), params.getCountryCode(), categoryOrTerm, mainCategory, pageNum);
        } else if(params.isLocationSet()) {
            return placeHelper.getPlacesByLocation(
                    params.getLatitude(), params.getLongitude(), categoryOrTerm, mainCategory, pageNum);
        } else if(params.isIpSet()) {
            return placeHelper.getPlacesByIp(params.getIp(), categoryOrTerm, mainCategory, pageNum);
        }
        return Collections.emptyList();        
    }
    
    @Override
    public List<String> getPlaceCategories(String mainCategory) {
        return placeHelper.getPlaceCategories(mainCategory);
    }
    
    @Override
    public String getZipCode(Location params) {
        if(params.isZipInfoSet()) {
            return params.getZip();
        } else if(params.isLocationSet()) {
            Location orig = locationService.getLocation(params.getLatitude(), params.getLongitude());
            return orig.getZip();
        } else if(params.isIpSet()) {
            CityLocation cityLocation = globalDataDao.getCityLocationByIp(params.getIp());
            return cityLocation.getPostalCode();
        }
        return null;
    }

}

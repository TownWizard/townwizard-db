package com.townwizard.globaldata.service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    
    /**
     * Having location params and distance, the local DB first is checked.  If no place ingest
     * exist for the params and distance, or the place ingest is expired, then places are
     * brought from the source, otherwise local DB is used
     * 
     * Currently the logic of retrieving a place from the source is this:
     * 1) Go to Google and get placess by location parameters.
     * 2) Collect place names
     * 3) Retrieve places from Yellow Pages using collected names as search terms
     * 
     * Retrieval of places from Yellow Pages is done in separate threads. 
     */
    @Override
    @Transactional("directoryTransactionManager")
    public List<Place> getPlaces(
            Location params, int distanceInMeters, String mainCategory, String categories) {
        if(params.isZipInfoSet()) {
            return placeHelper.getPlacesByZipInfo(params.getZip(), params.getCountryCode(),
                    mainCategory, categories, distanceInMeters);
        } else if(params.isLocationSet()) {
            return placeHelper.getPlacesByLocation(params.getLatitude(), params.getLongitude(),
                    mainCategory, categories, distanceInMeters);
        } else if(params.isIpSet()) {
            return placeHelper.getPlacesByIp(params.getIp(),
                    mainCategory, categories, distanceInMeters);
        }
        return Collections.emptyList();        
    }
    
    /**
     * Get places as in the method above, and collect categories from them.
     */
    @Override
    @Transactional("directoryTransactionManager")
    public List<String> getPlaceCategories(Location params, 
            int distanceInMeters, String mainCategory) {
        if(params.isZipInfoSet()) {
            return placeHelper.getLocationCategoriesByZipInfo(params.getZip(), params.getCountryCode(),
                    mainCategory, distanceInMeters);
        } else if(params.isLocationSet()) {
            return placeHelper.getLocationCategoriesByLocation(params.getLatitude(), params.getLongitude(),
                    mainCategory, distanceInMeters);
        } else if(params.isIpSet()) {
            return placeHelper.getLocationCategoriesByIp(params.getIp(),
                    mainCategory, distanceInMeters);
        }
        return Collections.emptyList();
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

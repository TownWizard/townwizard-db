package com.townwizard.globaldata.service;

import java.util.List;

import com.townwizard.globaldata.model.Location;

public interface GlobalDataService {
    
    List<Location> getLocations(String zip, String countryCode, int distanceInMeters);

}
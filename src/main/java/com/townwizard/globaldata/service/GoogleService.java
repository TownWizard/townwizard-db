package com.townwizard.globaldata.service;

import java.util.List;

import com.townwizard.globaldata.model.Location;

public interface GoogleService {
    
    List<Location> getLocations(String zip, String countryCode, Integer distanceInMeters);

}

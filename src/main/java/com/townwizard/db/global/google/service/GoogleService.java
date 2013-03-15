package com.townwizard.db.global.google.service;

import java.util.List;

import com.townwizard.db.global.model.Location;

public interface GoogleService {
    
    List<Location> getLocations(String zip, String countryCode, Integer distanceInMeters);

}

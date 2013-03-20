package com.townwizard.globaldata.service;

import java.util.List;

import com.townwizard.globaldata.model.Location;

public interface YellowPagesService {

    List<Location> getLocations(String term, String zip, double distanceInMiles);
    
}

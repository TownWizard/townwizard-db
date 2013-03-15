package com.townwizard.db.global.yellopages.service;

import java.util.List;

import com.townwizard.db.global.model.Location;

public interface YellowPagesService {

    List<Location> getLocations(String term, String zip, Integer distanceInMeters);
    
}

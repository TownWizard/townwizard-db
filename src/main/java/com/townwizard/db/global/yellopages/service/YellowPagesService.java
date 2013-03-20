package com.townwizard.db.global.yellopages.service;

import java.util.List;

import com.townwizard.globaldata.model.Location;

public interface YellowPagesService {

    List<Location> getLocations(String term, String zip, Integer distanceInMeters);
    
}

package com.townwizard.globaldata.service;

import java.util.List;

import com.townwizard.globaldata.model.Event;
import com.townwizard.globaldata.model.Location;

public interface FacebookService {

    List<Event> getEvents(List<String> terms);
    List<Location> getLocations(double latitude, double longitude, int distanceInMeters);
    
}

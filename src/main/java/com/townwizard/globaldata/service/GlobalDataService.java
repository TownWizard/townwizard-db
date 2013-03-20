package com.townwizard.globaldata.service;

import java.util.List;

import com.townwizard.globaldata.model.Event;
import com.townwizard.globaldata.model.Location;

public interface GlobalDataService {
    
    List<Event> getEvents(String zip, String countryCode);
    List<Event> getEvents(double latitude, double longitude);
    
    List<Location> getLocations(String zip, String countryCode, int distanceInMeters);
    List<Location> getLocations(double latitude, double longitude, int distanceInMeters);

}
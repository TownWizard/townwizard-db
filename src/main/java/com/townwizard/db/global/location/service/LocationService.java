package com.townwizard.db.global.location.service;

import com.townwizard.db.global.model.Location;

public interface LocationService {
    
    Location getZipLocation(String zip);
    Integer distance(Location location, String zip);
    Integer distance(Location location1, Location location2);

}

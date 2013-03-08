package com.townwizard.db.global.location.service;

import com.townwizard.db.global.model.Location;

public interface LocationService {
    
    Location getLocationByZip(String zip);

}

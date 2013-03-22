package com.townwizard.globaldata.dao;

import java.util.List;

import com.townwizard.db.dao.AbstractDao;
import com.townwizard.globaldata.model.Location;
import com.townwizard.globaldata.model.LocationIngest;

public interface LocationDao extends AbstractDao {
    
    LocationIngest getLocationIngest(String zip, String countryCode);
    void saveLocations(List<Location> locations, LocationIngest ingest);

}

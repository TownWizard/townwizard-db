package com.townwizard.globaldata.dao;

import com.townwizard.globaldata.model.CityLocation;

/**
 * Contains methods to read global data tables
 */
public interface GlobalDataDao {
    
    /**
     * Get time zone for a zip code
     */
    String getTimeZoneByZip(String zip);

    /**
     * Get city location by IP
     */
    CityLocation getCityLocationByIp(String ip);

}

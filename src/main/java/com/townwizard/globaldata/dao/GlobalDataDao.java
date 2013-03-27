package com.townwizard.globaldata.dao;

import com.townwizard.globaldata.model.CityLocation;

public interface GlobalDataDao {
    
    String getTimeZoneByZip(String zip);
    CityLocation getCityLocationByIp(String ip);

}

package com.townwizard.globaldata.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.util.StringUtils;
import com.townwizard.globaldata.model.CityLocation;

/**
 * Hibernate implementation of GlobalDataDao
 */
@Component("globalDataDao")
public class GlobalDataDaoHibernateImpl implements GlobalDataDao {
    
    @Autowired
    private SessionFactory sessionFactory;
    
    private static final Map<String, String> zipToTimeZone = new HashMap<>();

    /**
     * This method caches time zones for zip codes in a local cache
     */
    @Override
    public String getTimeZoneByZip(String zip) {
        String timeZone = zipToTimeZone.get(zip);
        if(timeZone == null) {
            Session session = sessionFactory.openSession();
            timeZone = (String)session
                    .createSQLQuery(TIME_ZONE_BY_ZIP_SQL)
                    .setString(0, zip)
                    .uniqueResult();
            zipToTimeZone.put(zip, timeZone);
        }
        return timeZone;
    }

    /**
     * Parses ip, and if ip is valid gets the city location for it, otherwise
     * returns null
     */
    @Override
    public CityLocation getCityLocationByIp(String ip) {
        int ipAsInt = -1;
        try {
            ipAsInt = StringUtils.ip4ToInteger(ip);
        } catch (NumberFormatException e) {
            //nothing.  Return null
        }
        
        if(ipAsInt != -1) {
            Session session = sessionFactory.openSession();
            @SuppressWarnings({"unchecked" })
            List<Object[]> zipCodes = session
                    .createSQLQuery(ZIP_BY_IP_SQL)
                    .setInteger(0, ipAsInt)
                    .list();
            if(zipCodes.size() > 0) {
                Object[] data = zipCodes.get(0);
                return new CityLocation((String)data[0], (String)data[1], (String)data[2],
                        (Double)data[3], (Double)data[4]);
            }
        }

        return null;
    }
    
    
    private static final String TIME_ZONE_BY_ZIP_SQL = "SELECT timezone FROM geo.TimeZones WHERE zip = ?"; 
    private static final String ZIP_BY_IP_SQL =     
        "SELECT cl.city, cl.postal_code, cl.country_code, cl.latitude, cl.longitude " +
        "FROM geo.CityLocations cl JOIN geo.CityBlocks cb ON cl.id = cb.location_id " +
        "WHERE ? BETWEEN cb.ip_start AND cb.ip_end";
}

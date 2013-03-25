package com.townwizard.globaldata.dao;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("globalDataDao")
public class GlobalDataDaoHibernateImpl implements GlobalDataDao {
    
    @Autowired
    private SessionFactory sessionFactory;
    
    private static final Map<String, String> zipToTimeZone = new HashMap<>();

    @Override
    public String getTimeZone(String zip) {
        String timeZone = zipToTimeZone.get(zip);
        if(timeZone == null) {
            Session session = sessionFactory.openSession();
            timeZone = (String)session
                    .createSQLQuery("SELECT timezone FROM timezonebyzipcode WHERE zip = ?")
                    .setString(0, zip)
                    .uniqueResult();
            zipToTimeZone.put(zip, timeZone);
        }
        return timeZone;
    }
    
}

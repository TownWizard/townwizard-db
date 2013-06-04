package com.townwizard.db.configuration;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import com.townwizard.db.dao.AbstractDaoHibernateImpl;

@Component("configurationDao")
public class ConfigurationDaoImpl extends AbstractDaoHibernateImpl implements ConfigurationDao {

    @Override
    public void save(String key, String value) {
        ConfigurationKey cKey = ConfigurationKey.byKey(key);
        String description = null;
        if(cKey != null) {
            description = cKey.getDescription();
        }        
        save(key, value, description);       
    }
    
    @Override
    public void save(String key, String value, String description) {
        ConfigurationAttribute a = getAttribute(key);
        if(a == null) {
            a = new ConfigurationAttribute(key, value, description);
        } else {
            a.setValue(value);
            a.setDescription(description);
        }
        getSession().save(a);        
    }

    @Override
    public void delete(String key) {
        ConfigurationAttribute a = getAttribute(key);
        if(a != null) {
            Session session = getSession();
            session.delete(a);
            session.flush();
        }
    }

    @Override
    public String get(String key) {
        ConfigurationAttribute a = getAttribute(key);
        if(a != null) {
            return a.getValue();
        }
        return null;
    }
    
    private ConfigurationAttribute getAttribute(String key) {
        return (ConfigurationAttribute)getSession()
            .createQuery("from ConfigurationAttribute where key = :key")
            .setString("key", key).uniqueResult();
    }

}

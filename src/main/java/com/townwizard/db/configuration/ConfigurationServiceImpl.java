package com.townwizard.db.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.logger.Log;

@Component("ConfigurationService")
public class ConfigurationServiceImpl implements ConfigurationService {
    
    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private ConfigurationCache configurationCache;
    

    @Override
    public void save(String key, String value) {
        configurationDao.save(key, value);
        configurationCache.remove(key);
    }

    @Override
    public void delete(String key) {
        configurationDao.delete(key);
        configurationCache.remove(key);
    }

    @Override
    public String getStringValue(ConfigurationKey key) {
        String value = getStringValue(key.getKey());
        if(value != null) {
            return value;
        }
        return (String)key.getDefaultValue();
    }

    @Override
    public int getIntValue(ConfigurationKey key) {
        int value = -999;
        String valueStr = getStringValue(key.getKey());
        if(valueStr != null) {
            try {
                value = Integer.parseInt(valueStr);
            } catch (NumberFormatException e) {
                Log.warning("Error parsing integer value '" + value + 
                        "' for configuration key '" + key  + "'");
            }
        }
        if(value == -999) {
            value = (Integer)key.getDefaultValue();
        }
        return value;
    }

    @Override
    public boolean getBooleanValue(ConfigurationKey key) {
        String valueStr = getStringValue(key.getKey());
        if(valueStr != null) {
            if(valueStr.equalsIgnoreCase("true") ||
               valueStr.equalsIgnoreCase("y") ||
               valueStr.equalsIgnoreCase("t")) return true;
            else if(valueStr.equalsIgnoreCase("false") ||
                    valueStr.equalsIgnoreCase("n") ||
                    valueStr.equalsIgnoreCase("f")) return false;
            else {
                Log.warning("Error parsing boolean value '" + valueStr + 
                        "' for configuration key '" + key  + "'");
            }
        }
        return (Boolean)key.getDefaultValue();
    }
    
    private String getStringValue(String key) {
        String value = configurationCache.get(key);
        if(value == null) {
            value = configurationDao.get(key);
            configurationCache.put(key, value);
        }
        return value;
    }
    
}

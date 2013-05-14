package com.townwizard.db.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.townwizard.db.logger.Log;

@Component("ConfigurationService")
@Transactional
public class ConfigurationServiceImpl implements ConfigurationService {
    
    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private ConfigurationCache configurationCache;
    
    private List<ConfigurationListener> listeners = new ArrayList<>();
    
    @Override
    public void addConfigurationListener(ConfigurationListener listener) {
        listeners.add(listener);
    }
    
    @Override
    public void save(String key, String value) {
        configurationDao.save(key, value);
        configurationCache.remove(key);
        notifyListeners(key);
    }

    @Override
    public void delete(String key) {
        configurationDao.delete(key);
        configurationCache.remove(key);
        notifyListeners(key);
    }

    @Override
    public String getStringValue(ConfigurationKey key) {
        String value = getValueAsString(key);
        if(value != null) {
            return value;
        }
        return (String)key.getDefaultValue();
    }

    @Override
    public int getIntValue(ConfigurationKey key) {
        int value = -999;
        String valueStr = getValueAsString(key);
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
        String valueStr = getValueAsString(key);
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
    
    private String getValueAsString(ConfigurationKey key) {
        String value = configurationCache.get(key.getKey());
        if(value == null) {
            value = configurationDao.get(key.getKey());
            if(value == null) {
                value = key.getDefaultValue().toString();
                configurationDao.save(key.getKey(), value);
            }
            configurationCache.put(key.getKey(), value);  
        }
        return value;
    }
    
    private void notifyListeners(String key) {
        ConfigurationKey cKey = ConfigurationKey.byKey(key);
        for(ConfigurationListener l : listeners) {
            if(Arrays.asList(l.keysOfInterest()).contains(cKey)) {
                l.configurationChanged(cKey);
            }
        }
    }
    
}

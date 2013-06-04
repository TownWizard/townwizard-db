package com.townwizard.db.configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component("ConfigurationCache")
public class ConfigurationCacheImpl implements ConfigurationCache {
    
    private static final Map<String, String> cache = new ConcurrentHashMap<>();
    
    public String get(String key) {
        return cache.get(key);
    }
    
    public void put(String key, String value) {
        cache.put(key, value);
    }
    
    public void remove(String key) {
        cache.remove(key);
    }    

}

package com.townwizard.db.configuration;

public interface ConfigurationCache {

    String get(String key);
    void put(String key, String value);
    void remove(String key);
    
}

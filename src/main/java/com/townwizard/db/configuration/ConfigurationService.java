package com.townwizard.db.configuration;

public interface ConfigurationService {
    
    void save(String key, String value);
    void delete(String key);
    
    String getStringValue(ConfigurationKey key);
    int getIntValue(ConfigurationKey key);
    boolean getBooleanValue(ConfigurationKey key);

}

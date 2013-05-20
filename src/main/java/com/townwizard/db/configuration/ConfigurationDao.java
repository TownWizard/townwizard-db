package com.townwizard.db.configuration;

public interface ConfigurationDao {
    
    void save(String key, String value);
    void save(String key, String value, String description);
    void delete(String key);
    String get(String key);


}

package com.townwizard.db.configuration;

public interface ConfigurationDao {
    
    void save(String key, String value);
    void delete(String key);
    String get(String key);


}

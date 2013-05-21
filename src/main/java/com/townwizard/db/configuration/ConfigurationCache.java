package com.townwizard.db.configuration;

/**
 * Implementations of this class are responsible of caching configuration attributes.
 * In a multi server environment this cache should be distributed, so if the request to change
 * a parameter comes from one of the servers, the new parameter value would be propagated though the
 * claster.
 * 
 * For one server environment, a simple HashMap backed implementation will suffice
 */
public interface ConfigurationCache {

    String get(String key);
    void put(String key, String value);
    void remove(String key);
    
}

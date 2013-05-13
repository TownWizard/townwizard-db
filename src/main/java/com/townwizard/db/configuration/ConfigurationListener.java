package com.townwizard.db.configuration;

public interface ConfigurationListener {
    
    ConfigurationKey[] keysOfInterest();
    void configurationChanged(ConfigurationKey key);

}

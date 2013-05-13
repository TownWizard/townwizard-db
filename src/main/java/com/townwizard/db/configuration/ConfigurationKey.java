package com.townwizard.db.configuration;

public enum ConfigurationKey {
    
    PLACE_INGEST_NUM_HTTP_EXECUTORS("PLACE_INGEST_NUM_HTTP_EXECUTORS", 5),
    PLACE_INGEST_NUM_DB_EXECUTORS("PLACE_INGEST_NUM_DB_EXECUTORS", 1);
    
    private String key; 
    private Object defaultValue;
    ConfigurationKey(String key, Object defaultValue){
        this.key = key;
        this.defaultValue = defaultValue;
    }
       
    public String getKey(){return key;}
    public Object getDefaultValue(){return defaultValue;}

}

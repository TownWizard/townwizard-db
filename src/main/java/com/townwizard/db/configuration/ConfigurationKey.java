package com.townwizard.db.configuration;

public enum ConfigurationKey {
    
    PLACE_INGEST_NUM_HTTP_EXECUTORS("PLACE_INGEST_NUM_HTTP_EXECUTORS", 5),
    PLACE_INGEST_STOPPED("PLACE_INGEST_STOPPED", false);
    
    private String key; 
    private Object defaultValue;
    ConfigurationKey(String key, Object defaultValue){
        this.key = key;
        this.defaultValue = defaultValue;
    }
       
    public String getKey(){return key;}
    public Object getDefaultValue(){return defaultValue;}
    
    public static ConfigurationKey byKey(String key) {
        for(ConfigurationKey k : ConfigurationKey.values()) {
            if(k.getKey().equals(key)) return k;
        }
        return null;
    }

}

package com.townwizard.db.configuration;

public enum ConfigurationKey {
    
    DIRECTORY_USE_PAGING("DIRECTORY_USE_PAGING", true,
            "Use paging when serving directory listings if yes"),
    DIRECTORY_PAGE_SIZE("DIRECTORY_PAGE_SIZE", 25,
            "Number of locations in the directory listing when paging is used"),    
    PLACE_INGEST_NUM_HTTP_EXECUTORS("PLACE_INGEST_NUM_HTTP_EXECUTORS", 30,
            "Number of threads executing HTTP requests to directory providers (such as Yellow Pages)"),
    PLACE_INGEST_STOPPED("PLACE_INGEST_STOPPED", false,
            "If set, the medium priority directory ingest will be suspended");
    
    private String key; 
    private Object defaultValue;
    private String description;
    ConfigurationKey(String key, Object defaultValue, String description){
        this.key = key;
        this.defaultValue = defaultValue;
        this.description = description;
    }
       
    public String getKey(){return key;}
    public Object getDefaultValue(){return defaultValue;}
    public String getDescription(){return description;}
    
    public static ConfigurationKey byKey(String key) {
        for(ConfigurationKey k : ConfigurationKey.values()) {
            if(k.getKey().equals(key)) return k;
        }
        return null;
    }

}

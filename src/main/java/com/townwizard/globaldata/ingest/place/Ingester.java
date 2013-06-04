package com.townwizard.globaldata.ingest.place;

public interface Ingester {
    
    String getZipCode();
    String getCountryCode();
    
    boolean hasNextCategory();
    String getNextCategory();
    boolean allDone();

    int size();
    int done();
    
    void ingest(IngestTask task);
}

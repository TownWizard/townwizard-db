package com.townwizard.globaldata.ingest.place;

import com.townwizard.globaldata.model.directory.PlaceCategory;

public interface Ingester {
    
    String getZipCode();
    String getCountryCode();
    
    boolean hasNextCategory();
    PlaceCategory getNextCategory();
    
    void addProcessedIngestTaskResult(IngestTask task);
    void merge();

}

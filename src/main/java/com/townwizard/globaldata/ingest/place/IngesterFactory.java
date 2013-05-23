package com.townwizard.globaldata.ingest.place;

import java.util.List;

import com.townwizard.globaldata.model.directory.PlaceCategory;

public final class IngesterFactory {
    
    private IngesterFactory(){}
    
    public static Ingester getIngester(String zipCode, String countryCode, List<PlaceCategory> categories) {
        return new JdbcIngester(zipCode, countryCode, categories);
    }
    
}

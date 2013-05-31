package com.townwizard.globaldata.service.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.constants.Constants;
import com.townwizard.db.logger.Log;
import com.townwizard.db.util.JSONUtils;
import com.townwizard.globaldata.connector.YellowPagesConnector;
import com.townwizard.globaldata.model.YellowPages;
import com.townwizard.globaldata.model.directory.Place;

/**
 * Yellow Pages service implementation
 */
@Component("yellowPagesService")
public class YellowPagesServiceImpl implements YellowPagesService {
    
    private static final int NUM_PLACES_IN_RESPONSE = 50;
    private static final int MAX_PAGES = 15;
    
    @Autowired
    private YellowPagesConnector connector;
    
    @Override
    public List<Place> getPageOfPlaces(String zip, String term, int pageNum, int listingCount) {
        try {
            String json = connector.executePlacesRequest(
                    term, zip, Constants.PLACE_DISTANCE_IN_MILES, pageNum, listingCount);
            return jsonToPlaces(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes one HTTP request to get locations
     */
    @Override
    public List<Place> getPlaces(String zip, String term) {
        List<Place> finalResult = new ArrayList<>();
        Exception ex = null;
        
        for(int attempt = 1; attempt <= 3; attempt++) {
            try {
                List<Place> result = null;
                int page = 0;
                do {
                    String json = connector.executePlacesRequest(
                            term, zip, Constants.PLACE_DISTANCE_IN_MILES, page, NUM_PLACES_IN_RESPONSE);
                    result = jsonToPlaces(json);
                    finalResult.addAll(result);
                    page++;
                } while(page < MAX_PAGES  && result.size() >= NUM_PLACES_IN_RESPONSE);
                
                if(attempt > 1) {
                    Log.warning("Successfully retrieved places for zip '" + zip + 
                            "' and term '" + term  + "'" + " after attempt " + attempt);
                }
                return finalResult;
            } catch (Exception e) {
                Log.warning("Exception happend while getting YP places for zip " + zip + 
                        " and term '" + term + "' " + "on attempt " + attempt);
                ex = e;
            }
        }

        Log.warning("Could not get YP places for zip " + zip + " and term '" + term + "'");
        throw new RuntimeException(ex);
    }

    private <T> List<T> jsonToObjects (String json, Class<T> objectClass) 
            throws JSONException, IllegalAccessException, InstantiationException {        
        JSONObject j = new JSONObject(json);
        JSONObject searchResult = j.optJSONObject("searchResult");
        if(searchResult != null) {
            JSONObject searchListings = searchResult.optJSONObject("searchListings");
            if(searchListings != null) { 
                JSONArray data = searchListings.getJSONArray("searchListing");
                return JSONUtils.jsonToObjects(data, objectClass);
            }
        }
        return Collections.emptyList();
    }
    
    private List<Place> jsonToPlaces(String json) throws Exception {
        List<YellowPages.Location> gObjects = jsonToObjects(json, YellowPages.Location.class);
        return ServiceUtils.convertList(gObjects);        
    }

}

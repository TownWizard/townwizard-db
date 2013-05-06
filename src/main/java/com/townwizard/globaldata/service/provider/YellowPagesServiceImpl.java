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
import com.townwizard.db.util.JSONUtils;
import com.townwizard.globaldata.connector.YellowPagesConnector;
import com.townwizard.globaldata.model.YellowPages;
import com.townwizard.globaldata.model.directory.Place;

/**
 * Yellow Pages service implementation
 */
@Component("yellowPagesService")
public class YellowPagesServiceImpl implements YellowPagesService {
    
    @Autowired
    private YellowPagesConnector connector;

    /**
     * Executes one HTTP request to get locations
     */
    @Override
    public List<Place> getPlaces(String zip, String term) {
        try {
            List<Place> finalResult = new ArrayList<>();
            int pageNum = 1;
            List<Place> result = null;
            do {
                result = getPageOfPlaces(zip, Constants.PLACE_DISTANCE_IN_MILES, term, pageNum++);
                finalResult.addAll(result);
            } while(result.size() >= 50);
            
            return finalResult;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private List<Place> getPageOfPlaces(String zip, double distanceInMiles, String term, int pageNum) 
        throws Exception {
        String json = connector.executePlacesRequest(term, zip, distanceInMiles, pageNum);
        List<YellowPages.Location> gObjects = jsonToObjects(json, YellowPages.Location.class);
        List<Place> objects = ServiceUtils.convertList(gObjects);
        return objects;
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

}

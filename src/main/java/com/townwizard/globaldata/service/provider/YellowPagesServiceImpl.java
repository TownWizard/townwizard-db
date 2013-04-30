package com.townwizard.globaldata.service.provider;

import java.util.Collections;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.townwizard.db.util.JSONUtils;
import com.townwizard.globaldata.connector.YellowPagesConnector;
import com.townwizard.globaldata.model.YellowPages;
import com.townwizard.globaldata.model.directory.Location;
import com.townwizard.globaldata.service.ServiceUtils;

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
    public List<Location> getLocations(String term, String zip, double distanceInMiles) {
        try {
            String json = connector.executePlacesRequest(term, zip, distanceInMiles);
            List<YellowPages.Location> gObjects = jsonToObjects(json, YellowPages.Location.class);
            List<Location> objects = ServiceUtils.convertList(gObjects);
            return objects;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
